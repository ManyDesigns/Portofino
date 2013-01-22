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
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public final static String DESCRIPTION = "Microsoft SQL Server (jTDS driver - Java 6+)";
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
