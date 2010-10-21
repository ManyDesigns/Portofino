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

package com.manydesigns.portofino.database;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.portofino.database.platforms.AbstractDatabasePlatform;
import com.manydesigns.portofino.model.datamodel.Database;
import com.manydesigns.portofino.model.datamodel.Schema;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.model.diff.ModelDiff;
import com.manydesigns.portofino.util.CommonTestUtil;

import java.util.logging.Level;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class JdbcMetadataReaderTest extends CommonTestUtil {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    ConnectionProvider connectionProvider;
    Database database;

    public void setUp()  {
        LogUtil.initializeLoggingSystem();
        super.setUp();

        connectionProvider=context.getConnectionProvider("jpetstore");
        connectionProvider.test();
        database = connectionProvider.readModel();

    }

    public void testReadModelFromConnection() {
        AbstractDatabasePlatform.logger.setLevel(Level.FINE);

        assertEquals(2, database.getSchemas().size());
        assertEquals(13, database.getAllTables().size());
        assertEquals(86, database.getAllColumns().size());
    }

    public void testDiff() {
        ModelDiff.logger.setLevel(Level.FINE);
        Database database2 = new Database("pippo");
        Schema schema2 = new Schema(database2, "PUBLIC");
        database2.getSchemas().add(schema2);
        Table table2 = new Table(schema2, "PRODUCT");
        schema2.getTables().add(table2);
        ModelDiff diff = new ModelDiff();
        diff.diff(database, database2);
        assertEquals(18, diff.size());
        assertEquals("Database names jpetstore / pippo are different", diff.get(0));
        assertEquals("Model 2 does not contain schema: pippo.INFORMATION_SCHEMA", diff.get(1));
        assertEquals("Model 2 does not contain table: pippo.PUBLIC.ACCOUNT", diff.get(2));
        assertEquals("Model 2 does not contain table: pippo.PUBLIC.BANNERDATA", diff.get(3));
        assertEquals("Model 2 does not contain table: pippo.PUBLIC.CATEGORY", diff.get(4));
        assertEquals("Model 2 does not contain table: pippo.PUBLIC.INVENTORY", diff.get(5));
        assertEquals("Model 2 does not contain table: pippo.PUBLIC.ITEM", diff.get(6));
        assertEquals("Model 2 does not contain table: pippo.PUBLIC.LINEITEM", diff.get(7));
        assertEquals("Model 2 does not contain table: pippo.PUBLIC.ORDERS", diff.get(8));
        assertEquals("Model 2 does not contain table: pippo.PUBLIC.ORDERSTATUS", diff.get(9));
        assertEquals("Model 2 does not contain table: pippo.PUBLIC.PROFILE", diff.get(10));
        assertEquals("Model 2 does not contain table: pippo.PUBLIC.SEQUENCE", diff.get(11));
        assertEquals("Model 2 does not contain table: pippo.PUBLIC.SIGNON", diff.get(12));
        assertEquals("Model 2 does not contain table: pippo.PUBLIC.SUPPLIER", diff.get(13));
        assertEquals("Model 2 does not contain column: pippo.PUBLIC.PRODUCT.CATEGORY", diff.get(14));
        assertEquals("Model 2 does not contain column: pippo.PUBLIC.PRODUCT.DESCN", diff.get(15));
        assertEquals("Model 2 does not contain column: pippo.PUBLIC.PRODUCT.NAME", diff.get(16));
        assertEquals("Model 2 does not contain column: pippo.PUBLIC.PRODUCT.PRODUCTID", diff.get(17));
    }
}
