/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.actions.admin;

import com.manydesigns.elements.annotations.AnnotationsManager;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.TextField;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.actions.model.AnnModel;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.application.ModelObjectNotFoundError;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.security.RequiresAdministrator;
import net.sourceforge.stripes.action.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@RequiresAdministrator
@UrlBinding(TablesAction.BASE_ACTION_PATH + "{databaseName}/{schemaName}/{tableName}/{columnName}")
public class TablesAction extends AbstractActionBean implements AdminAction {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public static final String BASE_ACTION_PATH = "/actions/admin/tables/";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(RequestAttributes.MODEL)
    public Model model;

    @Inject(RequestAttributes.APPLICATION)
    public Application application;

    //**************************************************************************
    // Web parameters
    //**************************************************************************
    public InputStream inputStream;

    protected String databaseName;
    protected String schemaName;
    protected String tableName;
    protected String columnName;

    protected String cancelReturnUrl;
    protected Table table;
    protected Column column;

    protected List<ColumnForm> decoratedColumns;

    public final List<String> annotations;
    public final List<String> annotationsImpl;
    public List<AnnModel> colAnnotations;


    public List<String> columnNames;

    //Selection provider
    protected String relName;

    //testo parziale per autocomplete
    public String term;

    //Step
    public Integer step;

    //**************************************************************************
    // Forms
    //**************************************************************************
    protected Form tableForm;
    protected TableForm columnsTableForm;
    protected Form columnForm;

    //**************************************************************************
    // Other objects
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(TablesAction.class);


    //**************************************************************************
    // WebParameters
    //**************************************************************************

