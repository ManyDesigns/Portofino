/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.actions.admin;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.AnnotationsManager;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertiesAccessor;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.actions.model.AnnModel;
import com.manydesigns.portofino.actions.model.PrimaryKeyColumnModel;
import com.manydesigns.portofino.actions.model.PrimaryKeyModel;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.application.ModelObjectNotFoundError;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.security.RequiresAdministrator;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.ActionResolver;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@RequiresAdministrator
@UrlBinding("/actions/admin/tables")
public class TablesAction extends AbstractActionBean implements AdminAction {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(RequestAttributes.MODEL)
    public Model model;

    @Inject(RequestAttributes.APPLICATION)
    public Application application;

    //**************************************************************************
    // STEP del wizard
    //**************************************************************************
    private static final int ANNOTATION_STEP = 4;
    private static final int COLUMN_STEP = 2;
    private static final int TABLE_STEP = 1;
    private static final int PRIMARYKEY_STEP = 3;

    //**************************************************************************
    // Ex PortofinoAction
    //**************************************************************************
    public final static String CREATE = "create";
    public final static String CANCEL = "cancel";

    //**************************************************************************
    // Web parameters
    //**************************************************************************
    public InputStream inputStream;

    public String qualifiedTableName;
    public String cancelReturnUrl;
    public Table table;
    public final List<String> annotations;
    public final List<String> annotationsImpl;
    public List<AnnModel> colAnnotations;
    public PrimaryKeyModel pkModel;
    public String pk_primaryKeyName;
    public String pk_column;
    public String colAnn_annotationName;

    public List<String> columnNames;

    //Contatori righe TableForm
    public Integer ncol;
    public Integer npkcol;
    public Integer nAnnotations;

    //righe da rimuovere
    public String[] cols_selection;
    public String[] pkCols_selection;
    public String[] colAnnT_selection;

    //testo parziale per autocomplete
    public String term;

    //Step
    public Integer step;


    //**************************************************************************
    // Web parameters setters (for struts.xml inspections in IntelliJ)
    //**************************************************************************
    public void setQualifiedTableName(String qualifiedTableName) {
        this.qualifiedTableName = qualifiedTableName;
    }


    //**************************************************************************
    // Forms
    //**************************************************************************
    public TableForm multilineForm;
    public Form tableForm;
    public Form columnForm;
    public Form pkForm;
    public Form pkColumnForm;
    //public Form colAnnotationForm;
    public Form annForm;
    public Form annPropForm;

    public TableForm columnTableForm;
    public TableForm pkColumnTableForm;
    public TableForm colAnnotationTableForm;


    //**************************************************************************
    // Other objects
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(TablesAction.class);


    //**************************************************************************
    // WebParameters
    //**************************************************************************

    public String table_databaseName;
    public String table_schemaName;
    public String table_tableName;




    //**************************************************************************
    // Constructor
    //**************************************************************************
    public TablesAction() {
        annotations = new ArrayList<String>();
        annotationsImpl = new ArrayList<String>();
        Set<Class> annotationsClasses
                =  AnnotationsManager.getManager().getManagedAnnotationClasses();

        for (Class aClass: annotationsClasses){
            Target target;
            target = (Target) aClass.getAnnotation(Target.class);
            if (null!= target && ArrayUtils.contains(target.value(),
                    ElementType.FIELD)){
                annotations.add(aClass.getName());
                annotationsImpl.add(AnnotationsManager.getManager()
                        .getAnnotationImplementationClass(aClass).getName());
            }
        }
        colAnnotations = new ArrayList<AnnModel>();
        columnNames = new ArrayList<String>();
    }

    //**************************************************************************
    // Action default execute method
    //**************************************************************************

    @DefaultHandler
    public Resolution execute() {
        if (qualifiedTableName == null) {
            return search();
        } else {
            return read();
        }
    }

    public Resolution search() {
        List<Table> tableList = DatabaseLogic.getAllTables(model);
        multilineForm = new TableFormBuilder(Table.class)
                .configFields("databaseName", "schemaName", "tableName")
                .configNRows(tableList.size())
                .configMode(Mode.VIEW)
                .build();
        multilineForm.readFromObject(tableList);
        multilineForm.setSelectable(true);
        return new ForwardResolution("/layouts/admin/tables/list.jsp");
    }

    public Resolution read() {
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
        return new ForwardResolution("/layouts/admin/tables/read.jsp");
    }

    //**************************************************************************
    // Common methods
    //**************************************************************************

