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
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.datamodel.Column;
import com.manydesigns.portofino.model.datamodel.Database;
import com.manydesigns.portofino.model.datamodel.Schema;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.model.diff.DatabaseDiff;
import com.manydesigns.portofino.model.diff.DiffUtil;
import com.manydesigns.portofino.model.diff.MessageDiffVisitor;
import com.manydesigns.portofino.model.io.ModelParser;
import com.manydesigns.portofino.model.io.ModelWriter;

import java.io.File;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class RoundtripTest extends AbstractPortofinoTest {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testSimpleModel() throws Exception {
        Model model = new Model();

        Database mydbDatabase = new Database("mydb");
        model.getDatabases().add(mydbDatabase);

        Schema publicSchema = new Schema(mydbDatabase, "PUBLIC");
        mydbDatabase.getSchemas().add(publicSchema);

        Table productTable = new Table(publicSchema, "PRODUCT");
        publicSchema.getTables().add(productTable);

        Column descnColumn = new Column(productTable, "DESCN", "varchar",
                true, false, 255, 0, true);
        productTable.getColumns().add(descnColumn);
        doRoundtrip(model);


    }

    public void testFullModel() throws Exception {
        Model model = context.getModel();
        doRoundtrip(model);
    }

    private void doRoundtrip(Model model) throws Exception {
        // Save the model to a file
        ModelWriter modelWriter = new ModelWriter(model);
        File file = File.createTempFile("portofino-model", ".xml");
        modelWriter.write(file);

        // Parse the model from the file into model2
        ModelParser modelParser = new ModelParser();
        Model model2 = modelParser.parse(file);
        assertNotNull(model2);

        // compare model and model2
        List<Database> databases = model.getDatabases();
        List<Database> databases2 = model2.getDatabases();
        assertEquals(databases.size(), databases2.size());

        // compare each database separately
        for (int i = 0; i < databases.size(); i++) {
            DatabaseDiff diff = DiffUtil.diff(databases.get(i), databases2.get(i));
            MessageDiffVisitor visitor = new MessageDiffVisitor();
            visitor.visitDatabaseDiff(diff);
            List<String> messages = visitor.getMessages();
            if (!messages.isEmpty()) {
                for (String message : messages) {
                    System.out.println(message);
                }
                fail("Differences were found!");
            }
        }
    }
}
