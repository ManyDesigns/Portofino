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

package com.manydesigns.portofino.actions.model;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.*;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.context.ModelObjectNotFoundError;
import com.manydesigns.portofino.model.datamodel.Column;
import com.manydesigns.portofino.model.datamodel.Database;
import com.manydesigns.portofino.model.datamodel.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableDesignAction extends PortofinoAction {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Web parameters
    //**************************************************************************

    public String qualifiedTableName;
    public String cancelReturnUrl;



    //**************************************************************************
    // Web parameters setters (for struts.xml inspections in IntelliJ)
    //**************************************************************************

    public void setQualifiedTableName(String qualifiedTableName) {
        this.qualifiedTableName = qualifiedTableName;
    }

    //**************************************************************************
    // Model metadata
    //**************************************************************************

    public Table table;


    //**************************************************************************
    // Presentation elements
    //**************************************************************************

    public Form form;
    public SearchForm searchForm;
    public TableForm columnTableForm;

    //**************************************************************************
    // Other objects
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(TableDesignAction.class);


    //**************************************************************************
    // Action default execute method
    //**************************************************************************

    public String execute() {
        if (qualifiedTableName == null) {
            qualifiedTableName = model.getAllTables().get(0).getQualifiedName();
            return REDIRECT_TO_FIRST;
        }

        setupTable();

        form = new FormBuilder(Table.class)
                .configFields("databaseName", "schemaName", "tableName")
                .configMode(Mode.VIEW)
                .build();
        form.readFromObject(table);

        columnTableForm = new TableFormBuilder(Column.class)
                .configFields("columnName", "columnType")
                .configNRows(table.getColumns().size())
                .configMode(Mode.VIEW)
                .build();
        columnTableForm.readFromObject(table.getColumns());



        return SUMMARY;
    }

    //**************************************************************************
    // Common methods
    //**************************************************************************

    public void setupTable() {
        table = model.findTableByQualifiedName(qualifiedTableName);
        if (table == null) {
            throw new ModelObjectNotFoundError(qualifiedTableName);
        }
    }

    //**************************************************************************
    // Cancel
    //**************************************************************************

    public String cancel() {
        return CANCEL;
    }

    //**************************************************************************
    // Drop
    //**************************************************************************

    public String drop() {


        return "drop";
    }

    //**************************************************************************
    // Create new
    //**************************************************************************

    public String createStep1() {
        /*CreateTableStatement cts = new CreateTableStatement("pubLic", "teZt");
        cts.addColumn("a1", new VarcharType());
        CreateTableGenerator generator = new CreateTableGenerator();
        try {
            Database database =
                   CommandLineUtils.createDatabaseObject(getClass().getClassLoader(),
                            "jdbc:postgresql://127.0.0.1:5432/portofino4", "manydesigns", "manydesigns", "org.postgresql.Driver",
                            "public", "liquibase.database.core.PostgresDatabase");
            SortedSet<SqlGenerator> sqlGenerators = new TreeSet<SqlGenerator>();
            sqlGenerators.add(generator);
            Sql[] sqls =  generator.generateSql(cts, database, new SqlGeneratorChain(sqlGenerators) );
            for(Sql sql : sqls){
                System.out.println(sql.toSql());
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/



        //Available databases
        List<Database> databases = model.getDatabases();

        String [] databaseNames = new String[databases.size()];
        int i = 0;
        for (Database db : databases){
            databaseNames[i++] = db.getQualifiedName();
        }
        FormBuilder formBuilder = new FormBuilder(Table.class)
                .configFields("databaseName", "schemaName", "tableName")
                .configMode(Mode.CREATE);

        SelectionProvider selectionProvider = DefaultSelectionProvider.create("databases",
                databaseNames, databaseNames);
        formBuilder.configSelectionProvider(selectionProvider, "databaseName");
        formBuilder.configPrefix("table_");
        form = formBuilder.build();

        return CREATE;
    }


}