    public String table_databaseName;

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
        if (tableName == null) {
            return search();
        } else if(columnName == null) {
            return editTable();
        } else {
            return editColumn();
        }
    }

    public Resolution search() {
        return new ForwardResolution("/layouts/admin/tables/list.jsp");
    }

    public Resolution editTable() {
        setupTableForm();
        setupColumnsForm();
        return new ForwardResolution("/layouts/admin/tables/edit-table.jsp");
    }

    public Resolution editColumn() {
        setupColumnForm();
        return new ForwardResolution("/layouts/admin/tables/edit-column.jsp");
    }

    @Button(key = "commons.save", list = "table-edit")
    public Resolution saveTable() {
        setupTableForm();
        setupColumnsForm();
        tableForm.readFromRequest(context.getRequest());
        columnsTableForm.readFromRequest(context.getRequest());
        if(tableForm.validate() && columnsTableForm.validate()) {
            tableForm.writeToObject(table);
            table.setEntityName(StringUtils.defaultIfEmpty(table.getEntityName(), null));
            table.setJavaClass(StringUtils.defaultIfEmpty(table.getJavaClass(), null));
            columnsTableForm.writeToObject(decoratedColumns);
            for(Column column : table.getColumns()) {
                for(ColumnForm columnForm : decoratedColumns) {
                    if(columnForm.getColumnName().equals(column.getColumnName())) {
                        columnForm.copyTo(column);
                    }
                }
            }
            try {
                model.init();
                application.saveXmlModel();
                DispatcherLogic.clearConfigurationCache();
                for(Table otherTable : table.getSchema().getTables()) {
                    for(ForeignKey fk : otherTable.getForeignKeys()) {
                        if(fk.getFromTable().equals(table) ||
                           (!fk.getFromTable().equals(table) && fk.getToTable().equals(table))) {
                            for(Reference ref : fk.getReferences()) {
                                Column fromColumn = ref.getActualFromColumn();
                                Column toColumn = ref.getActualToColumn();
                                if(fromColumn.getActualJavaType() != toColumn.getActualJavaType()) {
                                    SessionMessages.addWarningMessage(
                                            getMessage(
                                                    "layouts.admin.tables.typeMismatchInTable",
                                                    fromColumn.getQualifiedName(),
                                                    fromColumn.getActualJavaType().getName(),
                                                    toColumn.getQualifiedName(),
                                                    toColumn.getActualJavaType().getName(),
                                                    fk.getName()));
                                }
                            }
                        }
                    }
                }
                SessionMessages.addInfoMessage(getMessage("commons.save.successful"));
            } catch (Exception e) {
                logger.error("Could not save model", e);
                SessionMessages.addErrorMessage(e.toString());
            }
        }
        return new ForwardResolution("/layouts/admin/tables/edit-table.jsp");
    }

    @Button(key = "commons.save", list = "column-edit")
    public Resolution saveColumn() {
        ColumnForm cf = setupColumnForm();
        columnForm.readFromRequest(context.getRequest());
        if(columnForm.validate()) {
            columnForm.writeToObject(cf);
            cf.copyTo(column);
             try {
                model.init();
                application.saveXmlModel();
                DispatcherLogic.clearConfigurationCache();
                 for(Table otherTable : table.getSchema().getTables()) {
                    for(ForeignKey fk : otherTable.getForeignKeys()) {
                        for(Reference ref : fk.getReferences()) {
                            Column fromColumn = ref.getActualFromColumn();
                            Column toColumn = ref.getActualToColumn();
                            if((fromColumn.equals(column) || toColumn.equals(column)) &&
                                fromColumn.getActualJavaType() != toColumn.getActualJavaType()) {
                                Column otherColumn;
                                if(fromColumn.equals(column)) {
                                    otherColumn = toColumn;
                                } else {
                                    otherColumn = fromColumn;
                                }
                                SessionMessages.addWarningMessage(
                                        getMessage(
                                                "layouts.admin.tables.typeMismatchInColumn",
                                                otherColumn.getQualifiedName(),
                                                otherColumn.getActualJavaType().getName(),
                                                fk.getName()));
                            }
                        }
                    }
                }
             } catch (Exception e) {
                logger.error("Could not save model", e);
                SessionMessages.addErrorMessage(e.toString());
            }
        }
        setupColumnForm(); //Recalculate applicable annotations
        return new ForwardResolution("/layouts/admin/tables/edit-column.jsp");
    }

    protected void setupTableForm() {
        table = findTable();
        tableForm = new FormBuilder(Table.class)
                .configFields("entityName", "javaClass", "shortName")
                .build();
        Field shortNameField = tableForm.findFieldByPropertyName("shortName");
        shortNameField.setInsertable(false);
        shortNameField.setUpdatable(false);

        Field entityNameField = tableForm.findFieldByPropertyName("entityName");
        entityNameField.setHelp("If you leave this empty, the entity name will be generated automatically from the table name.");

        tableForm.readFromObject(table);

        ((TextField) shortNameField).setStringValue(
                StringUtils.defaultString(shortNameField.getStringValue(), "not set") +  " (Edit)");
        shortNameField.setHref(table.getTableName() + "?editShortName=");
    }

    protected void setupColumnsForm() {
        Type[] types = application.getConnectionProvider(table.getDatabaseName()).getTypes();
        decoratedColumns = new ArrayList<ColumnForm>(table.getColumns().size());
        TableAccessor tableAccessor = new TableAccessor(table);

        for(Column column : table.getColumns()) {
            PropertyAccessor columnAccessor;
            try {
                columnAccessor = tableAccessor.getProperty(column.getActualPropertyName());
            } catch (NoSuchFieldException e) {
                throw new Error(e);
            }
            ColumnForm cf = decorateColumn(column, columnAccessor, types);
            if(cf != null) {
                //Add to form
                decoratedColumns.add(cf);
            } else {
                SessionMessages.addWarningMessage(
                        "Skipped column " + column.getColumnName() + " with unknown type " + column.getColumnType() +
                        " (JDBC: " + column.getJdbcType() + ")");
            }
        }

        DefaultSelectionProvider typesSP = new DefaultSelectionProvider("columnType", 3);
        for(ColumnForm columnForm : decoratedColumns) {
            configureTypesSelectionProvider(typesSP, columnForm);
        }

        columnsTableForm = new TableFormBuilder(ColumnForm.class)
                .configFields("columnName", "propertyName", "javaType", "type", "length", "scale", "nullable")
                .configSelectionProvider(typesSP, "columnName", "type", "javaType")
                .configNRows(decoratedColumns.size())
                .build();
        columnsTableForm.setSelectable(false);
        columnsTableForm.setCaption("Columns");
        for(int i = 0; i < decoratedColumns.size(); i++) {
            TableForm.Row row = columnsTableForm.getRows()[i];
            Column column = decoratedColumns.get(i);

            Field columnNameField = row.findFieldByPropertyName("columnName");
            columnNameField.setHref(
                    context.getRequest().getContextPath() +
                    getActionPath() +
                    "/" + column.getColumnName());

            Field nullableField = row.findFieldByPropertyName("nullable");
            //Work around Introspector bug
            nullableField.setInsertable(false);
            nullableField.setUpdatable(false);
        }
        columnsTableForm.readFromObject(decoratedColumns);
    }

    protected ColumnForm setupColumnForm() {
        table = findTable();
        column = findColumn();

        TableAccessor tableAccessor = new TableAccessor(table);
        PropertyAccessor columnAccessor;
        try {
            columnAccessor = tableAccessor.getProperty(column.getActualPropertyName());
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }

        Type[] types = application.getConnectionProvider(table.getDatabaseName()).getTypes();
        ColumnForm cf = decorateColumn(column, columnAccessor, types);
        DefaultSelectionProvider typesSP = new DefaultSelectionProvider("columnType", 3);
        configureTypesSelectionProvider(typesSP, cf);
        columnForm = new FormBuilder(ColumnForm.class)
                .configFieldSetNames("Properties", "Annotations")
                .configFields(new String[] { "columnName", "propertyName", "javaType", "type",
                                             "length", "scale", "nullable", "inPk" },
                              getApplicableAnnotations(column.getActualJavaType()))
                .configSelectionProvider(typesSP, "columnName", "type", "javaType")
                .build();
        Field nullableField = columnForm.findFieldByPropertyName("nullable");
        nullableField.setInsertable(false);
        nullableField.setUpdatable(false);
        columnForm.readFromObject(cf);
        return cf;
    }

    protected void configureTypesSelectionProvider(DefaultSelectionProvider typesSP, ColumnForm columnForm) {
        Type type = columnForm.getType();
        Class[] javaTypes = getAvailableJavaTypes(type);
        long precision = columnForm.getLength() != null ? columnForm.getLength() : type.getMaximumPrecision();
        int scale = columnForm.getScale() != null ? columnForm.getScale() : type.getMaximumScale();
        Class defaultJavaType = Type.getDefaultJavaType(columnForm.getJdbcType(), precision, scale);
        if(defaultJavaType == null) {
            defaultJavaType = Object.class;
        }
        typesSP.appendRow(
                new Object[] { columnForm.getColumnName(), type, null },
                new String[] {
                        columnForm.getColumnName(),
                        type.getTypeName() + " (JDBC: " + type.getJdbcType() + ")",
                        "Auto (" + defaultJavaType.getSimpleName() + ")" },
                true);
        for (Class c : javaTypes) {
            typesSP.appendRow(
                    new Object[] { columnForm.getColumnName(), type, c.getName() },
                    new String[] {
                            columnForm.getColumnName(),
                            type.getTypeName() + " (JDBC: " + type.getJdbcType() + ")",
                            c.getSimpleName() },
                    true);
        }
    }

    protected ColumnForm decorateColumn(Column column, PropertyAccessor columnAccessor, Type[] types) {
        //Select the best matching type
        Type type = null;
        for (Type candidate : types) {
            if (candidate.getJdbcType() == column.getJdbcType() &&
                candidate.getTypeName().equalsIgnoreCase(column.getColumnType())) {
                type = candidate;
                break;
            }
        }
        if(type == null) {
            for (Type candidate : types) {
                if (candidate.getJdbcType() == column.getJdbcType()) {
                    type = candidate;
                    break;
                }
            }
        }
        ColumnForm cf = null;
        if(type != null) {
            cf = new ColumnForm(column, columnAccessor, type);
        }
        return cf;
    }

    protected Class[] getAvailableJavaTypes(Type type) {
        if(type.isNumeric()) {
            return new Class[] {
                    Integer.class, Long.class, Byte.class, Short.class,
                    Float.class, Double.class, BigInteger.class, BigDecimal.class,
                    Boolean.class };
        } else if(type.getDefaultJavaType() == String.class) {
            return new Class[] { String.class, Boolean.class };
        } else {
            Class defaultJavaType = type.getDefaultJavaType();
            if(defaultJavaType != null) {
                return new Class[] { defaultJavaType };
            } else {
                return new Class[] { Object.class };
            }
        }
    }

    protected String[] getApplicableAnnotations(Class type) {
        if(Number.class.isAssignableFrom(type)) {
            return new String[] { "fieldSize", "minValue", "maxValue" };
        } else if(String.class.equals(type)) {
            return new String[] { "fieldSize", "multiline", "email", "cap", "highlightLinks", "regexp" };
        } else if(Date.class.isAssignableFrom(type)) {
            return new String[] { "fieldSize", "dateFormat" };
        }
        return new String[0];
    }

    protected String getMessage(String key, Object... args) {
        Locale locale = context.getLocale();
        ResourceBundle resourceBundle = application.getBundle(locale);
        String msg = resourceBundle.getString(key);
        return MessageFormat.format(msg, args);
    }

    /*public Resolution jsonSelectFieldOptions() {
        if("columnType".equals(relName)) {
            setupTableForm();
            setupColumnsForm();
            HttpServletRequest request = context.getRequest();
            columnsTableForm.readFromRequest(request);
            for(TableForm.Row row : columnsTableForm.getRows()) {
                for(Field field : row) {
                    if(field.getPropertyAccessor().getName().equals("javaType") &&
                       request.getParameter(field.getInputName()) != null) {
                        field.readFromRequest(request);
                        String text = ((SelectField) field).jsonSelectFieldOptions(false);
                        return new NoCacheStreamingResolution(MimeTypes.APPLICATION_JSON_UTF8, text);
                    }
                }
            }
        }
        return null;
    }*/

    /*
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
    }*/

    //**************************************************************************
    // Common methods
    //**************************************************************************

    public Table findTable() {
        Table table = DatabaseLogic.findTableByName(
                model, databaseName, schemaName, tableName);
        if (table == null) {
            throw new ModelObjectNotFoundError(databaseName + "." + schemaName + "." + tableName);
        }
        return table;
    }

    public Column findColumn() {
        Column column = DatabaseLogic.findColumnByName(table, columnName);
        if(column == null) {
            throw new ModelObjectNotFoundError(table.getQualifiedName() + "." + column);
        }
        return column;
    }

    @Button(list = "tables-list", key = "commons.returnToPages", order = 3)
    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

    //**************************************************************************
    // Cancel
    //**************************************************************************

    public Resolution cancel() {
        return null; //TODO
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

    /*@Button(list = "tables-list", key = "commons.create", order = 1)
    public String create() throws CloneNotSupportedException {
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
                            DatabaseLogic.findColumnByName(
                                    table, currCol.getColumnName()));
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
    }*/

    //**************************************************************************
    // Preparazione dei form
    //**************************************************************************

    /*private void setupForms() {
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

    }*/

    //**************************************************************************
    // Inizializzazione dei form a partire dalla request
    //**************************************************************************
    /*private boolean readFromRequest() {
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
    }*/





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
    /*public String jsonColumns() throws Exception {
        return createJsonArray(columnNames);
    }*/

    //**************************************************************************
    // Json output per i corretti types per una piattaforma
    //**************************************************************************
    /*public String jsonTypes() throws Exception {
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
    }*/

    //**************************************************************************
    // Json output per i corretti Java types per una piattaforma
    //**************************************************************************
    /*public String jsonJavaTypes() throws Exception {
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
    }*/
    
    //**************************************************************************
    // Json output per vedere se richiesta Precision, Scale, ...
    //**************************************************************************
    /*public String jsonTypeInfo() throws Exception {
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
    }*/

    //**************************************************************************
    // Json output per lista Annotations
    //**************************************************************************
    public String jsonAnnotation() throws Exception {
        return createJsonArray(annotations);
    }

    public String getActionPath() {
        String path = BASE_ACTION_PATH;
        if(tableName != null) {
            path += "/" + databaseName + "/" + schemaName + "/" + tableName;
            if(columnName != null) {
                path += "/" + columnName;
            }
        }
        return path;
    }

    public Model getModel() {
        return model;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Table getTable() {
        return table;
    }

    public Column getColumn() {
        return column;
    }

    public Form getTableForm() {
        return tableForm;
    }

    public TableForm getColumnsTableForm() {
        return columnsTableForm;
    }

    public Form getColumnForm() {
        return columnForm;
    }

    public void setRelName(String relName) {
        this.relName = relName;
    }
}