    public Table setupTable() {
        Table table = DatabaseLogic.findTableByQualifiedName(
                model, qualifiedTableName);
        if (table == null) {
            throw new ModelObjectNotFoundError(qualifiedTableName);
        }
        return table;
    }

    @Button(list = "tables-list", key = "commons.returnToPages", order = 3)
    public Resolution returnToPages() {
        return new RedirectResolution("/");
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

    @Button(list = "tables-list", key = "commons.delete", order = 2)
    public String bulkDelete() {
        return "drop";
    }

    //**************************************************************************
    // Add new Column
    //**************************************************************************

    @Button(list = "tables-list", key = "commons.create", order = 1)
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
        setupForms();
        step= TABLE_STEP;
        return CREATE;
    }



    public String addCol() {
        step = COLUMN_STEP;
        setupForms();
        readFromRequest();
        Column col = new Column();
        col.setTable(table);
        columnForm.readFromRequest(context.getRequest());
        if(!columnForm.validate()){
            return CREATE;
        }      
       
        columnForm.writeToObject(col);
        List<Column> columns = table.getColumns();
        boolean found = false;
        for (Column currentColumn : columns){
            String name = currentColumn.getColumnName();
            if (name.equals(col.getColumnName())){
                found = true;
            }
        }       
        if (!found){
            columns.add(col);
            columnNames.add(col.getColumnName());
        } else {
            SessionMessages.addInfoMessage("Column exists");
        }
        columnTableForm = new TableFormBuilder(Column.class)
            .configFields("columnName", "columnType", "nullable",
                    "autoincrement", "length", "scale",
                    "searchable", "javaType", "propertyName")
                .configPrefix("cols_")
                .configNRows(table.getColumns().size())
                .configMode(Mode.CREATE_PREVIEW)
                .build();
        ncol++;
        columnTableForm.setSelectable(true);
        columnTableForm.setKeyGenerator(OgnlTextFormat.create("%{columnName}"));
        columnTableForm.readFromObject(table.getColumns());

        return CREATE;
}

    public String remCol() {
        step= COLUMN_STEP;
        setupForms();
        if(!readFromRequest()){
            return CREATE;
        }
        columnNames.clear();
        for(TableForm.Row row : columnTableForm.getRows()) {
            try {
                Column currCol = new Column();
                currCol.setTable(table);
                row.writeToObject(currCol);
                if (ArrayUtils.contains(cols_selection, currCol.getColumnName())){
                    table.getColumns().remove(
                            table.findColumnByName(
                                    currCol.getColumnName()));
                } else {
                    columnNames.add(currCol.getColumnName());
                }
            } catch (Throwable e) {
                logger.info(e.getMessage());
            }
        }

        columnTableForm = new TableFormBuilder(Column.class)
            .configFields("columnName", "columnType", "nullable",
                    "autoincrement", "length", "scale",
                    "searchable", "javaType", "propertyName")
                .configPrefix("cols_")
                .configNRows(table.getColumns().size())
                .configMode(Mode.CREATE_PREVIEW)
                .build();
        columnTableForm.setSelectable(true);
        columnTableForm.setKeyGenerator(OgnlTextFormat.create("%{columnName}"));
        columnTableForm.readFromObject(table.getColumns());
        ncol = table.getColumns().size();
        columnTableForm.setSelectable(true);
        columnTableForm.setKeyGenerator(OgnlTextFormat.create("%{columnName}"));
        columnTableForm.readFromObject(table.getColumns());

        return CREATE;
    }


    public String addColAnnotation() {
        step= ANNOTATION_STEP;
        setupForms();
        readFromRequest();
        
        AnnModel annotation = new AnnModel();
        Properties properties = new Properties();

        annForm.writeToObject(annotation);
        annPropForm.writeToObject(properties);
        annotation.properties=properties;

        colAnnotations.add(annotation);

        colAnnotationTableForm = new TableFormBuilder(AnnModel.class)
            .configFields("columnName", "annotationName", "propValues")
                .configPrefix("colAnnT_")
            .configNRows(colAnnotations.size())
            .configMode(Mode.CREATE_PREVIEW)
            .build();
        nAnnotations++;
        colAnnotationTableForm.setSelectable(true);
        colAnnotationTableForm.setKeyGenerator(
                OgnlTextFormat.create("%{columnName+\"_\"+annotationName}"));
        colAnnotationTableForm.readFromObject(colAnnotations);

        return CREATE;
    }

