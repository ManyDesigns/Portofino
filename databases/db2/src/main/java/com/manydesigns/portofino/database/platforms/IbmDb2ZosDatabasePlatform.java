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

package com.manydesigns.portofino.database.platforms;

import com.manydesigns.portofino.database.model.ConnectionProvider;
import com.manydesigns.portofino.database.model.platforms.AbstractDatabasePlatform;
import org.hibernate.dialect.DB2zDialect;


/*
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 * @author Manuel Durán Aguete  - manuel@aguete.org
*/
public class IbmDb2ZosDatabasePlatform extends AbstractDatabasePlatform {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    public final static String DESCRIPTION = "IBM DB2 for z/OS";
    public final static String STANDARD_DRIVER_CLASS_NAME =
            "com.ibm.db2.jcc.DB2Driver";

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public IbmDb2ZosDatabasePlatform()  {
        super(DB2zDialect.class.getName(), "jdbc:db2://<host>[:<port>]/<database_name>");
    }

    //**************************************************************************
    // Implementation of DatabaseAbstraction
    //**************************************************************************

    public boolean isDialectAutodetected() {
        return false;
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    public String getStandardDriverClassName() {
        return STANDARD_DRIVER_CLASS_NAME;
    }

    public boolean isApplicable(ConnectionProvider connectionProvider) {
        return connectionProvider.getDatabaseProductName().startsWith("DB2");
    }
}
