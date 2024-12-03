package tukano.impl.rest;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import com.microsoft.sqlserver.jdbc.SQLServerException;

import java.sql.Connection;
import java.util.List;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import tukano.api.User;
import tukano.api.Users;
import tukano.api.rest.RestUsers;
import tukano.impl.SQLImpl.JavaUsersForSQL;

@Singleton
public class RestUsersResourceForSQL extends RestResource implements RestUsers {

    final Users impl;

    public RestUsersResourceForSQL() {
        this.impl = JavaUsersForSQL.getInstance();
    }

    @Override
    public String createUser(User user) {
        return super.resultOrThrow(impl.createUser(user));
    }

    @Override
    public Response getUser(String name, String pwd) {
        return impl.getUser(name, pwd);
    }

    @Override
    public User updateUser(String name, String pwd, User user) {
        return super.resultOrThrow(impl.updateUser(name, pwd, user));
    }

    @Override
    public User deleteUser(String name, String pwd) {
        return super.resultOrThrow(impl.deleteUser(name, pwd));
    }

    @Override
    public List<User> searchUsers(String pattern) {
        return super.resultOrThrow(impl.searchUsers(pattern));
    }
}
