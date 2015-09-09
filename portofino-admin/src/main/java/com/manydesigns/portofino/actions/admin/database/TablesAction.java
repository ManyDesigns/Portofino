/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.actions.admin.database;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.*;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.actions.admin.tables.forms.ColumnForm;
import com.manydesigns.portofino.actions.admin.tables.forms.DatabaseSelectionProviderForm;
import com.manydesigns.portofino.buttons.GuardType;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.buttons.annotations.Guard;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.logic.SelectionProviderLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.persistence.ModelObjectNotFoundError;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import groovy.json.JsonBuilder;
import net.sourceforge.stripes.action.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@RequiresAuthentication
@RequiresAdministrator
@UrlBinding(TablesAction.BASE_ACTION_PATH + "/{databaseName}/{schemaName}/{tableName}/{columnName}")
public class TablesAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final String BASE_ACTION_PATH = "/actions/admin/tables";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(DatabaseModule.PERSISTENCE)
    public Persistence persistence;

    //**************************************************************************
    // Web parameters
    //**************************************************************************
    protected String databaseName;
    protected String schemaName;
    protected String tableName;
    protected String columnName;
    protected String returnUrl;
    protected String shortName;
    protected String selectionProviderName;
    protected final Map<String, String> fkOnePropertyNames = new HashMap<String, String>();
    protected final Map<String, String> fkManyPropertyNames = new HashMap<String, String>();

    //**************************************************************************
    // Domain objects
    //**************************************************************************
    protected Table table;
    protected Column column;
    protected List<String> sortedColumnNames;
    protected List<ColumnForm> decoratedColumns;
    protected DatabaseSelectionProvider databaseSelectionProvider;

    //**************************************************************************
    // Forms
    //**************************************************************************
    protected Form tableForm;
    protected TableForm columnsTableForm;
    protected Form columnForm;

    protected Field shortNameField;

    protected Form dbSelectionProviderForm;

    //**************************************************************************
    // UI
    //**************************************************************************
    protected String selectedTabId;

    //**************************************************************************
    // Other objects
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(TablesAction.class);


    //**************************************************************************
    // Constructor
    //**************************************************************************
    public TablesAction() {}

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
        return new ForwardResolution("/m/admin/tables/list.jsp");
    }

    public Resolution getTables() {
        ArrayList<Map> treeTables = new  ArrayList<Map>();

        String lastDatabase = null;
        String lastSchema = null;
        Map schema = null;
        Map database = null;
        List<Table> tables = getAllTables();
        for(Table table : tables) {
            if(table.getPrimaryKey() == null) {
                continue;
            }
            if(!table.getDatabaseName().equals(lastDatabase)) {
                lastDatabase = table.getDatabaseName();
                database = createObject(table.getDatabaseName(),false,true,false, null, new ArrayList());
                treeTables.add(database);
                lastSchema = null;
            }
            if(!table.getSchemaName().equals(lastSchema)) {
                lastSchema = table.getSchemaName();
                schema = createObject(table.getSchemaName(), false, true, false, null, new ArrayList());

                String changelogFileNameTemplate = "{0}-changelog.xml";
                String changelogFileName = MessageFormat.format(
                        changelogFileNameTemplate, table.getDatabaseName() + "-" + table.getSchemaName());
                File changelogFile = new File(persistence.getAppDbsDir(), changelogFileName);
                String schemaDescr = table.getSchemaName();
                if(changelogFile.isFile()) {
                    schemaDescr += " <img height='15px' src='" + context.getRequest().getContextPath()+ "/m/admin/tables/liquibase_logo_small.gif' /> ";
                }

                lastSchema = table.getSchemaName();
                schema = createObject(schemaDescr,false,true,false,null, new ArrayList());
                ((List)database.get("children")).add(schema);
            }
            String tableDescr = table.getTableName();
            if(!table.getActualEntityName().equals(table.getTableName())) {
                tableDescr += " (" + table.getActualEntityName() + ")";
            }
            String href = lastDatabase +"/"+ lastSchema +"/"+ table.getTableName();
            ((List)schema.get("children")).add( createObject(tableDescr,false,false,false,href,null) );
        }

        return new StreamingResolution("text/json", new JsonBuilder(treeTables).toPrettyString());
    }

    private Map createObject(
            String title, boolean expanded, boolean folder, boolean lazy,
            @Nullable String href, @Nullable List children) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("title",title);
        map.put("expanded",expanded);
        map.put("folder",folder);
        map.put("lazy",lazy);
        map.put("href",href);
        map.put("children",children);

        return map;
    }

    public Resolution editTable() {
        setupTableForm(Mode.EDIT);
        setupColumnsForm(Mode.EDIT);
        tableForm.readFromRequest(context.getRequest());
        for(ForeignKey fk : table.getForeignKeys()) {
            fkOnePropertyNames.put(fk.getName(), fk.getOnePropertyName());
            fkManyPropertyNames.put(fk.getName(), fk.getManyPropertyName());
        }
        return new ForwardResolution("/m/admin/tables/edit-table.jsp");
    }

    public Resolution editColumn() {
        setupTableForm(Mode.HIDDEN);
        tableForm.readFromRequest(context.getRequest());

        setupColumnForm();
        return new ForwardResolution("/m/admin/tables/edit-column.jsp");
    }

    @Button(key = "save", list = "table-edit", order = 1, type = Button.TYPE_PRIMARY)
    public Resolution saveTable() {
        com.manydesigns.portofino.actions.admin.tables.forms.TableForm tf = setupTableForm(Mode.EDIT);
        setupColumnsForm(Mode.EDIT);
        tableForm.readFromRequest(context.getRequest());
        columnsTableForm.readFromRequest(context.getRequest());
        if(validateTableForm() && columnsTableForm.validate()) {
            tableForm.writeToObject(tf);
            tf.copyTo(table);
            table.setEntityName(StringUtils.defaultIfEmpty(table.getEntityName(), null));
            table.setJavaClass(StringUtils.defaultIfEmpty(table.getJavaClass(), null));
            table.setShortName(StringUtils.defaultIfEmpty(table.getShortName(), null));
            columnsTableForm.writeToObject(decoratedColumns);
            //Copy by name, not by index. Some columns may have been skipped.
            for(Column column : table.getColumns()) {
                for(ColumnForm columnForm : decoratedColumns) {
                    if(columnForm.getColumnName().equals(column.getColumnName())) {
                        columnForm.copyTo(column);
                    }
                }
            }
            Collections.sort(table.getColumns(), new Comparator<Column>() {
                public int compare(Column o1, Column o2) {
                    int i1 = sortedColumnNames.indexOf(o1.getColumnName());
                    int i2 = sortedColumnNames.indexOf(o2.getColumnName());
                    return Integer.valueOf(i1).compareTo(i2);
                }
            });

            for(ForeignKey fk : table.getForeignKeys()) {
                fk.setOnePropertyName(fkOnePropertyNames.get(fk.getName()));
                fk.setManyPropertyName(fkManyPropertyNames.get(fk.getName()));
            }

            try {
                saveModel();
                for(Table otherTable : table.getSchema().getTables()) {
                    for(ForeignKey fk : otherTable.getForeignKeys()) {
                        if(fk.getFromTable().equals(table) ||
                                (!fk.getFromTable().equals(table) && fk.getToTable().equals(table))) {
                            for(Reference ref : fk.getReferences()) {
                                Column fromColumn = ref.getActualFromColumn();
                                Column toColumn = ref.getActualToColumn();
                                if(fromColumn.getActualJavaType() != toColumn.getActualJavaType()) {
                                    SessionMessages.addWarningMessage(
                                            ElementsThreadLocals.getText(
                                                    "detected.type.mismatch.between.column._.and.column._",
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
                SessionMessages.consumeWarningMessages(); //Clear skipped columns warnings
                //setupTableForm(Mode.EDIT); //Recalculate entity name
                //setupColumnsForm(Mode.EDIT); //Reflect the new order of the columns
            } catch (Exception e) {
                logger.error("Could not save model", e);
                SessionMessages.addErrorMessage(e.toString());
            }
            return new RedirectResolution(TablesAction.class, "editTable")
                    .addParameter("databaseName", databaseName)
                    .addParameter("schemaName", schemaName)
                    .addParameter("tableName", tableName);
        } else {
            return new ForwardResolution("/m/admin/tables/edit-table.jsp");
        }
    }

    protected boolean validateTableForm() {
        if(tableForm.validate()) {
            Field javaClassField = tableForm.findFieldByPropertyName("javaClass");
            String javaClass = javaClassField.getStringValue();
            if(!StringUtils.isBlank(javaClass)) {
                try {
                    ClassLoader classLoader =
                            (ClassLoader) context.getServletContext().getAttribute(BaseModule.CLASS_LOADER);
                    Class.forName(javaClass, true, classLoader);
                } catch (ClassNotFoundException e) {
                    javaClassField.getErrors().add(ElementsThreadLocals.getText("class.not.found._"));
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Button(key = "cancel", list = "table-edit", order = 2)
    public Resolution returnToTables() {
        return new RedirectResolution(BASE_ACTION_PATH);
    }

    @Button(key = "save", list = "column-edit", order = 1, type = Button.TYPE_PRIMARY)
    public Resolution saveColumn() {
        setupTableForm(Mode.HIDDEN);
        tableForm.readFromRequest(context.getRequest());

        ColumnForm cf = setupColumnForm();
        columnForm.readFromRequest(context.getRequest());
        if(saveToColumnForm(columnForm, cf)) {
            cf.copyTo(column);
            try {
                saveModel();
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
                                        ElementsThreadLocals.getText(
                                                "detected.type.mismatch.with.column._",
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
        columnForm.readFromRequest(context.getRequest());
        saveToColumnForm(columnForm, cf);
        return new RedirectResolution(TablesAction.class, "editColumn")
                .addParameter("databaseName", databaseName)
                .addParameter("schemaName", schemaName)
                .addParameter("tableName", tableName)
                .addParameter("columnName", columnName);
    }

    @Buttons({
            @Button(key = "cancel", list = "column-edit", order = 2),
            @Button(key = "cancel", list = "table-short-name", order = 2),
            @Button(key = "cancel", list = "table-selection-provider", order = 3)
    })
    public Resolution returnToTable() {
        RedirectResolution resolution =
                new RedirectResolution(BASE_ACTION_PATH + "/" + databaseName + "/" + schemaName + "/" + tableName);
        resolution.addParameter("selectedTabId", selectedTabId);
        return resolution;
    }

    @Button(key = "layouts.admin.tables.addSelectionProvider", list="table-selection-providers" , icon = Button.ICON_PLUS , type = Button.TYPE_INFO )
    public Resolution addSelectionProvider() {
        table = findTable();
        databaseSelectionProvider = new DatabaseSelectionProvider(table);
        DatabaseSelectionProviderForm databaseSelectionProviderForm = setupDbSelectionProviderForm(Mode.CREATE);
        return doEditSelectionProvider(databaseSelectionProviderForm);
    }

    @Button(key = "delete", list="table-selection-provider", order = 2)
    @Guard(test = "getSelectionProviderName() != null", type = GuardType.VISIBLE)
    public Resolution removeSelectionProvider() {
        table = findTable();
        ModelSelectionProvider sp = DatabaseLogic.findSelectionProviderByName(table, selectionProviderName);
        table.getSelectionProviders().remove(sp);
        try {
            saveModel();
        } catch (Exception e) {
            logger.error("Could not save model", e);
            SessionMessages.addErrorMessage(e.toString());
        }
        return editTable();
    }

    public Resolution editSelectionProvider() {
        table = findTable();
        databaseSelectionProvider =
                (DatabaseSelectionProvider) DatabaseLogic.findSelectionProviderByName(
                        table, selectionProviderName);
        DatabaseSelectionProviderForm databaseSelectionProviderForm = setupDbSelectionProviderForm(Mode.CREATE);
        return doEditSelectionProvider(databaseSelectionProviderForm);
    }

    protected Resolution doEditSelectionProvider(DatabaseSelectionProviderForm databaseSelectionProviderForm) {
        setupTableForm(Mode.HIDDEN);
        tableForm.readFromRequest(context.getRequest());
        return new ForwardResolution("/m/admin/tables/edit-db-selection-provider.jsp");
    }

    protected DatabaseSelectionProviderForm setupDbSelectionProviderForm(Mode mode) {
        SelectionProvider databaseChooser =
                SelectionProviderLogic.createSelectionProvider(
                        "database",
                        persistence.getModel().getDatabases(),
                        Database.class,
                        null,
                        new String[]{"databaseName"});
        dbSelectionProviderForm = new FormBuilder(DatabaseSelectionProviderForm.class)
                .configFields("name", "toDatabase", "hql", "sql", "columns")
                .configSelectionProvider(databaseChooser, "toDatabase")
                .configMode(mode)
                .build();
        DatabaseSelectionProviderForm databaseSelectionProviderForm =
                new DatabaseSelectionProviderForm(databaseSelectionProvider);
        List<String> refCols = new ArrayList<String>();
        for(Reference ref : databaseSelectionProvider.getReferences()) {
            refCols.add(ref.getFromColumn());
        }
        databaseSelectionProviderForm.setColumns(StringUtils.join(refCols, ", "));
        dbSelectionProviderForm.readFromObject(databaseSelectionProviderForm);
        return databaseSelectionProviderForm;
    }

    @Button(key = "save", list = "table-selection-provider", order = 1, type = Button.TYPE_PRIMARY)
    public Resolution saveSelectionProvider() {
        table = findTable();
        Mode mode = selectionProviderName == null ? Mode.CREATE : Mode.EDIT;
        if(selectionProviderName == null) {
            databaseSelectionProvider = new DatabaseSelectionProvider(table);
        } else {
            databaseSelectionProvider = (DatabaseSelectionProvider) DatabaseLogic.findSelectionProviderByName(
                    table, selectionProviderName);
        }
        DatabaseSelectionProviderForm databaseSelectionProviderForm = setupDbSelectionProviderForm(mode);
        dbSelectionProviderForm.readFromRequest(context.getRequest());
        if(dbSelectionProviderForm.validate()) {
            dbSelectionProviderForm.writeToObject(databaseSelectionProviderForm);
            if((!StringUtils.isEmpty(databaseSelectionProviderForm.getSql()) &&
                    !StringUtils.isEmpty(databaseSelectionProviderForm.getHql())) ||
                    (StringUtils.isEmpty(databaseSelectionProviderForm.getSql()) &&
                            StringUtils.isEmpty(databaseSelectionProviderForm.getHql()))) {
                SessionMessages.addErrorMessage(
                        ElementsThreadLocals.getText("please.fill.exactly.one.of.the.fields.hql.sql"));
                return doEditSelectionProvider(databaseSelectionProviderForm);
            }

            String[] refCols = StringUtils.split(databaseSelectionProviderForm.getColumns(), ",");
            List<Column> columns = new ArrayList<Column>();
            for(String c : refCols) {
                Column col = DatabaseLogic.findColumnByName(table, c.trim());
                if(col == null) {
                    SessionMessages.addErrorMessage(
                            ElementsThreadLocals.getText("column._.not.found", c));
                    return doEditSelectionProvider(databaseSelectionProviderForm);
                } else {
                    columns.add(col);
                }
            }

            if(!StringUtils.equals(databaseSelectionProviderForm.getName(), selectionProviderName)) {
                logger.debug("Selection provider name changed or is new, checking for duplicates");
                if(DatabaseLogic.findSelectionProviderByName(
                        table, databaseSelectionProviderForm.getName()) != null) {
                    String message = ElementsThreadLocals.getText(
                            "selection.provider._.already.exists",
                            databaseSelectionProviderForm.getName());
                    SessionMessages.addErrorMessage(message);
                    return doEditSelectionProvider(databaseSelectionProviderForm);
                }
                if(selectionProviderName == null) {
                    logger.debug("Selection provider is new, adding");
                    table.getSelectionProviders().add(databaseSelectionProvider);
                }
            }
            databaseSelectionProviderForm.copyTo(databaseSelectionProvider);
            databaseSelectionProvider.getReferences().clear();
            for(Column col : columns) {
                Reference ref = new Reference(databaseSelectionProvider);
                ref.setFromColumn(col.getColumnName());
                databaseSelectionProvider.getReferences().add(ref);
            }
            try {
                saveModel();
            } catch (Exception e) {
                logger.error("Could not save model", e);
                SessionMessages.addErrorMessage(e.toString());
            }
            selectedTabId = "tab-fk-sp";
            return editTable();
        } else {
            return doEditSelectionProvider(databaseSelectionProviderForm);
        }
    }

    protected void saveModel() throws IOException, JAXBException {
        persistence.initModel();
        persistence.saveXmlModel();
        SessionMessages.addInfoMessage(ElementsThreadLocals.getText("model.saved.successfully"));
    }

    protected boolean saveToColumnForm(Form columnForm, ColumnForm cf) {
        if(columnForm.validate()) {
            columnForm.writeToObject(cf);
            if(!StringUtils.isEmpty(cf.getDateFormat())) {
                try {
                    new SimpleDateFormat(cf.getDateFormat());
                } catch (Exception e) {
                    String message = ElementsThreadLocals.getText("invalid.date.format.string");
                    columnForm.findFieldByPropertyName("dateFormat").getErrors().add(message);
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    protected com.manydesigns.portofino.actions.admin.tables.forms.TableForm setupTableForm(Mode mode) {
        table = findTable();
        tableForm = new FormBuilder(com.manydesigns.portofino.actions.admin.tables.forms.TableForm.class)
                .configFields("entityName", "javaClass", "shortName", "hqlQuery")
                .configMode(mode)
                .build();
        com.manydesigns.portofino.actions.admin.tables.forms.TableForm tf =
                new com.manydesigns.portofino.actions.admin.tables.forms.TableForm(table);
        tableForm.readFromObject(tf);
        return tf;
    }

    protected void setupColumnsForm(Mode mode) {
        Type[] types = persistence.getConnectionProvider(table.getDatabaseName()).getTypes();
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
                        ElementsThreadLocals.getText(
                                "skipped.column._.with.unknown.type._",
                                column.getColumnName(),
                                column.getColumnType(),
                                column.getJdbcType()));
            }
        }

        DefaultSelectionProvider typesSP = new DefaultSelectionProvider("columnType", 3);
        for(ColumnForm columnForm : decoratedColumns) {
            configureTypesSelectionProvider(typesSP, columnForm);
        }

        columnsTableForm = new TableFormBuilder(ColumnForm.class)
                .configFields("columnName", "propertyName", "javaType", "type", "shortLength", "scale", "reallyNullable")
                .configSelectionProvider(typesSP, "columnName", "type", "javaType")
                .configNRows(decoratedColumns.size())
                .configMode(mode)
                .build();
        columnsTableForm.setSelectable(false);
        columnsTableForm.setCondensed(true);
        for(int i = 0; i < decoratedColumns.size(); i++) {
            TableForm.Row row = columnsTableForm.getRows()[i];
            Column column = decoratedColumns.get(i);

            Field columnNameField = row.findFieldByPropertyName("columnName");
            columnNameField.setHref(
                    context.getRequest().getContextPath() +
                            getActionPath() +
                            "/" + column.getColumnName());
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

        Type[] types = persistence.getConnectionProvider(table.getDatabaseName()).getTypes();
        ColumnForm cf = decorateColumn(column, columnAccessor, types);
        DefaultSelectionProvider typesSP = new DefaultSelectionProvider("columnType", 3);
        configureTypesSelectionProvider(typesSP, cf);

        DefaultSelectionProvider stringFormatSP = new DefaultSelectionProvider("stringFormat");
        stringFormatSP.appendRow(Email.class.getName(), "Email", true);
        stringFormatSP.appendRow(Password.class.getName(), "Password", true);
        stringFormatSP.appendRow(CAP.class.getName(), "CAP", true);
        stringFormatSP.appendRow(PartitaIva.class.getName(), "Partita IVA", true);
        stringFormatSP.appendRow(CodiceFiscale.class.getName(), "Codice Fiscale", true);
        stringFormatSP.appendRow(Phone.class.getName(), "Phone", true);

        DefaultSelectionProvider typeOfContentSP = new DefaultSelectionProvider("typeOfContent");
        typeOfContentSP.appendRow(Multiline.class.getName(), "Multiline", true);
        typeOfContentSP.appendRow(RichText.class.getName(), "RichText", true);

        columnForm = new FormBuilder(ColumnForm.class)
                .configFieldSetNames("Properties", "Annotations")
                .configFields(new String[] { "columnName", "propertyName", "javaType", "type",
                        "length", "scale", "reallyNullable", "reallyAutoincrement", "inPk" },
                        getApplicableAnnotations(column.getActualJavaType()))
                .configSelectionProvider(typesSP, "columnName", "type", "javaType")
                .configSelectionProvider(stringFormatSP, "stringFormat")
                .configSelectionProvider(typeOfContentSP, "typeOfContent")
                .build();

        SelectField typeOfContentField = (SelectField) columnForm.findFieldByPropertyName("typeOfContent");
        if(typeOfContentField != null) {
            typeOfContentField.setComboLabel("Plain"); //TODO I18n
        }

        columnForm.readFromObject(cf);
        return cf;
    }

    protected void configureTypesSelectionProvider(DefaultSelectionProvider typesSP, ColumnForm columnForm) {
        Type type = columnForm.getType();
        Class[] javaTypes = getAvailableJavaTypes(type, columnForm.getLength());
        Integer precision = columnForm.getLength();
        Integer scale = columnForm.getScale();
        Class defaultJavaType = Type.getDefaultJavaType(columnForm.getJdbcType(), precision, scale);
        if(defaultJavaType == null) {
            defaultJavaType = Object.class;
        }
        typesSP.appendRow(
                new Object[] { columnForm.getColumnName(), type, null },
                new String[] {
                        columnForm.getColumnName(),
                        type.getTypeName() + " (JDBC: " + type.getJdbcType() + ")",
                        "Auto (" + getJavaTypeName(defaultJavaType) + ")" },
                true);
        try {
            Class existingType = Class.forName(columnForm.getJavaType());
            if(!ArrayUtils.contains(javaTypes, existingType)) {
                typesSP.appendRow(
                        new Object[] { columnForm.getColumnName(), type, null },
                        new String[] {
                                columnForm.getColumnName(),
                                type.getTypeName() + " (JDBC: " + type.getJdbcType() + ")",
                                getJavaTypeName(existingType)},
                        true);
            }
        } catch (Exception e) {
            logger.debug("Invalid Java type", e);
        }
        for (Class c : javaTypes) {
            typesSP.appendRow(
                    new Object[] { columnForm.getColumnName(), type, c.getName() },
                    new String[] {
                            columnForm.getColumnName(),
                            type.getTypeName() + " (JDBC: " + type.getJdbcType() + ")",
                            getJavaTypeName(c)},
                    true);
        }
    }

    protected String getJavaTypeName(Class javaType) {
        if(javaType.getPackage().getName().startsWith("java.")) {
            return javaType.getSimpleName();
        } else {
            return javaType.getName();
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

    protected Class[] getAvailableJavaTypes(Type type, Integer length) {
        if(type.isNumeric()) {
            return new Class[] {
                    Integer.class, Long.class, Byte.class, Short.class,
                    Float.class, Double.class, BigInteger.class, BigDecimal.class,
                    Boolean.class };
        } else if(type.getDefaultJavaType() == String.class) {
            if(length != null && length < 256) {
                return new Class[] { String.class, Boolean.class };
            } else {
                return new Class[] { String.class };
            }
        } else if(type.getDefaultJavaType() == Timestamp.class) {
            return new Class[] { Timestamp.class, java.sql.Date.class, DateTime.class };
        } else if(type.getDefaultJavaType() == java.sql.Date.class) {
            return new Class[] { java.sql.Date.class, Timestamp.class, DateTime.class };
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
            return new String[] { "fieldSize", "minValue", "maxValue", "decimalFormat" };
        } else if(String.class.equals(type)) {
            return new String[] { "fieldSize", "typeOfContent", "stringFormat", "regexp",
                    "highlightLinks", "fileBlob" };
        } else if(Date.class.isAssignableFrom(type)) {
            return new String[] { "fieldSize", "dateFormat" };
        }
        return new String[0];
    }

    //**************************************************************************
    // Common methods
    //**************************************************************************

    public Table findTable() {
        Table table = DatabaseLogic.findTableByName(
                persistence.getModel(), databaseName, schemaName, tableName);
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

    @Button(list = "tables-list", key = "return.to.pages", order = 3  , icon = Button.ICON_HOME)
    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

    public String getBaseActionPath() {
        return BASE_ACTION_PATH;
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

    public List<Table> getAllTables() {
        List<Table> tables = DatabaseLogic.getAllTables(persistence.getModel());
        Collections.sort(tables, new Comparator<Table>() {
            public int compare(Table o1, Table o2) {
                int dbComp = o1.getDatabaseName().compareToIgnoreCase(o2.getDatabaseName());
                if(dbComp == 0) {
                    int schemaComp = o1.getSchemaName().compareToIgnoreCase(o2.getSchemaName());
                    if(schemaComp == 0) {
                        return o1.getTableName().compareToIgnoreCase(o2.getTableName());
                    } else {
                        return schemaComp;
                    }
                } else {
                    return dbComp;
                }
            }
        });
        return tables;
    }

    public Model getModel() {
        return persistence.getModel();
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

    @FieldSize(75)
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Field getShortNameField() {
        return shortNameField;
    }

    public String getSelectedTabId() {
        return selectedTabId;
    }

    public void setSelectedTabId(String selectedTabId) {
        this.selectedTabId = selectedTabId;
    }

    public DatabaseSelectionProvider getDatabaseSelectionProvider() {
        return databaseSelectionProvider;
    }

    public Form getDbSelectionProviderForm() {
        return dbSelectionProviderForm;
    }

    public String getSelectionProviderName() {
        return selectionProviderName;
    }

    public void setSelectionProviderName(String selectionProviderName) {
        this.selectionProviderName = selectionProviderName;
    }

    public Persistence getPersistence() {
        return persistence;
    }

    public List<String> getSortedColumnNames() {
        return sortedColumnNames;
    }

    public void setSortedColumnNames(List<String> sortedColumnNames) {
        this.sortedColumnNames = sortedColumnNames;
    }

    public List<ColumnForm> getDecoratedColumns() {
        return decoratedColumns;
    }

    public Map<String, String> getFkOnePropertyNames() {
        return fkOnePropertyNames;
    }

    public Map<String, String> getFkManyPropertyNames() {
        return fkManyPropertyNames;
    }
}