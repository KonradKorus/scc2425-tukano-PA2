package tukano.impl;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import tukano.api.Result;
import tukano.api.User;
import tukano.api.Users;
import tukano.db.CosmosDBLayer;
import tukano.impl.auth.RequestCookies;
import tukano.impl.auth.Session;
import utils.CSVLogger;

import static java.lang.String.format;
import static tukano.api.Result.ErrorCode.BAD_REQUEST;
import static tukano.api.Result.ErrorCode.FORBIDDEN;
import static tukano.api.Result.error;
import static tukano.api.Result.errorOrResult;
import static tukano.api.Result.errorOrValue;
import static tukano.api.Result.ok;

public class JavaUsers implements Users {

	private final static String REDIS_USERS = "users:";

	public static final String ADMIN = "admin";
	public static final String COOKIE_KEY = "scc:session";
	public static final int MAX_COOKIE_AGE = 3600;
	static final String REDIRECT_TO_AFTER_LOGIN = "/";

	CSVLogger csvLogger = new CSVLogger();
	private static Logger Log = Logger.getLogger(JavaUsers.class.getName());
	private static Users instance;
	private final CosmosDBLayer cosmosDBLayerForUsers = new CosmosDBLayer("users");

	synchronized public static Users getInstance() {
		if( instance == null )
			instance = new JavaUsers();
		return instance;
	}
	
	private JavaUsers() {
		System.out.println("==========NOSQLDB SOLUTION FOR USERS============");
	}
	
	@Override
	public Result<String> createUser(User user) {
		long startTime = System.currentTimeMillis();
		Log.info(() -> format("createUser : %s\n", user));

		if( badUserInfo( user ) )
			return error(BAD_REQUEST);

		Result<String> cosmosResult = errorOrValue( cosmosDBLayerForUsers.insertOne(user), user.getId() );

		csvLogger.logToCSV("Create user ", System.currentTimeMillis() - startTime);

		if (cosmosResult.isOK()) {
			RedisJedisPool.addToCache(REDIS_USERS + user.getId(), user);
		}

		return cosmosResult;
	}

	@Override
	public Response getUser(String userId, String pwd) {
		boolean pwdOk = true; // ToDo replace with code to check user password
		if (pwdOk) {
			String uid = UUID.randomUUID().toString();
			var cookie = new NewCookie.Builder(COOKIE_KEY)
					.value(uid).path("/")
					.comment("sessionid")
					.maxAge(MAX_COOKIE_AGE)
					.secure(false) //ideally it should be true to only work for https requests
					.httpOnly(true)
					.build();

			RedisJedisPool.addToCache(uid, new Session( uid, userId));

			Log.info( () -> format("getUser : userId = %s, pwd = %s\n", userId, pwd));

			if (userId == null)
				return Response.status(1).build();

			User cacheUser = RedisJedisPool.getFromCache(REDIS_USERS + userId, User.class);
			if (cacheUser != null) {
				return Response.ok(cacheUser).cookie(cookie).build();
			}
			var result = validatedUserOrError( cosmosDBLayerForUsers.getOne( userId, User.class), pwd);
			return Response.ok(result).cookie(cookie).build();
		} else {
			throw new NotAuthorizedException("Incorrect login");
		}
	}

	@Override
	public Result<User> getUserWithoutPwd(String userId) {
		Log.info( () -> format("getUser : userId = %s, pwd = *no pwd*\n", userId));

		if (userId == null)
			return error(BAD_REQUEST);

		return cosmosDBLayerForUsers.getOne( userId, User.class);
	}

	@Override
	public Result<User> updateUser(String userId, String pwd, User other) {
		long startTime = System.currentTimeMillis();
		Log.info(() -> format("updateUser : userId = %s, pwd = %s, user: %s\n", userId, pwd, other));

		if (badUpdateUserInfo(userId, pwd, other))
			return error(BAD_REQUEST);

		Result <User> cosmosResult = validatedUserOrError( cosmosDBLayerForUsers.updateOne( other), pwd);
		csvLogger.logToCSV("update user", System.currentTimeMillis() - startTime);

		if (cosmosResult.isOK()) {
			RedisJedisPool.addToCache(REDIS_USERS + userId, cosmosResult.value());
		}

		return cosmosResult;
	}


