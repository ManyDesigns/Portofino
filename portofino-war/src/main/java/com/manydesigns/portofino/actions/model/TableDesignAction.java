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
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.context.ModelObjectNotFoundError;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.model.datamodel.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
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
    public InputStream inputStream;

    public String qualifiedTableName;
    public String cancelReturnUrl;
    public Table table;
    public Integer ncol;
    public Integer npkcol;
    public String pk_primaryKeyName;
    public String pk_column;

    public List<String> columnNames = new ArrayList<String>();
    //colonne da rimuovere
    public String[] cols_selection;
    public String[] pkCols_selection;
    //testo parziale per autocomplite
    public String term;

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
    public TableForm pkColumnTableForm;



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
    // Add new Column
    //**************************************************************************

    public String create() throws CloneNotSupportedException {
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

        if (npkcol == null){
            npkcol = 0;
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
            Database database  =
                new Database(model.findDatabaseByName(table_databaseName)
                        .getDatabaseName());
            Schema schema = new Schema(database, table_schemaName);

            table = new Table(schema, table_tableName);
            schema.getTables().add(table);
            tableForm.readFromObject(table);

            columnTableForm = new TableFormBuilder(Column.class)
                .configFields("columnName", "columnType", "nullable",
                        "autoincrement", "length", "scale",
                        "searchable", "javaType", "propertyName")
                .configPrefix("cols_").configNRows(ncol)
                .configMode(Mode.CREATE_PREVIEW)
                .build();
            columnTableForm.setSelectable(true);
            columnTableForm.setKeyGenerator(OgnlTextFormat.create("%{columnName}"));
            columnTableForm.readFromRequest(req);

            if (table.getPrimaryKey()!=null){
                npkcol =table.getPrimaryKey().size();
            }
            pkColumnTableForm = new TableFormBuilder(PrimaryKeyColumn.class)
                .configFields("columnName").configPrefix("pkCols_")
            .configNRows(npkcol)
            .configMode(Mode.CREATE_PREVIEW)
            .build();
            pkColumnTableForm.readFromObject(req);
            pkColumnTableForm.setSelectable(true);
            columnTableForm.setKeyGenerator(OgnlTextFormat.create("%{columnName}"));           

            for(TableForm.Row row : columnTableForm.getRows()) {
                try {
                    Column currCol = new Column(table);
                    row.writeToObject(currCol);
                    table.getColumns().add(currCol);
                    columnNames.add(currCol.getColumnName());
                } catch (Throwable e) {
                    //Do nothing
                }
            }
            String operation = req.getParameter("method:create");
            if ("Add column".equals(operation)) {
                addCol();
            } else if ("Remove column" .equals(operation)){
                removeCol();
            } else if ("Add primary key column".equals(operation)) {
                addPkCol();
            } else if ("Remove primary key column".equals(operation)) {

            }
            columnTableForm.setSelectable(true);
            columnTableForm.setKeyGenerator(OgnlTextFormat.create("%{columnName}"));
            columnTableForm.readFromObject(table.getColumns());
        }

        return CREATE;
    }

    private void removeCol() {
        columnNames.clear();
        for(TableForm.Row row : columnTableForm.getRows()) {
            try {
                Column currCol = new Column(table);
                row.writeToObject(currCol);
                if (ArrayUtils.contains(cols_selection, currCol.getColumnName())){
                    table.getColumns().remove(
                            table.findColumnByName(
                                    currCol.getColumnName()));
                } else {
                    columnNames.add(currCol.getColumnName());
                }
            } catch (Throwable e) {
                //do nothing: accetto errori quali assenza di pk sulla tabella
            }
        }

        columnTableForm = new TableFormBuilder(Column.class)
            .configFields("columnName", "columnType", "nullable",
                    "autoincrement", "length", "scale",
                    "searchable", "javaType", "propertyName").configPrefix("cols_")
            .configNRows(table.getColumns().size())
            .configMode(Mode.CREATE_PREVIEW)
            .build();
        columnTableForm.setSelectable(true);
        columnTableForm.setKeyGenerator(OgnlTextFormat.create("%{columnName}"));
        columnTableForm.readFromObject(table.getColumns());
        ncol = table.getColumns().size();
    }

    private void addPkCol() {
        PrimaryKey pk = new PrimaryKey(table);
        table.setPrimaryKey(pk);
        pk.setPrimaryKeyName(pk_primaryKeyName!=null?pk_primaryKeyName:"pk_"+table_tableName);

        for(Column currentColumn : table.getColumns()){
            if(currentColumn.getColumnName().equals(pk_column)){
                pk.add(new PrimaryKeyColumn(pk, pk_column));
            }
        }
        pkColumnTableForm = new TableFormBuilder(PrimaryKeyColumn.class)
                .configFields("columnName").configPrefix("pkCols_")
            .configNRows(table.getPrimaryKey().size())
            .configMode(Mode.CREATE_PREVIEW)
            .build();
        pkColumnTableForm.readFromObject(pk);
        pkColumnTableForm.setSelectable(true);
        columnTableForm.setKeyGenerator(OgnlTextFormat.create("%{columnName}"));
        npkcol++;
    }

    private void addCol() {
        Column col = new Column(table);
        columnForm.readFromRequest(req);
        columnForm.writeToObject(col);
        List<Column> columns = table.getColumns();
        boolean found = false;
        columnNames.clear();
        for (Column currentColumn : columns){
            String name = currentColumn.getColumnName();
            if (name.equals(col.getColumnName())){
                found = true;
            }
        }
        if (!found){
            columns.add(col);
            columnNames.add(col.getColumnName());
        }
        columnTableForm = new TableFormBuilder(Column.class)
            .configFields("columnName", "columnType", "nullable",
                    "autoincrement", "length", "scale",
                    "searchable", "javaType", "propertyName").configPrefix("cols_")
            .configNRows(table.getColumns().size())
            .configMode(Mode.CREATE_PREVIEW)
            .build();
        ncol++;
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
    // Json output per i corretti types per una piattaforma
    //**************************************************************************
    public String jsonTypes() throws Exception {
        Type[] types = context.getConnectionProvider(table_databaseName).getTypes();
        List<String> typesString = new ArrayList<String>();

        for(Type currentType : types){
            if(null!=term && !"".equals(term)) {
                if (StringUtils.startsWithIgnoreCase(currentType.getTypeName(),term))
                   typesString.add("\""+currentType.getTypeName()+"\"");
            } else {
                typesString.add("\""+currentType.getTypeName()+"\"");
            }
        }
        String result = "["+ StringUtils.join(typesString, ",")+"]";
        inputStream = new ByteArrayInputStream(result.getBytes());
        return "json";
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
