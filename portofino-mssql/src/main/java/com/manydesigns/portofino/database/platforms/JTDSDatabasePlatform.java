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

package com.manydesigns.portofino.database.platforms;

import com.manydesigns.portofino.model.database.ConnectionProvider;
import org.hibernate.dialect.SQLServerDialect;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class JTDSDatabasePlatform extends AbstractDatabasePlatform {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public final static String DESCRIPTION = "Microsoft SQL Server (jTDS driver)";
    public final static String STANDARD_DRIVER_CLASS_NAME =
            "net.sourceforge.jtds.jdbc.Driver";

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public JTDSDatabasePlatform() {
        super(new SQLServerDialect(), "jdbc:jtds:sqlserver://<server>[:<port>][/<database>][;instance=<instance>]");
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

    public void test() {
	super.test();
	if(status == STATUS_OK) {
	    checkJdbc4();
	}
    }

    protected void checkJdbc4() {
	try {
	    Class.forName("java.sql.NClob");
	} catch(ClassNotFoundException e) {
	    status = STATUS_DRIVER_ERROR;
	}
    }

    public boolean isApplicable(ConnectionProvider connectionProvider) {
        return connectionProvider
                .getDatabaseProductName()
                .startsWith("Microsoft SQL Server") &&
	       connectionProvider.getDriverName().contains("jTDS");
    }
}