	@Override
	public Result<User> deleteUser(String userId, String pwd) {
		long startTime = System.currentTimeMillis();
		Log.info(() -> format("deleteUser : userId = %s, pwd = %s\n", userId, pwd));

		if (userId == null || pwd == null )
			return error(BAD_REQUEST);

		return  errorOrResult( validatedUserOrError(cosmosDBLayerForUsers.getOne( userId, User.class), pwd), user -> {
			JavaShorts.getInstance().deleteAllShorts(userId, pwd, Token.get(userId));

            Result<User> result = cosmosDBLayerForUsers.deleteUser(user);
			csvLogger.logToCSV("delete user", System.currentTimeMillis() - startTime);

			if (result.isOK()) {
                RedisJedisPool.removeFromCache(REDIS_USERS + userId);
            }

            return  result;
		});
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		long startTime = System.currentTimeMillis();
		Log.info( () -> format("searchUsers : patterns = %s\n", pattern));

		var query = format("SELECT * FROM users u WHERE UPPER(u.id) LIKE '%%%s%%'", pattern.toUpperCase());
		var cosmosResult = cosmosDBLayerForUsers.query(User.class, query)
				.value()
				.stream()
				.map(User::copyWithoutPassword)
				.toList();

		csvLogger.logToCSV("search users", System.currentTimeMillis() - startTime);
//		Getting cached users is obsolete since we need to get all of them from database anyway
//		List<User> cacheUsers = RedisJedisPool.getByKeyPatternFromCache(REDIS_USERS + format("*%s*", pattern.toUpperCase()), User.class);
//		List<User> cacheUsersWithoutPwd = cacheUsers.stream().map(User::copyWithoutPassword).toList();

		return ok(cosmosResult);
	}

	
	private Result<User> validatedUserOrError( Result<User> res, String pwd ) {
		if( res.isOK())
			return res.value().getPwd().equals( pwd ) ? res : error(FORBIDDEN);
		else
			return res;
	}
	
	private boolean badUserInfo( User user) {
		return (user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null);
	}
	
	private boolean badUpdateUserInfo( String userId, String pwd, User info) {
		return (userId == null || pwd == null || info.getId() != null && ! userId.equals( info.getId()));
	}

	static public Session validateSession(String userId) throws NotAuthorizedException {
		var cookies = RequestCookies.get();

		if (userId == null) {
			return validateUserSession(cookies.get(COOKIE_KEY));
		}
		return validateCorrectUserSession(cookies.get(COOKIE_KEY), userId);
	}

	static public Session validateUserSession(Cookie cookie) throws NotAuthorizedException {

		if (cookie == null)
			throw new NotAuthorizedException("No session initialized");

		var session = RedisJedisPool.getFromCache(cookie.getValue(), Session.class);
		if (session == null)
			throw new NotAuthorizedException("No valid session initialized");

		if (session.user() == null || session.user().length() == 0)
			throw new NotAuthorizedException("No valid session initialized");

		return session;
	}

	static public Session validateCorrectUserSession(Cookie cookie, String userId) throws NotAuthorizedException {

		if (cookie == null)
			throw new NotAuthorizedException("No session initialized");

		var session = RedisJedisPool.getFromCache(cookie.getValue(), Session.class);
		if (session == null)
			throw new NotAuthorizedException("No valid session initialized");

		if (session.user() == null || session.user().length() == 0)
			throw new NotAuthorizedException("No valid session initialized");

		if (!session.user().equals(userId))
			throw new NotAuthorizedException("Invalid user : " + session.user());

		return session;
	}
}
