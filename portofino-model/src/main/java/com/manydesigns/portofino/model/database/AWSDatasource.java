package com.manydesigns.portofino.model.database;

import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/*
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Iacopo Filiberto - iacopo.filiberto@manydesigns.com
 */
public class AWSDatasource  implements DataSource, Serializable {
    AWSConnectionProvider awsConnectionProvider;
    private int loginTimeOut;
    public static final org.slf4j.Logger logger = LoggerFactory.getLogger(ConnectionProvider.class);

    public AWSDatasource (AWSConnectionProvider awsConnectionProvider){
        this.awsConnectionProvider = awsConnectionProvider;
        loginTimeOut = 10; //taken from Default Postgres Value
    }

    public String getDescription() {
        return "Non-Pooling DataSource for AWS RDS";
    }
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(getClass());
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(getClass())) {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }

    @Override
    public Connection getConnection() throws SQLException {
        return awsConnectionProvider.acquireConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return awsConnectionProvider.acquireConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        // NOOP
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        loginTimeOut = seconds;

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return loginTimeOut;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger("org.postgresql");
    }
}
