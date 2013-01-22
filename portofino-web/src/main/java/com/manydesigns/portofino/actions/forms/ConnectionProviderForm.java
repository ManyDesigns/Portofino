/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */

package com.manydesigns.portofino.actions.forms;

import com.manydesigns.elements.annotations.*;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.JdbcConnectionProvider;
import com.manydesigns.portofino.model.database.JndiConnectionProvider;

import java.util.Date;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ConnectionProviderForm {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

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

    @FieldSize(50)
    @Required
    public String getDriver() {
        return jdbcConnectionProvider.getDriver();
    }

    public void setDriver(String driver) {
        jdbcConnectionProvider.setDriver(driver);
    }

    @FieldSize(100)
    @Required
    @Label("connection URL")
    public String getUrl() {
        return jdbcConnectionProvider.getUrl();
    }

    public void setUrl(String url) {
        jdbcConnectionProvider.setUrl(url);
    }

    public String getUsername() {
        return jdbcConnectionProvider.getUsername();
    }

    public void setUsername(String username) {
        jdbcConnectionProvider.setUsername(username);
    }

    @Password
    public String getPassword() {
        return jdbcConnectionProvider.getPassword();
    }

    public void setPassword(String password) {
        jdbcConnectionProvider.setPassword(password);
    }

    @Required
    public String getJndiResource() {
        return jndiConnectionProvider.getJndiResource();
    }

    public void setJndiResource(String jndiResource) {
        jndiConnectionProvider.setJndiResource(jndiResource);
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
