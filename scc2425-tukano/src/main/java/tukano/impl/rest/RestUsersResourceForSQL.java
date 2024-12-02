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
        try {
            Connection driver = new SQLServerDriver().connect("jdbc:sqlserver://scc2324sql71846.database.windows.net:1433;database=scc2324sql71846;user=scc2324@scc2324sql71846;password=Asdfqwerzxcv12!@;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;", null);
        } catch (SQLServerException e) {
            throw new RuntimeException(e);
        }

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
