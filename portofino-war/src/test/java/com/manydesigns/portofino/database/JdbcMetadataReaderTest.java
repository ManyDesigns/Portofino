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

import com.manydesigns.portofino.AbstractPortofinoTest;
import com.manydesigns.portofino.database.platforms.AbstractDatabasePlatform;
import com.manydesigns.portofino.model.annotations.Annotation;
import com.manydesigns.portofino.model.datamodel.*;
import com.manydesigns.portofino.model.diff.DatabaseDiff;
import com.manydesigns.portofino.model.diff.DiffUtil;
import com.manydesigns.portofino.model.diff.MessageDiffer;

import java.util.List;
import java.util.logging.Level;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class JdbcMetadataReaderTest extends AbstractPortofinoTest {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    ConnectionProvider connectionProvider;
    Database database;

    public void setUp() throws Exception {
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
        DiffUtil.logger.setLevel(Level.FINE);
        Database pippoDatabase = new Database("pippo");
        Schema publicSchema = new Schema(pippoDatabase, "PUBLIC");
        pippoDatabase.getSchemas().add(publicSchema);

        Table productTable = new Table(publicSchema, "PRODUCT");
        publicSchema.getTables().add(productTable);
        Column descnColumn = new Column(productTable, "DESCN", "varchar", true, false, 255, 0, true);
        productTable.getColumns().add(descnColumn);
        Annotation annotation1 = new Annotation(
                com.manydesigns.elements.annotations.Label.class.getName());
        descnColumn.getAnnotations().add(annotation1);

        Table supplierTable = new Table(publicSchema, "SUPPLIER");
        publicSchema.getTables().add(supplierTable);
        PrimaryKey supplierPrimaryKey = new PrimaryKey(supplierTable, "some_pk");
        supplierTable.setPrimaryKey(supplierPrimaryKey);

        DatabaseDiff databaseComparison =
                DiffUtil.diff(database, pippoDatabase);
        assertNotNull(databaseComparison);

        MessageDiffer diffs = new MessageDiffer();
        diffs.diffDatabase(databaseComparison);
        List<String> diff = diffs.getMessages();

        assertEquals(30, diff.size());
        assertEquals("Database names jpetstore / pippo are different", diff.get(0));
        assertEquals("Target does not contain schema: pippo.INFORMATION_SCHEMA", diff.get(1));
        assertEquals("Target does not contain table: pippo.PUBLIC.ACCOUNT", diff.get(2));
        assertEquals("Target does not contain table: pippo.PUBLIC.BANNERDATA", diff.get(3));
        assertEquals("Target does not contain table: pippo.PUBLIC.CATEGORY", diff.get(4));
        assertEquals("Target does not contain table: pippo.PUBLIC.INVENTORY", diff.get(5));
        assertEquals("Target does not contain table: pippo.PUBLIC.ITEM", diff.get(6));
        assertEquals("Target does not contain table: pippo.PUBLIC.LINEITEM", diff.get(7));
        assertEquals("Target does not contain table: pippo.PUBLIC.ORDERS", diff.get(8));
        assertEquals("Target does not contain table: pippo.PUBLIC.ORDERSTATUS", diff.get(9));

        assertEquals("Target does not contain column: pippo.PUBLIC.PRODUCT.CATEGORY", diff.get(10));
        assertEquals("Source column jpetstore.PUBLIC.PRODUCT.DESCN does not contain annotation of type: com.manydesigns.elements.annotations.Label", diff.get(11));
        assertEquals("Target does not contain column: pippo.PUBLIC.PRODUCT.NAME", diff.get(12));
        assertEquals("Target does not contain column: pippo.PUBLIC.PRODUCT.PRODUCTID", diff.get(13));
        assertEquals("Target table pippo.PUBLIC.PRODUCT does not contain primary key: PK_PRODUCT", diff.get(14));
        assertEquals("Target table pippo.PUBLIC.PRODUCT does not contain foreign key: FK_PRODUCT_1", diff.get(15));

        assertEquals("Target does not contain table: pippo.PUBLIC.PROFILE", diff.get(16));
        assertEquals("Target does not contain table: pippo.PUBLIC.SEQUENCE", diff.get(17));
        assertEquals("Target does not contain table: pippo.PUBLIC.SIGNON", diff.get(18));

        assertEquals("Target does not contain column: pippo.PUBLIC.SUPPLIER.ADDR1", diff.get(19));
        assertEquals("Target does not contain column: pippo.PUBLIC.SUPPLIER.ADDR2", diff.get(20));
        assertEquals("Target does not contain column: pippo.PUBLIC.SUPPLIER.CITY", diff.get(21));
        assertEquals("Target does not contain column: pippo.PUBLIC.SUPPLIER.NAME", diff.get(22));
        assertEquals("Target does not contain column: pippo.PUBLIC.SUPPLIER.PHONE", diff.get(23));
        assertEquals("Target does not contain column: pippo.PUBLIC.SUPPLIER.STATE", diff.get(24));
        assertEquals("Target does not contain column: pippo.PUBLIC.SUPPLIER.STATUS", diff.get(25));
        assertEquals("Target does not contain column: pippo.PUBLIC.SUPPLIER.SUPPID", diff.get(26));
        assertEquals("Target does not contain column: pippo.PUBLIC.SUPPLIER.ZIP", diff.get(27));
        assertEquals("Primary key names PK_SUPPLIER / some_pk are different", diff.get(28));
        assertEquals("Target table pippo.PUBLIC.SUPPLIER primary key some_pk does not contain column: SUPPID", diff.get(29));
    }
}
