package com.manydesigns.portofino.model.database.driver;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.rds.auth.GetIamAuthTokenRequest;
import com.amazonaws.services.rds.auth.RdsIamAuthTokenGenerator;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

public abstract class AWSRdsDriver implements Driver {
    public static final org.slf4j.Logger logger =
            LoggerFactory.getLogger(AWSRdsDriver.class);

    public static final String SCHEME = "jdbc-rds";
    public static final int MAX_RETRY = 5;
    public static final String PROPERTY_PREFIX = "rds_drivers";

    private String realDriverClass;
    private String regionName = "eu-south-1";
    private String rdsInstanceHostName = "md-postgres01.cqeklspk4krg.eu-south-1.rds.amazonaws.com";
    private String rdsInstancePort = "5432";
    private String username;

    protected AWSRdsDriver() {
        setProperties();
        AWSRdsDriver.register(this);
    }

    private void setProperties() {
        this.realDriverClass = getDefaultDriverClass();
    }

    private static void shutdown(AWSRdsDriver driver) {
    }

    /**
     * Registers a driver along with the <code>DriverAction</code> implementation.
     *
     * @param driver                                            The driver to register.
     *
     * @throws RuntimeException                                 If the driver could not be registered.
     */
    protected static void register(AWSRdsDriver driver) {
        try {
            DriverManager.registerDriver(driver, () -> shutdown(driver));
        } catch (SQLException e) {
            throw new RuntimeException("Driver could not be registered.", e);
        }
    }

    public abstract String getPropertySubprefix();

    private String unwrapUrl(String jdbcUrl) {
        if (!jdbcUrl.startsWith(SCHEME)) {
            throw new IllegalArgumentException("JDBC URL is malformed. Must use scheme, \""+ SCHEME + "\".");
        }
        return jdbcUrl.replaceFirst(SCHEME, "jdbc");
    }

    public Driver getWrapperDriver() {
        Enumeration<Driver> availableDrivers = DriverManager.getDrivers();
        while (availableDrivers.hasMoreElements()) {
            Driver driver = availableDrivers.nextElement();
            if (driver.getClass().getName().equals(this.realDriverClass)) {
                return driver;
            }
        }

        throw new IllegalStateException("No Driver has been registered with name, "+ this.realDriverClass
                + ". Please check your system properties or database.xml for typos. Also ensure that the Driver registers itself.");
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        if (url == null) {
            throw new SQLException("url cannot be null.");
        }

        if (url.startsWith(SCHEME)) {
            return getWrapperDriver().acceptsURL(unwrapUrl(url));
        } else {
            return false;
        }
    }

    /**
     * Get the default real driver class name for this driver.
     *
     * @return String                                           The default real driver class name
     */
    public abstract String getDefaultDriverClass();

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }

        if (info.containsKey(PROPERTY_PREFIX + "rdsInstanceHostName")) {
            this.rdsInstanceHostName = info.getProperty(PROPERTY_PREFIX + "rdsInstanceHostName");
        }
        if (info.containsKey(PROPERTY_PREFIX + "rdsInstancePort")) {
            this.rdsInstancePort = info.getProperty(PROPERTY_PREFIX + "rdsInstancePort");
        }
        if (info.containsKey(PROPERTY_PREFIX + "regionName")) {
            this.regionName = info.getProperty(PROPERTY_PREFIX + "regionName");
        }
        if (info.containsKey("username")) {
            this.username = info.getProperty("username");
        }

        logger.info("Current configuration:\n" + toString());

        String unwrappedUrl = "";
        if (url.startsWith(SCHEME)) {
            unwrappedUrl = unwrapUrl(url);
        } else {
            throw new IllegalStateException("JDBC URL is malformed. Must use scheme, \""+ SCHEME + "\".");
        }

        return connectWithAuthToken(unwrappedUrl, info);
    }

    private Connection connectWithAuthToken(String unwrappedUrl, Properties info) throws SQLException {
        Properties updatedInfo = new Properties(info);
        updatedInfo.setProperty("password", generateAuthToken());

        return getWrapperDriver().connect(unwrappedUrl, updatedInfo);
    }

    protected String generateAuthToken() {
        logger.info("Generating new auth token");
        RdsIamAuthTokenGenerator generator = RdsIamAuthTokenGenerator.builder()
                .credentials(new DefaultAWSCredentialsProviderChain())
                .region(regionName)
                .build();

        return generator.getAuthToken(GetIamAuthTokenRequest.builder()
                .hostname(rdsInstanceHostName)
                .port(Integer.parseInt(rdsInstancePort))
                .userName(username)
                .build());
    }

    @Override
    public int getMajorVersion() {
        return getWrapperDriver().getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return getWrapperDriver().getMinorVersion();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return getWrapperDriver().getParentLogger();
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return getWrapperDriver().getPropertyInfo(unwrapUrl(url), info);
    }

    @Override
    public boolean jdbcCompliant() {
        return getWrapperDriver().jdbcCompliant();
    }

    @Override
    public String toString() {
        return "AWSRdsDriver{" +
                "realDriverClass='" + realDriverClass + '\'' +
                ", regionName='" + regionName + '\'' +
                ", rdsInstanceHostName='" + rdsInstanceHostName + '\'' +
                ", rdsInstancePort='" + rdsInstancePort + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
