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

import com.manydesigns.elements.Element;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.*;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.context.ModelObjectNotFoundError;
import com.manydesigns.portofino.model.datamodel.*;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableDesignAction extends PortofinoAction implements ServletRequestAware{
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Implementazione ServletRequestAware
    //**************************************************************************
    private HttpServletRequest req;
    public void setServletRequest(HttpServletRequest request) {
        req = request;
    }

    //**************************************************************************
    // Web parameters
    //**************************************************************************
    public String qualifiedTableName;
    public String cancelReturnUrl;
    public Table table;

    public Integer ncol;


    //**************************************************************************
    // Web parameters setters (for struts.xml inspections in IntelliJ)
    //**************************************************************************
    public void setQualifiedTableName(String qualifiedTableName) {
        this.qualifiedTableName = qualifiedTableName;
    }




    //**************************************************************************
    // Presentation elements
    //**************************************************************************
    public Element wizard;

    public Form tableForm;
    public Form columnForm;
    public Form pkForm;
    public SearchForm searchForm;
    public TableForm columnTableForm;

    //**************************************************************************
    // Other objects
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(TableDesignAction.class);


    //**************************************************************************
    // WebParameters
    //**************************************************************************

    public String table_databaseName;
    public String table_schemaName;
    public String table_tableName;

    //**************************************************************************
    // Action default execute method
    //**************************************************************************

    public String execute() {
        if (qualifiedTableName == null) {
            qualifiedTableName = model.getAllTables().get(0).getQualifiedName();
            return REDIRECT_TO_FIRST;
        }

        Table table = setupTable();

        tableForm = new FormBuilder(Table.class)
                .configFields("databaseName", "schemaName", "tableName")
                .configMode(Mode.VIEW)
                .build();
        tableForm.readFromObject(table);

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

    public Table setupTable() {
        Table table = model.findTableByQualifiedName(qualifiedTableName);
        if (table == null) {
            throw new ModelObjectNotFoundError(qualifiedTableName);
        }
        return table;
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

    public String create() {
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
        if (ncol == null){
            ncol = 0;
        }
        //Available databases
        List<Database> databases = model.getDatabases();
        String [] databaseNames = new String[databases.size()];
        int i = 0;
        for (Database db : databases){
            databaseNames[i++] = db.getQualifiedName();
        }
        setupTableForm(databaseNames);
        setupColumnForm();
        setupPkForm();

        if(table_tableName != null){
            Database database  = model.findDatabaseByName(table_databaseName);
            Schema schema = model.findSchemaByQualifiedName(
                    database.getQualifiedName()+"."+table_schemaName);
            if (schema == null) {
                schema = new Schema(database,  table_schemaName);
            }

            if(table == null) {
                table = new Table(schema, table_tableName);
            } else {
                table.setTableName(table_tableName);    
            }

            schema.getTables().add(table);
            table.init(model);
            tableForm.readFromObject(table);

            Column col = new Column(table);
            columnForm.readFromRequest(req);
            columnForm.writeToObject(col);


            columnTableForm = new TableFormBuilder(Column.class)
                .configFields("columnName", "columnType", "nullable",
                        "autoincrement", "length", "scale",
                        "searchable", "javaType", "propertyName")
                .configPrefix("cols_").configNRows(ncol)
                .configMode(Mode.CREATE_PREVIEW)
                .build();
            columnTableForm.readFromRequest(req);
            for(TableForm.Row row : columnTableForm.getRows()) {
                try {
                    Column currCol = new Column(table);
                    row.writeToObject(currCol);
                    table.getColumns().add(currCol);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            table.getColumns().add(col);
            columnTableForm = new TableFormBuilder(Column.class)
                .configFields("columnName", "columnType", "nullable",
                        "autoincrement", "length", "scale",
                        "searchable", "javaType", "propertyName").configPrefix("cols_")
                .configNRows(table.getColumns().size())
                .configMode(Mode.CREATE_PREVIEW)
                .build();
            columnTableForm.readFromObject(table.getColumns());
        }
        ncol++;
        return CREATE;
    }

    //**************************************************************************
    // Save
    //**************************************************************************
    public String step1(){
        FormBuilder formBuilder = new FormBuilder(Table.class)
                .configFields("databaseName", "schemaName", "tableName")
                .configMode(Mode.CREATE);
        formBuilder.configPrefix("table_");
        tableForm = formBuilder.build();
        tableForm.readFromRequest(req);
        if(!tableForm.validate()){
            return CREATE;
        }



        return SUMMARY;
    }


    //**************************************************************************
    // private methods
    //**************************************************************************
    private void setupTableForm(String[] databaseNames) {
        FormBuilder formBuilder = new FormBuilder(Table.class)
                .configFields("databaseName", "schemaName", "tableName")
                .configMode(Mode.CREATE);

        SelectionProvider selectionProvider = DefaultSelectionProvider.create("databases",
                databaseNames, databaseNames);
        formBuilder.configSelectionProvider(selectionProvider, "databaseName");
        formBuilder.configPrefix("table_");
        tableForm = formBuilder.build();
    }

    private void setupColumnForm() {
        FormBuilder formBuilder = new FormBuilder(Column.class)
                .configFields("columnName", "columnType", "nullable",
                        "autoincrement", "length", "scale",
                        "searchable", "javaType", "propertyName")
                .configMode(Mode.CREATE);
        formBuilder.configPrefix("column_");
        columnForm = formBuilder.build();
    }

    private void setupPkForm() {
        FormBuilder formBuilder = new FormBuilder(PrimaryKey.class)
                .configFields("primaryKeyName")
                .configMode(Mode.CREATE);
        formBuilder.configPrefix("pk_");
        pkForm = formBuilder.build();
    }
}