    public String setAnnParameters() throws ClassNotFoundException, NoSuchFieldException {
        step= ANNOTATION_STEP;
        setupForms();
        readFromRequest(); 
        if (colAnn_annotationName==null){
            SessionMessages.addErrorMessage("SELECT A ANNOTATION");
 
        }
        return CREATE;
    }

    public String remColAnnotation() {
        step= ANNOTATION_STEP;
        setupForms();
        if(!readFromRequest()){
            return CREATE;
        }

        for(TableForm.Row row : colAnnotationTableForm.getRows()) {
            try {
                AnnModel annotation = new AnnModel();
                row.writeToObject(annotation);
                if (ArrayUtils.contains(colAnnT_selection,
                        annotation.columnName+"_"+
                        annotation.annotationName)){
                        colAnnotations.remove(annotation);
                }
            } catch (Throwable e) {
                //do nothing: accetto errori quali assenza di pk sulla tabella
                logger.info(e.getMessage());
            }
        }

        colAnnotationTableForm = new TableFormBuilder(AnnModel.class)
            .configFields("columnName", "annotationName", "propValues")
                .configPrefix("colAnnT_")
            .configNRows(colAnnotations.size())
            .configMode(Mode.CREATE_PREVIEW)
            .build();
        colAnnotationTableForm.setSelectable(true);
        colAnnotationTableForm.setKeyGenerator(OgnlTextFormat
                .create("%{columnName+\"_\"+annotationName}"));
        colAnnotationTableForm.readFromObject(colAnnotations);
        nAnnotations=colAnnotations.size();

        return CREATE;
    }

    public String addPkCol() {
        step= PRIMARYKEY_STEP;
        setupForms();
        if(!readFromRequest()){
            return CREATE;
        }

        PrimaryKeyColumnModel colModel = new PrimaryKeyColumnModel();

        pkColumnForm.writeToObject(colModel);
        pkModel.add(colModel);
        npkcol++;

        pkColumnTableForm = new TableFormBuilder(PrimaryKeyColumnModel.class)
                .configFields("column", "genType", "seqName",
                        "tabName", "colName", "colValue").configPrefix("pkCols_")
            .configNRows(pkModel.size())
            .configMode(Mode.CREATE_PREVIEW)
            .build();

        pkColumnTableForm.setSelectable(true);
        pkColumnTableForm.setKeyGenerator(OgnlTextFormat.create("%{column}"));
        pkColumnTableForm.readFromObject(pkModel);
        return CREATE;
    }

    public String remPkCol() {
        step= PRIMARYKEY_STEP;
        setupForms();
        if(!readFromRequest()){
            return CREATE;
        }
        for(TableForm.Row row : pkColumnTableForm.getRows()) {
            try {
                PrimaryKeyColumnModel currCol = new PrimaryKeyColumnModel();
                row.writeToObject(currCol);
                if (ArrayUtils.contains(pkCols_selection, currCol.column)){
                    pkModel.remove(currCol);
                    npkcol--;
                } 
            } catch (Throwable e) {
                // do nothing: accetto errori quali assenza di pk sulla tabella
                // la classe mi serve solo come modello dei dati
                logger.info(e.getMessage());
            }
        }
        pkColumnTableForm = new TableFormBuilder(PrimaryKeyColumnModel.class)
                .configFields("column", "genType", "seqName",
                        "tabName", "colName", "colValue").configPrefix("pkCols_")
            .configNRows(pkModel.size())
            .configMode(Mode.CREATE_PREVIEW)
            .build();

        pkColumnTableForm.setSelectable(true);
        pkColumnTableForm.setKeyGenerator(OgnlTextFormat.create("%{column}"));
        pkColumnTableForm.readFromObject(pkModel);

        return CREATE;
    }

