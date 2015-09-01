/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.actions.admin.database.forms;

import com.manydesigns.elements.annotations.*;
import com.manydesigns.elements.util.BootstrapSizes;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.JdbcConnectionProvider;
import com.manydesigns.portofino.model.database.JndiConnectionProvider;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ConnectionProviderForm {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    protected Database database;
    protected JdbcConnectionProvider jdbcConnectionProvider;
    protected JndiConnectionProvider jndiConnectionProvider;

    public ConnectionProviderForm(Database database) {
        this.database = database;
        if(database.getConnectionProvider() instanceof JdbcConnectionProvider) {
            jdbcConnectionProvider = (JdbcConnectionProvider) database.getConnectionProvider();
        } else if(database.getConnectionProvider() instanceof JndiConnectionProvider) {
            jndiConnectionProvider = (JndiConnectionProvider) database.getConnectionProvider();
        } else {
            throw new IllegalArgumentException("Invalid connection provider type: " + database.getConnectionProvider());
        }
    }

    public void setDatabaseName(String databaseName) {
        database.setDatabaseName(databaseName);
    }

    @Updatable(false)
    @Required(true)
    public String getDatabaseName() {
        return database.getDatabaseName();
    }

    public void setTrueString(String trueString) {
        database.setTrueString(StringUtils.defaultIfEmpty(trueString, null));
    }

    public String getTrueString() {
        return database.getTrueString();
    }

    public void setFalseString(String falseString) {
        database.setFalseString(StringUtils.defaultIfEmpty(falseString, null));
    }

    public String getFalseString() {
        return database.getFalseString();
    }

    @FieldSize(50)
    @Required
    @CssClass(BootstrapSizes.FILL_ROW)
    public String getDriver() {
        return jdbcConnectionProvider.getDriver();
    }

    public void setDriver(String driver) {
        jdbcConnectionProvider.setDriver(driver);
    }

    @FieldSize(100)
    @Required
    @CssClass(BootstrapSizes.FILL_ROW)
    @Label("connection URL")
    public String getUrl() {
        return jdbcConnectionProvider.getActualUrl();
    }

    public void setUrl(String url) {
        jdbcConnectionProvider.setActualUrl(url);
    }

    public String getUsername() {
        return jdbcConnectionProvider.getActualUsername();
    }

    public void setUsername(String username) {
        jdbcConnectionProvider.setActualUsername(username);
    }

    @Password
    public String getPassword() {
        return jdbcConnectionProvider.getActualPassword();
    }

    public void setPassword(String password) {
        jdbcConnectionProvider.setActualPassword(password);
    }

    @Required
    public String getJndiResource() {
        return jndiConnectionProvider.getJndiResource();
    }

    public void setJndiResource(String jndiResource) {
        jndiConnectionProvider.setJndiResource(jndiResource);
    }

    @Label("Hibernate dialect (leave empty to use default)")
    public String getHibernateDialect() {
        if(jdbcConnectionProvider != null) {
            return jdbcConnectionProvider.getHibernateDialect();
        } else if(jndiConnectionProvider != null) {
            return jndiConnectionProvider.getHibernateDialect();
        } else {
            return null;
        }
    }

    public void setHibernateDialect(String dialect) {
        if(jdbcConnectionProvider != null) {
            jdbcConnectionProvider.setHibernateDialect(dialect);
        } else if(jndiConnectionProvider != null) {
            jndiConnectionProvider.setHibernateDialect(dialect);
        } else {
            throw new Error("Misconfigured");
        }
    }

    public String getErrorMessage() {
        if(jdbcConnectionProvider != null) {
            return jdbcConnectionProvider.getErrorMessage();
        } else if(jndiConnectionProvider != null) {
            return jndiConnectionProvider.getErrorMessage();
        } else {
            return null;
        }
    }

    public String getStatus() {
        if(jdbcConnectionProvider != null) {
            return jdbcConnectionProvider.getStatus();
        } else if(jndiConnectionProvider != null) {
            return jndiConnectionProvider.getStatus();
        } else {
            return null;
        }
    }

    public Date getLastTested() {
        if(jdbcConnectionProvider != null) {
            return jdbcConnectionProvider.getLastTested();
        } else if(jndiConnectionProvider != null) {
            return jndiConnectionProvider.getLastTested();
        } else {
            return null;
        }
    }

}
