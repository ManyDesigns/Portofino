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
import com.manydesigns.portofino.model.DataModel;
import com.manydesigns.portofino.model.Database;
import com.manydesigns.portofino.model.Schema;
import com.manydesigns.portofino.model.Table;
import com.manydesigns.portofino.model.diff.ModelDiff;
import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.logging.Level;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class JdbcMetadataReaderTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    ConnectionProvider connectionProvider;
    DatabaseAbstraction databaseAbstraction;
    DataModel dataModel;

    public void setUp() throws ClassNotFoundException, SQLException {
        LogUtil.initializeLoggingSystem();

        connectionProvider =
                new JdbcConnectionProvider(
                        "org.postgresql.Driver",
                        "jdbc:postgresql://127.0.0.1:5432/jpetstore",
                        "manydesigns",
                        "manydesigns");

        databaseAbstraction =
                DatabaseAbstractionManager.getManager()
                        .getDatabaseAbstraction(connectionProvider);

        dataModel = databaseAbstraction.readModelFromConnection("dbprova");
    }

    public void testReadModelFromConnection() {
        CommonDatabaseAbstraction.logger.setLevel(Level.FINE);

        assertEquals(1, dataModel.getDatabases().size());
        assertEquals(4, dataModel.getAllSchemas().size());
        assertEquals(13, dataModel.getAllTables().size());
        assertEquals(86, dataModel.getAllColumns().size());
    }

    public void testDiff() {
        ModelDiff.logger.setLevel(Level.FINE);

        Database database1 = dataModel.getDatabases().get(0);

        Database database2 = new Database("pippo", null);
        Schema schema2 = new Schema(database2.getDatabaseName(), "public");
        database2.getSchemas().add(schema2);
        Table table2 = new Table(schema2.getDatabaseName(),
                schema2.getSchemaName(), "product");
        schema2.getTables().add(table2);

        ModelDiff diff = new ModelDiff();
        diff.diff(database1, database2);

        assertEquals(20, diff.size());
        assertEquals("Database names dbprova / pippo are different", diff.get(0));
        assertEquals("Model 2 does not contain schema: pippo.information_schema", diff.get(1));
        assertEquals("Model 2 does not contain schema: pippo.pg_catalog", diff.get(2));
        assertEquals("Model 2 does not contain schema: pippo.pg_toast_temp_1", diff.get(3));
        assertEquals("Model 2 does not contain table: pippo.public.account", diff.get(4));
        assertEquals("Model 2 does not contain table: pippo.public.bannerdata", diff.get(5));
        assertEquals("Model 2 does not contain table: pippo.public.category", diff.get(6));
        assertEquals("Model 2 does not contain table: pippo.public.inventory", diff.get(7));
        assertEquals("Model 2 does not contain table: pippo.public.item", diff.get(8));
        assertEquals("Model 2 does not contain table: pippo.public.lineitem", diff.get(9));
        assertEquals("Model 2 does not contain table: pippo.public.orders", diff.get(10));
        assertEquals("Model 2 does not contain table: pippo.public.orderstatus", diff.get(11));
        assertEquals("Model 2 does not contain table: pippo.public.profile", diff.get(12));
        assertEquals("Model 2 does not contain table: pippo.public.sequence", diff.get(13));
        assertEquals("Model 2 does not contain table: pippo.public.signon", diff.get(14));
        assertEquals("Model 2 does not contain table: pippo.public.supplier", diff.get(15));
        assertEquals("Model 2 does not contain column: pippo.public.product.category", diff.get(16));
        assertEquals("Model 2 does not contain column: pippo.public.product.descn", diff.get(17));
        assertEquals("Model 2 does not contain column: pippo.public.product.name", diff.get(18));
        assertEquals("Model 2 does not contain column: pippo.public.product.productid", diff.get(19));
    }
}