    //**************************************************************************
    // private methods
    //**************************************************************************
    //**************************************************************************
    // Preparazione dei form
    //**************************************************************************
    private void setupForms() {
        if (ncol == null){
            ncol = 0;
        }
        if (npkcol == null){
            npkcol = 0;
        }
        if (nAnnotations == null){
            nAnnotations = 0;
        }
        Mode mode = Mode.CREATE;

        //Available databases
        List<Database> databases = model.getDatabases();
        String [] databaseNames = new String[databases.size()];
        int i = 0;
        for (Database db : databases){
            databaseNames[i++] = db.getQualifiedName();
        }
        //Costruisco form per Table

        FormBuilder formBuilder = new FormBuilder(Table.class)
                .configFields("databaseName", "schemaName", "tableName")
                .configMode(mode);

        DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider("databases");
        for(i = 0; i < databaseNames.length; i++) {
            selectionProvider.appendRow(databaseNames[i], databaseNames[i], true);
        }
        formBuilder.configSelectionProvider(selectionProvider, "databaseName");
        formBuilder.configPrefix("table_");
        tableForm = formBuilder.build();

        //Costruisco form per Column
        formBuilder = new FormBuilder(Column.class)
                .configFields("columnName", "columnType", "nullable",
                        "autoincrement", "length", "scale",
                        "searchable", "javaType", "propertyName")
                .configMode(mode);
        formBuilder.configPrefix("column_");
        columnForm = formBuilder.build();

        //Costruisco form per Primary Key
        formBuilder = new FormBuilder(PrimaryKey.class)
                .configFields("primaryKeyName")
                .configMode(mode);
        formBuilder.configPrefix("pk_");
        pkForm = formBuilder.build();
        pkColumnForm = new FormBuilder(PrimaryKeyColumnModel.class)
                .configFields("column", "genType", "seqName",
                        "tabName", "colName", "colValue").configPrefix("pk_")
                .configMode(mode).build();

        //Costruisco form per Annotations
        formBuilder = new FormBuilder(AnnModel.class)
                .configFields("columnName", "annotationName").configPrefix("colAnn_")
                .configMode(mode);
        DefaultSelectionProvider selectionProviderAnns = new DefaultSelectionProvider("annotations");
        for(i = 0; i < annotationsImpl.size(); i++) {
            selectionProviderAnns.appendRow(annotationsImpl.get(i), annotations.get(i), true);
        }
        formBuilder.configSelectionProvider(selectionProviderAnns, "annotationName");
        annForm = formBuilder.build();

        if (colAnn_annotationName!=null && colAnn_annotationName.length()>0){
            try {
                Class annotationClass =
                        this.getClass().getClassLoader()
                        .loadClass(colAnn_annotationName);
                Properties properties = new Properties();
                Field[] fields = annotationClass.getDeclaredFields();
                for (Field field : fields){
                    if (!Modifier.isStatic(field.getModifiers())){
                        properties.put(field.getName(), "");
                    }
                }
                ClassAccessor propertiesAccessor = new PropertiesAccessor(properties);
                FormBuilder builder = new FormBuilder(propertiesAccessor);
                annPropForm = builder.configMode(Mode.CREATE).build();
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage());
                SessionMessages.addErrorMessage(e.getMessage());
            }
        }

    }

    //**************************************************************************
    // Inizializzazione dei form a partire dalla request
    //**************************************************************************
    private boolean readFromRequest() {
        if(null==table_databaseName){
            return false;
        }

        tableForm.readFromRequest(context.getRequest());

        if(!tableForm.validate()){
            return false;
        }
        //Gestione tabella
        Database database = new Database();
        database.setDatabaseName(
                DatabaseLogic.findDatabaseByName(model, table_databaseName)
                    .getDatabaseName());
        Schema schema = new Schema();
        schema.setDatabase(database);
        schema.setSchemaName(table_schemaName);
        table = new Table();
        table.setSchema(schema);
        table.setTableName(table_tableName);

        schema.getTables().add(table);
        tableForm.readFromObject(table);

        if(!tableForm.validate()){
            return false;
        }

        HttpServletRequest req = context.getRequest();
        //Gestione colonne
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
        for(TableForm.Row row : columnTableForm.getRows()) {
            try {
                Column currCol = new Column();
                currCol.setTable(table);
                row.writeToObject(currCol);
                table.getColumns().add(currCol);
                columnNames.add(currCol.getColumnName());
            } catch (Throwable e) {
                //Do nothing
            }
        }

        //Gestione Chiave primaria
        pkModel = new PrimaryKeyModel();
        pkColumnTableForm = new TableFormBuilder(PrimaryKeyColumnModel.class)
                .configFields("column", "genType", "seqName",
                        "tabName", "colName", "colValue").configPrefix("pkCols_")
            .configNRows(npkcol)
            .configMode(Mode.CREATE_PREVIEW)
            .build();

        pkColumnTableForm.setSelectable(true);
        pkColumnTableForm.setKeyGenerator(OgnlTextFormat.create("%{column}"));
        pkColumnTableForm.readFromRequest(req);
        pkForm.readFromRequest(req);
        pkModel.primaryKeyName = pk_primaryKeyName!=null?
            pk_primaryKeyName:"pk_"+table_tableName;
        pkColumnForm.readFromRequest(req);
        for(TableForm.Row row : pkColumnTableForm.getRows()) {
            try {
                PrimaryKeyColumnModel currCol = new PrimaryKeyColumnModel();
                row.writeToObject(currCol);
                pkModel.add(currCol);
            } catch (Throwable e) {
                //Do nothing
                logger.error(e.getMessage());
            }
        }

        //Gestione annotations
        colAnnotationTableForm = new TableFormBuilder(AnnModel.class)
            .configFields("columnName", "annotationName", "propValues")
                .configPrefix("colAnnT_")
            .configNRows(nAnnotations)
            .configMode(Mode.CREATE_PREVIEW)
            .build();

        colAnnotationTableForm.setSelectable(true);
        colAnnotationTableForm.setKeyGenerator(
                OgnlTextFormat.create("%{columnName+\"_\"+annotationName}"));
        colAnnotationTableForm.readFromRequest(req);
        for(TableForm.Row row : colAnnotationTableForm.getRows()) {
            try {
                AnnModel currAnnotation = new AnnModel();
                row.writeToObject(currAnnotation);
                colAnnotations.add(currAnnotation);
            } catch (Throwable e) {
                logger.error(e.getMessage());
            }
        }
        
        //Proprieta' delle annotation
        annForm.readFromRequest(req);
        annPropForm.readFromRequest(req);

        return true;
    }





    private String createJsonArray (List<String> collection) {
        List<String> resulList = new ArrayList<String>();

        for(String string : collection){

                resulList.add("\""+string+"\"");
        }
        String result = "["+ StringUtils.join(resulList, ",")+"]";
        inputStream = new ByteArrayInputStream(result.getBytes());
        return "json";

    }

    //**************************************************************************
    // Json output per lista Colonne
    //**************************************************************************
    public String jsonColumns() throws Exception {
        return createJsonArray(columnNames);
    }

    //**************************************************************************
    // Json output per i corretti types per una piattaforma
    //**************************************************************************
    public String jsonTypes() throws Exception {
        Type[] types = application.getConnectionProvider(table_databaseName).getTypes();
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
    // Json output per i corretti Java types per una piattaforma
    //**************************************************************************
    public String jsonJavaTypes() throws Exception {
        Type[] types = application.getConnectionProvider(table_databaseName).getTypes();
        List<String> javaTypesString = new ArrayList<String>();

        for(Type currentType : types){
            if(StringUtils.equalsIgnoreCase(currentType.getTypeName(),
                    context.getRequest().getParameter("column_columnType"))){
                String defJavaType;
                try{
                    defJavaType= currentType.getDefaultJavaType().getName();
                } catch (Throwable e){
                    defJavaType="UNSOPPORTED";
                }
                javaTypesString.add("\""+defJavaType+"\"");
            }
        }
        String result = "["+ StringUtils.join(javaTypesString, ",")+"]";
        inputStream = new ByteArrayInputStream(result.getBytes());
        return "json";
    }
    
    //**************************************************************************
    // Json output per vedere se richiesta Precision, Scale, ...
    //**************************************************************************
    public String jsonTypeInfo() throws Exception {
        Type[] types = application.getConnectionProvider(table_databaseName).getTypes();
        List<String> info = new ArrayList<String>();

        for(Type currentType : types){
            if(StringUtils.equalsIgnoreCase(currentType.getTypeName(),
                    context.getRequest().getParameter("column_columnType"))){

                info.add("\"precision\" : \""+
                        (currentType.isPrecisionRequired()?"true":"false")+"\"");
                info.add("\"scale\" : \""+
                        (currentType.isScaleRequired()?"true":"false")+"\"");
                info.add("\"searchable\" : \""+
                        (currentType.isSearchable()?"true":"false")+"\"");
                info.add("\"autoincrement\" : \""+
                        (currentType.isAutoincrement()?"true":"false")+"\"");
            }
        }
        String result = "{"+ StringUtils.join(info, ",")+"}";
        inputStream = new ByteArrayInputStream(result.getBytes());
        return "json";
    }

    //**************************************************************************
    // Json output per lista Annotations
    //**************************************************************************
    public String jsonAnnotation() throws Exception {
        return createJsonArray(annotations);
    }

    public String getActionPath() {
        return (String) getContext().getRequest().getAttribute(ActionResolver.RESOLVED_ACTION);
    }
}

