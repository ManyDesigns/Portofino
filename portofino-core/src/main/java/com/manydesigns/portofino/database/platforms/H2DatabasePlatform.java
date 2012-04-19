/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.database.platforms;

import com.manydesigns.portofino.model.database.ConnectionProvider;
import org.hibernate.dialect.PostgreSQLDialect;

import java.sql.Connection;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class H2DatabasePlatform extends AbstractDatabasePlatform {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public final static String DESCRIPTION = "H2";
    public final static String STANDARD_DRIVER_CLASS_NAME =
            "org.h2.Driver";

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public H2DatabasePlatform() {
        super(new PostgreSQLDialect());
    }

    //**************************************************************************
    // Implementation of DatabaseAbstraction
    //**************************************************************************

    public String getDescription() {
        return DESCRIPTION;
    }

    public String getStandardDriverClassName() {
        return STANDARD_DRIVER_CLASS_NAME;
    }

    public boolean isApplicable(ConnectionProvider connectionProvider) {
        return "H2".equals(connectionProvider.getDatabaseProductName());
    }

    @Override
    public void shutdown(ConnectionProvider connectionProvider) {
        super.shutdown(connectionProvider);
        Connection connection = null;
        try {
            connection = connectionProvider.acquireConnection();
            connection.createStatement().execute("SHUTDOWN");
        } catch (Exception e) {
            logger.warn("Could not shutdown connection provider: {}",
                    connectionProvider.getDatabase().getDatabaseName());
        } finally {
            connectionProvider.releaseConnection(connection);
        }
    }
}