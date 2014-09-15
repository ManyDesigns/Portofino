/*
 * Copyright (C) 2005-2014 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsRegistry;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
public class JdbcConnectionProvider extends ConnectionProvider {
    public static final String copyright =
            "Copyright (c) 2005-2014, ManyDesigns srl";

    //**************************************************************************
    // Fields (configured values)
    //**************************************************************************

    protected String driver;
    protected String url;
    protected String username;
    protected String password;

    protected Configuration configuration;
    protected String keyPrefix;

    //**************************************************************************
    // Fields (calcuated values)
    //**************************************************************************

    protected String actualUrl;
    protected String actualUsername;
    protected String actualPassword;

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public JdbcConnectionProvider() {
        super();
    }

    //**************************************************************************
    // Overrides
    //**************************************************************************

    @Override
    public void init(DatabasePlatformsRegistry databasePlatformsRegistry) {
        keyPrefix = "portofino.database." + getDatabase().getDatabaseName() + ".";
        configuration = databasePlatformsRegistry.getPortofinoConfiguration();
        if(url.startsWith(keyPrefix) && configuration.containsKey(url)) {
            actualUrl = configuration.getString(url);
        } else {
            actualUrl = url;
        }
        actualUrl = OgnlTextFormat.format(actualUrl, null);
        if(username.startsWith(keyPrefix) && configuration.containsKey(username)) {
            actualUsername = configuration.getString(username);
        } else {
            actualUsername = username;
        }
        if(password.startsWith(keyPrefix) && configuration.containsKey(password)) {
            actualPassword = configuration.getString(password);
        } else {
            actualPassword = password;
        }
        super.init(databasePlatformsRegistry);
    }


    //**************************************************************************
    // Implementation of ConnectionProvider
    //**************************************************************************

    public String getDescription() {
        return MessageFormat.format(
                "JDBC connection to URL: {0}", url);
    }

    public Connection acquireConnection() throws Exception {
        Class.forName(driver);
        return DriverManager.getConnection(actualUrl, actualUsername, actualPassword);
    }

    public void releaseConnection(Connection conn) {
        DbUtils.closeQuietly(conn);
    }


    //**************************************************************************
    // Getters
    //**************************************************************************

    @XmlAttribute(required = true)
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

    @XmlAttribute(required = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getActualUrl() {
        return actualUrl;
    }

    public void setActualUrl(String url) {
        if(this.url.startsWith(keyPrefix) && configuration.containsKey(this.url)) {
            configuration.setProperty(this.url, url);
        } else {
            this.url = url;
        }
        actualUrl = url;
    }

    public String getActualUsername() {
        return actualUsername;
    }

    public void setActualUsername(String username) {
        if(this.username.startsWith(keyPrefix) && configuration.containsKey(this.username)) {
            configuration.setProperty(this.username, username);
        } else {
            this.username = username;
        }
        actualUsername = username;
    }

    public String getActualPassword() {
        return actualPassword;
    }

    public void setActualPassword(String password) {
        if(this.password.startsWith(keyPrefix) && configuration.containsKey(this.password)) {
            configuration.setProperty(this.password, password);
        } else {
            this.password = password;
        }
        actualPassword = password;
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
}
