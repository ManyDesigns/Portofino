/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.model.database;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.auth.GetIamAuthTokenRequest;
import com.amazonaws.services.rds.auth.RdsIamAuthTokenGenerator;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.portofino.model.database.driver.AWSRdsDriver;
import com.manydesigns.portofino.model.database.platforms.DatabasePlatformsRegistry;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Properties;

/*
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Iacopo Filiberto - iacopo.filiberto@manydesigns.com
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"driver", "url", "username", "rdsInstanceHostName", "rdsInstancePort", "regionName"})
public class AWSConnectionProvider extends ConnectionProvider {
    public static final String copyright =
            "Copyright (C) 2005-2021 ManyDesigns srl";

    //**************************************************************************
    // Fields (configured values)
    //**************************************************************************

    protected String driver;
    protected String url;
    protected String username;
    protected String keyPrefix;

    //AWS
    protected String rdsInstanceHostName;
    protected String rdsInstancePort;
    protected String regionName;

    private static final String KEY_STORE_TYPE = "JKS";
    private static final String KEY_STORE_PROVIDER = "SUN";
    private static final String KEY_STORE_FILE_PREFIX = "sys-connect-via-ssl-test-cacerts";
    private static final String KEY_STORE_FILE_SUFFIX = ".jks";
    private static final String DEFAULT_KEY_STORE_PASSWORD = "changeit";


    //**************************************************************************
    // Fields (calcuated values)
    //**************************************************************************

    protected String actualUrl;
    protected String actualUsername;
    protected String actualPassword;

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public AWSConnectionProvider() {
        super();
    }

    //**************************************************************************
    // Overrides
    //**************************************************************************

    @Override
    public void init(DatabasePlatformsRegistry databasePlatformsRegistry) {
        try {
            setSslProperties();
        } catch (Exception e) {
            logger.error("Cannot upload AWS keystore", e.getMessage());
        }
        keyPrefix = "portofino.database." + getDatabase().getDatabaseName() + ".";
        configuration = databasePlatformsRegistry.getPortofinoConfiguration();
        if (url == null || url.equals(keyPrefix + "url")) {
            actualUrl = configuration.getString(keyPrefix + "url");
            if (actualUrl == null) {
                status = STATUS_ERROR;
                throw new RuntimeException("Invalid connection provider for database " + getDatabase().getDatabaseName() + " - no URL specified");
            }
        } else {
            actualUrl = url;
        }
        actualUrl = OgnlTextFormat.format(actualUrl, null);
        if (username == null || username.equals(keyPrefix + "username")) {
            actualUsername = configuration.getString(keyPrefix + "username");
            if (actualUsername == null) {
                status = STATUS_ERROR;
                throw new RuntimeException("Invalid connection provider for database " + getDatabase().getDatabaseName() + " - no username specified");
            }
        } else {
            actualUsername = username;
        }

        super.init(databasePlatformsRegistry);
    }

    //**************************************************************************
    // Implementation of ConnectionProvider
    //**************************************************************************

    public String getDescription() {
        return MessageFormat.format(
                "RDS connection to URL: {0}", actualUrl);
    }

    public Connection acquireConnection() throws SQLException {
        try {
            if (driver != null) {
                Class.forName(driver);
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException(e.getMessage());
        }
        return DriverManager.getConnection(url, setConnectionProperties());
    }

    //**************************************************************************
    // Getters
    //**************************************************************************

    @XmlAttribute
    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    @XmlAttribute(required = true)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlAttribute(required = false)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getActualUrl() {
        return actualUrl;
    }

    public void setActualUrl(String url) {
        if (this.url == null || this.url.equals(keyPrefix + "url")) {
            configuration.setProperty(keyPrefix + "url", url);
        } else {
            this.url = url;
        }
        actualUrl = url;
    }

    public String getActualUsername() {
        return actualUsername;
    }

    public void setActualUsername(String username) {
        if (this.username == null || this.username.equals(keyPrefix + "username")) {
            configuration.setProperty(keyPrefix + "username", username);
        } else {
            this.username = username;
        }
        actualUsername = username;
    }

    public String getActualPassword() {
        return generateAuthToken();
    }

    @XmlAttribute(required = true)
    public String getRdsInstanceHostName() {
        return rdsInstanceHostName;
    }

    public void setRdsInstanceHostName(String rdsInstanceHostName) {
        this.rdsInstanceHostName = rdsInstanceHostName;
    }

    @XmlAttribute(required = true)
    public String getRdsInstancePort() {
        return rdsInstancePort;
    }

    public void setRdsInstancePort(String rdsInstancePort) {
        this.rdsInstancePort = rdsInstancePort;
    }

    @XmlAttribute(required = true)
    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    //**************************************************************************
    // Other methods
    //**************************************************************************

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("driver", driver)
                .append("url", actualUrl)
                .append("username", actualUsername)
                .append("password", actualPassword)
                .toString();
    }

    /**
     * This method sets the mysql connection properties which includes the IAM Database Authentication token
     * as the password. It also specifies that SSL verification is required.
     *
     * @return
     */
    public Properties setConnectionProperties() {
        Properties properties = new Properties();
        properties.setProperty("verifyServerCertificate", "true");
        properties.setProperty("useSSL", "true");
        properties.setProperty(AWSRdsDriver.PROPERTY_PREFIX + ".rdsInstanceHostName", rdsInstanceHostName);
        properties.setProperty(AWSRdsDriver.PROPERTY_PREFIX + ".rdsInstancePort", rdsInstancePort);
        properties.setProperty(AWSRdsDriver.PROPERTY_PREFIX + ".regionName", regionName);
        properties.setProperty("username", this.username);
//        properties.setProperty("password", generateAuthToken());
        return properties;
    }

    /**
     * This method generates the IAM Auth Token.
     * An example IAM Auth Token would look like follows:
     * btusi123.cmz7kenwo2ye.rds.cn-north-1.amazonaws.com.cn:3306/?Action=connect&DBUser=iamtestuser&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20171003T010726Z&X-Amz-SignedHeaders=host&X-Amz-Expires=899&X-Amz-Credential=AKIAPFXHGVDI5RNFO4AQ%2F20171003%2Fcn-north-1%2Frds-db%2Faws4_request&X-Amz-Signature=f9f45ef96c1f770cdad11a53e33ffa4c3730bc03fdee820cfdf1322eed15483b
     *
     * @return
     */
    private String generateAuthToken() {
        AWSCredentials awsCredentials;
        awsCredentials = new DefaultAWSCredentialsProviderChain().getCredentials();

        RdsIamAuthTokenGenerator generator = RdsIamAuthTokenGenerator.builder()
                .credentials(new AWSStaticCredentialsProvider(awsCredentials))
                .region(regionName)
                .build();

        return generator.getAuthToken(GetIamAuthTokenRequest.builder()
                .hostname(rdsInstanceHostName)
                .port(Integer.parseInt(rdsInstancePort))
                .userName(username)
                .build());
    }

    /**
     * This method sets the SSL properties which specify the key store file, its type and password:
     *
     * @throws Exception
     */
    private void setSslProperties() throws Exception {
        System.setProperty("javax.net.ssl.trustStore", createKeyStoreFile());
        System.setProperty("javax.net.ssl.trustStoreType", KEY_STORE_TYPE);
        System.setProperty("javax.net.ssl.trustStorePassword", DEFAULT_KEY_STORE_PASSWORD);
    }

    /**
     * This method returns the path of the Key Store File needed for the SSL verification during the IAM Database Authentication to
     * the db instance.
     *
     * @return
     * @throws Exception
     */
    private String createKeyStoreFile() throws Exception {
        return createKeyStoreFile(createCertificate()).getPath();
    }

    /**
     * This method generates the SSL certificate
     *
     * @return
     * @throws Exception
     */
    private X509Certificate createCertificate() throws Exception {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        URL url = getClass().getClassLoader().getResource("rds-combined-ca-bundle.pem");
        if (url == null) {
            throw new FileNotFoundException("rds-ca certificate not found");
        }
        try (InputStream certInputStream = url.openStream()) {
            return (X509Certificate) certFactory.generateCertificate(certInputStream);
        }
    }

    private String getCertificateFromRegion(String regionName) {
        switch (Regions.fromName(regionName)) {
            case AP_EAST_1:
                return "rds-ca-2019-ap-east-1.pem";
            case EU_WEST_1:
                return "rds-ca-2019-eu-west-1.pem";
            case EU_WEST_2:
                return "rds-ca-2019-eu-west-2.pem";
            case EU_WEST_3:
                return "rds-ca-2019-eu-west-3.pem";
            case US_EAST_1:
                return "rds-ca-2019-us-east-1.pem";
            case US_EAST_2:
                return "rds-ca-2019-us-east-2.pem";
            case US_WEST_2:
                return "rds-ca-2019-us-west-2.pem";
            case EU_SOUTH_1:
                return "rds-ca-2019-eu-south-1.pem";
            case EU_NORTH_1:
                return "rds-ca-2019-eu-north-1.pem";
            case EU_CENTRAL_1:
                return "rds-ca-2019-eu-central-1.pem";
            case CA_CENTRAL_1:
                return "rds-ca-2019-ca-central-1.pem";
            case AF_SOUTH_1:
                return "rds-ca-2019-af-south-1.pem";
            case AP_NORTHEAST_1:
                return "rds-ca-2019-ap-northeast-1.pem";
            case AP_NORTHEAST_2:
                return "rds-ca-2019-ap-northeast-2.pem";
            case AP_NORTHEAST_3:
                return "rds-ca-2019-ap-northeast-3.pem";
            case AP_SOUTH_1:
                return "rds-ca-2019-ap-south-1.pem";
            case AP_SOUTHEAST_1:
                return "rds-ca-2019-ap-southeast-1.pem";
            case AP_SOUTHEAST_2:
                return "rds-ca-2019-ap-southeast-2.pem";
            case ME_SOUTH_1:
                return "rds-ca-2019-me-south-1.pem";
            case SA_EAST_1:
                return "rds-ca-2019-sa-east-1.pem";
            default:
                return "rds-ca-2019-eu-west-1.pem";

        }
    }

    /**
     * This method creates the Key Store File
     *
     * @param rootX509Certificate - the SSL certificate to be stored in the KeyStore
     * @return
     * @throws Exception
     */
    private File createKeyStoreFile(X509Certificate rootX509Certificate) throws Exception {
        File keyStoreFile = File.createTempFile(KEY_STORE_FILE_PREFIX, KEY_STORE_FILE_SUFFIX);
        try (FileOutputStream fos = new FileOutputStream(keyStoreFile.getPath())) {
            KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE, KEY_STORE_PROVIDER);
            ks.load(null);
            ks.setCertificateEntry("rootCaCertificate", rootX509Certificate);
            ks.store(fos, DEFAULT_KEY_STORE_PASSWORD.toCharArray());
        }
        return keyStoreFile;
    }

    /**
     * This method clears the SSL properties.
     *
     * @throws Exception
     */
    private void clearSslProperties() throws Exception {
        System.clearProperty("javax.net.ssl.trustStore");
        System.clearProperty("javax.net.ssl.trustStoreType");
        System.clearProperty("javax.net.ssl.trustStorePassword");
    }
}
