/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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
package com.manydesigns.portofino.util;

import com.manydesigns.portofino.context.Context;
import com.manydesigns.portofino.context.hibernate.HibernateContextImpl;
import junit.framework.TestCase;
import org.h2.tools.RunScript;

import java.io.FileReader;
import java.sql.Connection;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class CommonTestUtil extends TestCase{

    //Connessioni e context
    public Connection connPetStore;
    public Connection connPortofino;
    public Connection connDBTest;
    public Context context = null;


    //Script per h2 3 file di configurazione
    public  static final String DIR_TEST_DB = "portofino-war/src/test/resources/database/";
    public  static final String PETSTORE_DB_SCHEMA
            = DIR_TEST_DB +
            "jpetstore-postgres-schema.sql";
    public  static final String PETSTORE_DB_DATA
            = DIR_TEST_DB +
            "jpetstore-postgres-dataload.sql";
    public  static final String PORTODINO4_DB = DIR_TEST_DB + "portofino4.sql";
    public  static final String TEST_DB = DIR_TEST_DB + "hibernatetest.sql";


    public CommonTestUtil() {
        try {
            context = new HibernateContextImpl();
            context.loadConnectionsAsResource("database/portofino-connections.xml");
            context.loadXmlModelAsResource(
                    "database/portofino-model.xml");
            connPortofino = context.getConnectionProvider("portofino").acquireConnection();
            connPetStore = context.getConnectionProvider("jpetstore").acquireConnection();
            connDBTest = context.getConnectionProvider("hibernatetest").acquireConnection();
            RunScript.execute(connPortofino, new FileReader(PORTODINO4_DB));
            RunScript.execute(connPetStore, new FileReader(PETSTORE_DB_SCHEMA));
            RunScript.execute(connPetStore, new FileReader(PETSTORE_DB_DATA));
            RunScript.execute(connDBTest, new FileReader(TEST_DB));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void setUp() throws Exception {


    }

}
