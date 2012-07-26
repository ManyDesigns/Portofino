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

package com.manydesigns.portofino.actions.admin.appwizard;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.LabelI18N;
import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.elements.annotations.Password;
import com.manydesigns.elements.fields.BooleanField;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.fields.TextField;
import com.manydesigns.elements.forms.*;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.actions.admin.ConnectionProvidersAction;
import com.manydesigns.portofino.actions.forms.ConnectionProviderForm;
import com.manydesigns.portofino.actions.forms.SelectableSchema;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.pageactions.calendar.configuration.CalendarConfiguration;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudConfiguration;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudProperty;
import com.manydesigns.portofino.pageactions.wizard.AbstractWizardPageAction;
import com.manydesigns.portofino.pages.ChildPage;
import com.manydesigns.portofino.pages.Group;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.pages.Permissions;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.sync.DatabaseSyncer;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.ActionResolver;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.Date;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAdministrator
public class ApplicationWizard extends AbstractWizardPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";
    public static final String JDBC = "JDBC";
    public static final String JNDI = "JNDI";
    @SuppressWarnings({"RedundantStringConstructorCall"})
    public static final String NO_LINK_TO_PARENT = new String();
    public static final int LARGE_RESULT_SET_THRESHOLD = 10000;

    protected int step = 0;

    protected Form jndiCPForm;
    protected Form jdbcCPForm;
    protected Form connectionProviderForm;

    protected Form userAndGroupTablesForm;
    protected Form userManagementSetupForm;

    protected String connectionProviderType;
    protected ConnectionProvider connectionProvider;
    protected boolean advanced;
    protected Form advancedOptionsForm;

    public TableForm schemasForm;
    protected List<SelectableSchema> selectableSchemas;
    public TableForm rootsForm;
    protected List<SelectableRoot> selectableRoots = new ArrayList<SelectableRoot>();

    //Users
    protected String userTableName;
    protected String groupTableName;
    protected String userGroupTableName;

    protected String userNameProperty;
    protected String userIdProperty;
    protected String userPasswordProperty;
    protected String encryptionAlgorithm;
    protected String groupIdProperty;
    protected String groupNameProperty;
    protected String groupLinkProperty;
    protected String userLinkProperty;
    protected String adminGroupName;

    protected List<Table> roots;
    protected MultiMap children;
    protected List<Table> allTables;
    protected Table userTable;
    protected Table groupTable;
    protected Table userGroupTable;
    protected int maxColumnsInSummary = 5;
    protected int maxDepth = 5;
    protected int depth;

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(RequestAttributes.APPLICATION)
    public Application application;

    public static final Logger logger = LoggerFactory.getLogger(ApplicationWizard.class);

    @DefaultHandler
    @Button(list = "select-schemas", key="wizard.prev", order = 1)
    public Resolution start() {
        buildCPForms();
        return createConnectionProviderForm();
    }

    protected Resolution createConnectionProviderForm() {
        step = 0;
        return new ForwardResolution("/layouts/admin/appwizard/create-connection-provider.jsp");
    }

    @Before
    public void prepare() {
        ClassAccessor selfAccessor = JavaClassAccessor.getClassAccessor(ApplicationWizard.class);
        PropertyAccessor propertyAccessor;
        try {
            propertyAccessor = selfAccessor.getProperty("advanced");
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
        Field advancedOptionsField = new BooleanField(propertyAccessor, Mode.EDIT, "advanced_");
        advancedOptionsForm = new Form(Mode.EDIT);
        FieldSet fieldSet = new FieldSet(null, 1, Mode.EDIT);
        fieldSet.add(advancedOptionsField);
        advancedOptionsForm.add(fieldSet);
        advancedOptionsForm.readFromObject(this);
        advancedOptionsForm.readFromRequest(context.getRequest());
        advancedOptionsForm.writeToObject(this);
    }

    protected void buildCPForms() {
        jndiCPForm = new FormBuilder(ConnectionProviderForm.class)
                            .configFields(ConnectionProvidersAction.jndiEditFields)
                            .configPrefix("jndi")
                            .configMode(Mode.CREATE)
                            .build();
        jdbcCPForm = new FormBuilder(ConnectionProviderForm.class)
                            .configFields(ConnectionProvidersAction.jdbcEditFields)
                            .configPrefix("jdbc")
                            .configMode(Mode.CREATE)
                            .build();

        //Handle back
        jndiCPForm.readFromRequest(context.getRequest());
        jdbcCPForm.readFromRequest(context.getRequest());
    }

    @Buttons({
        @Button(list = "create-connection-provider", key="wizard.next", order = 2),
        @Button(list = "select-tables", key="wizard.prev", order = 1)
    })
    public Resolution createConnectionProvider() {
        buildCPForms();
        if(JDBC.equals(connectionProviderType)) {
            connectionProvider = new JdbcConnectionProvider();
            connectionProviderForm = jdbcCPForm;
        } else if(JNDI.equals(connectionProviderType)) {
            connectionProvider = new JndiConnectionProvider();
            connectionProviderForm = jndiCPForm;
        } else {
            throw new Error("Unknown connection provider type: " + connectionProviderType);
        }
        Database database = new Database();
        database.setConnectionProvider(connectionProvider);
        connectionProvider.setDatabase(database);
        ConnectionProviderForm edit = new ConnectionProviderForm(database);
        connectionProviderForm.readFromRequest(context.getRequest());
        if(connectionProviderForm.validate()) {
            connectionProviderForm.writeToObject(edit);
            return afterCreateConnectionProvider();
        } else {
            return createConnectionProviderForm();
        }
    }

    public Resolution afterCreateConnectionProvider() {
        try {
            configureEditSchemas();
        } catch (Exception e) {
            logger.error("Couldn't read schema names from db", e);
            SessionMessages.addErrorMessage(getMessage("appwizard.error.schemas", e));
            return createConnectionProviderForm();
        }
        return selectSchemasForm();
    }

    protected Resolution selectSchemasForm() {
        step = 1;
        return new ForwardResolution("/layouts/admin/appwizard/select-schemas.jsp");
    }

    protected void configureEditSchemas() throws Exception {
        connectionProvider.init(application.getDatabasePlatformsManager(), application.getAppDir());
        Connection conn = connectionProvider.acquireConnection();
        logger.debug("Reading database metadata");
        DatabaseMetaData metadata = conn.getMetaData();
        List<String> schemaNamesFromDb =
                connectionProvider.getDatabasePlatform().getSchemaNames(metadata);
        connectionProvider.releaseConnection(conn);

        List<Schema> selectedSchemas = connectionProvider.getDatabase().getSchemas();

        selectableSchemas = new ArrayList<SelectableSchema>(schemaNamesFromDb.size());
        for(String schemaName : schemaNamesFromDb) {
            if (DatabaseSyncer.INFORMATION_SCHEMA.equalsIgnoreCase(schemaName)) {
                logger.info("Skipping information schema: {}", schemaName);
                continue;
            }
            boolean selected = false;
            for(Schema schema : selectedSchemas) {
                if(schemaName.equalsIgnoreCase(schema.getSchemaName())) {
                    selected = true;
                    break;
                }
            }
            SelectableSchema schema = new SelectableSchema(schemaName, selected);
            selectableSchemas.add(schema);
        }
        schemasForm = new TableFormBuilder(SelectableSchema.class)
                .configFields(
                        "selected", "schemaName"
                )
                .configMode(Mode.EDIT)
                .configNRows(selectableSchemas.size())
                .configPrefix("schemas_")
                .build();
        schemasForm.readFromObject(selectableSchemas);
        //Handle back
        schemasForm.readFromRequest(context.getRequest());
    }

    @Buttons({
        @Button(list = "select-schemas", key="wizard.next", order = 2),
        @Button(list = "select-user-fields", key="wizard.prev", order = 1)
    })
    public Resolution selectSchemas() {
        createConnectionProvider();
        schemasForm.readFromRequest(context.getRequest());
        if(schemasForm.validate()) {
            schemasForm.writeToObject(selectableSchemas);
            boolean atLeastOneSelected = false;
            for(SelectableSchema schema : selectableSchemas) {
                if(schema.selected) {
                    atLeastOneSelected = true;
                    break;
                }
            }
            if(atLeastOneSelected) {
                if (!addSchemasToModel()) {
                    return selectSchemasForm();
                }
                return afterSelectSchemas();
            } else {
                SessionMessages.addErrorMessage(getMessage("appwizard.error.schemas.noneSelected"));
                return selectSchemasForm();
            }
        }
        return selectSchemasForm();
    }

    protected boolean addSchemasToModel() {
        Database database = connectionProvider.getDatabase();
        for(SelectableSchema schema : selectableSchemas) {
            if(schema.selected) {
                Schema modelSchema = new Schema();
                modelSchema.setSchemaName(schema.schemaName);
                modelSchema.setDatabase(database);
                database.getSchemas().add(modelSchema);
            }
        }
        Database targetDatabase;
        DatabaseSyncer dbSyncer = new DatabaseSyncer(connectionProvider);
        try {
            targetDatabase = dbSyncer.syncDatabase(new Model());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            SessionMessages.addErrorMessage(getMessage("appwizard.error.sync", e));
            return false;
        }
        connectionProvider.setDatabase(targetDatabase);
        connectionProvider.init(application.getDatabasePlatformsManager(), application.getAppDir());
        Model model = new Model();
        model.getDatabases().add(targetDatabase);
        model.init();
        return true;
    }

    public Resolution afterSelectSchemas() {
        children = new MultiHashMap();
        allTables = new ArrayList<Table>();
        roots = determineRoots(children, allTables);
        Collections.sort(allTables, new Comparator<Table>() {
            public int compare(Table o1, Table o2) {
                return o1.getQualifiedName().compareToIgnoreCase(o2.getQualifiedName());
            }
        });
        rootsForm = new TableFormBuilder(SelectableRoot.class)
                .configFields(
                        "selected", "tableName"
                )
                .configMode(Mode.EDIT)
                .configNRows(selectableRoots.size())
                .configPrefix("roots_")
                .build();
        rootsForm.readFromObject(selectableRoots);

        try {
            ClassAccessor classAccessor = JavaClassAccessor.getClassAccessor(getClass());
            PropertyAccessor userPropertyAccessor = classAccessor.getProperty("userTableName");
            PropertyAccessor groupPropertyAccessor = classAccessor.getProperty("groupTableName");
            PropertyAccessor userGroupPropertyAccessor = classAccessor.getProperty("userGroupTableName");

            DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider("tableName");

            int schemaCount = 0;
            for(SelectableSchema schema : selectableSchemas) {
                if(schema.selected) {
                    schemaCount++;
                }
            }
            for(Table table : allTables) {
                String tableName;
                if(schemaCount > 1) {
                    tableName = table.getSchemaName() + "." + table.getTableName();
                } else {
                    tableName = table.getTableName();
                }
                selectionProvider.appendRow(
                        table.getQualifiedName(),
                        tableName,
                        true);
            }
            Mode mode = Mode.CREATE;

            Field userTableField = new SelectField(userPropertyAccessor, selectionProvider, mode, "");
            Field groupTableField = new SelectField(groupPropertyAccessor, selectionProvider, mode, "");
            Field userGroupTableField = new SelectField(userGroupPropertyAccessor, selectionProvider, mode, "");

            userAndGroupTablesForm = new Form(mode);
            FieldSet fieldSet = new FieldSet(getMessage("appwizard.userAndGroupTables"), 1, mode);
            fieldSet.add(userTableField);
            fieldSet.add(groupTableField);
            fieldSet.add(userGroupTableField);
            userAndGroupTablesForm.add(fieldSet);
            //Handle back
            userAndGroupTablesForm.readFromRequest(context.getRequest());
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }

        return selectTablesForm();
    }

    protected Resolution selectTablesForm() {
        step = 2;
        return new ForwardResolution("/layouts/admin/appwizard/select-tables.jsp");
    }

    protected List<Table> determineRoots(MultiMap children, List<Table> allTables) {
        List<Table> roots = new ArrayList<Table>();
        for(SelectableSchema selectableSchema : selectableSchemas) {
            if(selectableSchema.selected) {
                Schema schema = DatabaseLogic.findSchemaByName(
                        connectionProvider.getDatabase(), selectableSchema.schemaName);
                roots.addAll(schema.getTables());
            }
        }
        for(Iterator<Table> it = roots.iterator(); it.hasNext();) {
            Table table = it.next();

            if(table.getPrimaryKey() == null) {
                it.remove();
                continue;
            }

            allTables.add(table);

            boolean removed = false;
            boolean selected = false; //Così che selected => known
            boolean known = false;

            for(SelectableRoot root : selectableRoots) {
                if(root.tableName.equals(table.getSchemaName() + "." + table.getTableName())) {
                    selected = root.selected;
                    known = true;
                    break;
                }
            }

            if(known && !selected) {
                it.remove();
                removed = true;
            }

            if(!table.getForeignKeys().isEmpty()) {
                for(ForeignKey fk : table.getForeignKeys()) {
                    for(Reference ref : fk.getReferences()) {
                        Column column = ref.getActualToColumn();
                        if(column.getTable() != table) {
                            children.put(column.getTable(), ref);
                            //TODO potrebbe essere un ciclo nel grafo...
                            if(!selected && !removed) {
                                it.remove();
                                removed = true;
                            }
                        }
                    }
                }
            }
            if(!table.getSelectionProviders().isEmpty()) {
                for(ModelSelectionProvider sp : table.getSelectionProviders()) {
                    for(Reference ref : sp.getReferences()) {
                        Column column = ref.getActualToColumn();
                        if(column.getTable() != table) {
                            children.put(column.getTable(), ref);
                            //TODO potrebbe essere un ciclo nel grafo...
                            if(!selected && !removed) {
                                it.remove();
                                removed = true;
                            }
                        }
                    }
                }
            }

            if(!known) {
                SelectableRoot root =
                        new SelectableRoot(table.getSchemaName() + "." + table.getTableName(), !removed);
                selectableRoots.add(root);
            }
        }
        Collections.sort(selectableRoots, new Comparator<SelectableRoot>() {
            public int compare(SelectableRoot o1, SelectableRoot o2) {
                return o1.tableName.compareTo(o2.tableName);
            }
        });
        return roots;
    }

    @Button(list = "select-tables", key="wizard.next", order = 2)
    public Resolution selectTables() {
        selectSchemas();

        rootsForm.readFromRequest(context.getRequest());
        rootsForm.writeToObject(selectableRoots);
        //Recalc roots
        afterSelectSchemas();

        if(roots.isEmpty()) {
            SessionMessages.addWarningMessage(getMessage("appwizard.warning.noRoot"));
        }

        userAndGroupTablesForm.readFromRequest(context.getRequest());
        userAndGroupTablesForm.writeToObject(this);
        if(!StringUtils.isEmpty(userTableName)) {
            Model tmpModel = new Model();
            tmpModel.getDatabases().add(connectionProvider.getDatabase());
            String[] name = DatabaseLogic.splitQualifiedTableName(userTableName);
            userTable = DatabaseLogic.findTableByName(tmpModel, name[0], name[1], name[2]);

            if(!StringUtils.isEmpty(groupTableName)) {
                name = DatabaseLogic.splitQualifiedTableName(groupTableName);
                groupTable = DatabaseLogic.findTableByName(tmpModel, name[0], name[1], name[2]);
            }

            if(!StringUtils.isEmpty(userGroupTableName)) {
                name = DatabaseLogic.splitQualifiedTableName(userGroupTableName);
                userGroupTable = DatabaseLogic.findTableByName(tmpModel, name[0], name[1], name[2]);
                userGroupTable.setManyToMany(true);
            }

            createUserManagementSetupForm();
            return selectUserFieldsForm();
        } else {
            return buildAppForm();
        }
    }

    protected void createUserManagementSetupForm() {
        DefaultSelectionProvider userSelectionProvider = new DefaultSelectionProvider("");
        for(Column column : userTable.getColumns()) {
            userSelectionProvider.appendRow(
                    column.getActualPropertyName(),
                    column.getActualPropertyName(),
                    true);
        }

        DefaultSelectionProvider algoSelectionProvider = new DefaultSelectionProvider("");
        algoSelectionProvider.appendRow(
                "md5Base64",
                getMessage("appwizard.userTable.encryption.md5Base64"),
                true);
        algoSelectionProvider.appendRow(
                "md5Hex",
                getMessage("appwizard.userTable.encryption.md5Hex"),
                true);
        algoSelectionProvider.appendRow(
                "sha1Base64",
                getMessage("appwizard.userTable.encryption.sha1Base64"),
                true);
        algoSelectionProvider.appendRow(
                "sha1Hex",
                getMessage("appwizard.userTable.encryption.sha1Hex"),
                true);
        try {
            ClassAccessor classAccessor = JavaClassAccessor.getClassAccessor(getClass());
            Mode mode = Mode.CREATE;

            userManagementSetupForm = new Form(mode);

            PropertyAccessor propertyAccessor = classAccessor.getProperty("userIdProperty");
            Field userIdPropertyField = new SelectField(propertyAccessor, userSelectionProvider, mode, "");
            userIdPropertyField.setRequired(true);

            propertyAccessor = classAccessor.getProperty("userNameProperty");
            Field userNamePropertyField = new SelectField(propertyAccessor, userSelectionProvider, mode, "");
            userNamePropertyField.setRequired(true);

            propertyAccessor = classAccessor.getProperty("userPasswordProperty");
            Field userPasswordPropertyField = new SelectField(propertyAccessor, userSelectionProvider, mode, "");
            userPasswordPropertyField.setRequired(true);

            propertyAccessor = classAccessor.getProperty("encryptionAlgorithm");
            Field encryptionAlgorithmField = new SelectField(propertyAccessor, algoSelectionProvider, mode, "");
            encryptionAlgorithmField.setRequired(true);

            FieldSet uFieldSet = new FieldSet(getMessage("appwizard.userTable"), 1, mode);
            uFieldSet.add(userIdPropertyField);
            uFieldSet.add(userNamePropertyField);
            uFieldSet.add(userPasswordPropertyField);
            uFieldSet.add(encryptionAlgorithmField);
            userManagementSetupForm.add(uFieldSet);

            userIdProperty = userTable.getPrimaryKey().getColumns().get(0).getActualPropertyName();

            if(groupTable != null && userGroupTable != null) {
                DefaultSelectionProvider groupSelectionProvider = new DefaultSelectionProvider("");
                for(Column column : groupTable.getColumns()) {
                    groupSelectionProvider.appendRow(
                            column.getActualPropertyName(),
                            column.getActualPropertyName(),
                            true);
                }

                DefaultSelectionProvider userGroupSelectionProvider = new DefaultSelectionProvider("");
                for(Column column : userGroupTable.getColumns()) {
                    userGroupSelectionProvider.appendRow(
                            column.getActualPropertyName(),
                            column.getActualPropertyName(),
                            true);
                }

                propertyAccessor = classAccessor.getProperty("groupIdProperty");
                Field groupIdPropertyField = new SelectField(propertyAccessor, groupSelectionProvider, mode, "");
                groupIdPropertyField.setRequired(true);

                propertyAccessor = classAccessor.getProperty("groupNameProperty");
                Field groupNamePropertyField = new SelectField(propertyAccessor, groupSelectionProvider, mode, "");
                groupNamePropertyField.setRequired(true);

                propertyAccessor = classAccessor.getProperty("groupLinkProperty");
                Field groupLinkPropertyField = new SelectField(propertyAccessor, userGroupSelectionProvider, mode, "");
                groupLinkPropertyField.setRequired(true);

                propertyAccessor = classAccessor.getProperty("userLinkProperty");
                Field userLinkPropertyField = new SelectField(propertyAccessor, userGroupSelectionProvider, mode, "");
                userLinkPropertyField.setRequired(true);

                propertyAccessor = classAccessor.getProperty("adminGroupName");
                Field adminGroupNameField = new TextField(propertyAccessor, mode);

                FieldSet gFieldSet = new FieldSet(getMessage("appwizard.groupTable"), 1, mode);
                gFieldSet.add(groupIdPropertyField);
                gFieldSet.add(groupNamePropertyField);
                gFieldSet.add(groupLinkPropertyField);
                gFieldSet.add(userLinkPropertyField);
                gFieldSet.add(adminGroupNameField);
                userManagementSetupForm.add(gFieldSet);

                groupIdProperty = groupTable.getPrimaryKey().getColumns().get(0).getActualPropertyName();

                for(ForeignKey fk : userGroupTable.getForeignKeys()) {
                    for(Reference ref : fk.getReferences()) {
                        if(ref.getActualToColumn().getTable().equals(userTable)) {
                            userLinkProperty = ref.getActualFromColumn().getActualPropertyName();
                        } else if(ref.getActualToColumn().getTable().equals(groupTable)) {
                            groupLinkProperty = ref.getActualFromColumn().getActualPropertyName();
                        }
                    }
                }
            }

            userManagementSetupForm.readFromObject(this);
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    @Button(list = "select-user-fields", key="wizard.next", order = 2)
    public Resolution selectUserFields() {
        selectTables();
        if(userTable != null) {
            userManagementSetupForm.readFromRequest(context.getRequest());
            if(userManagementSetupForm.validate()) {
                userManagementSetupForm.writeToObject(this);
                return buildAppForm();
            } else {
                return selectUserFieldsForm();
            }
        } else {
            return buildAppForm();
        }
    }

    protected Resolution selectUserFieldsForm() {
        step = 3;
        return new ForwardResolution("/layouts/admin/appwizard/select-user-fields.jsp");
    }

    protected Resolution buildAppForm() {
        step = (userTable == null) ? 3 : 4;
        return new ForwardResolution("/layouts/admin/appwizard/build-app.jsp");
    }

    @Button(list = "build-app", key="wizard.prev", order = 1)
    public Resolution goBackFromBuildApplication() {
        selectUserFields();
        if(userTable == null) {
            return selectTablesForm();
        } else {
            return selectUserFieldsForm();
        }
    }

    @Button(list = "build-app", key="wizard.finish", order = 2)
    public Resolution buildApplication() {
        selectUserFields();
        application.getModel().getDatabases().add(connectionProvider.getDatabase());
        application.initModel();
        try {
            TemplateEngine engine = new SimpleTemplateEngine();
            Template template = engine.createTemplate(ApplicationWizard.class.getResource("CrudPage.groovy"));
            List<ChildPage> childPages = new ArrayList<ChildPage>();
            for(Table table : roots) {
                File dir = new File(application.getPagesDir(), table.getActualEntityName());
                depth = 1;
                createCrudPage(dir, table, childPages, template);
            }
            if(userTable != null) {
                setupUsers(childPages, template);
            }
            setupCalendar(childPages);
            Page rootPage = DispatcherLogic.getPage(application.getPagesDir());
            Collections.sort(childPages, new Comparator<ChildPage>() {
                public int compare(ChildPage o1, ChildPage o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
            rootPage.getLayout().getChildPages().addAll(childPages);
            DispatcherLogic.savePage(application.getPagesDir(), rootPage);
        } catch (Exception e) {
            logger.error("Error while creating pages", e);
            SessionMessages.addErrorMessage(getMessage("appwizard.error.createPagesFailed", e));
            return buildAppForm();
        }
        try {
            application.initModel();
            application.saveXmlModel();
        } catch (Exception e) {
            logger.error("Could not save model", e);
            SessionMessages.addErrorMessage(
                    getMessage("appwizard.error.saveModelFailed", ExceptionUtils.getRootCauseMessage(e)));
            application.getModel().getDatabases().remove(connectionProvider.getDatabase());
            application.initModel();
            return buildAppForm();
        }
        SessionMessages.addInfoMessage(getMessage("appwizard.finished"));
        if(userTable != null) {
            SessionMessages.addWarningMessage(getMessage("appwizard.warning.userTable.created"));
            //ShiroUtils.clearCache(SecurityUtils.getSubject().getPrincipals());
        }
        return new RedirectResolution("/");
    }

    public String getMessage(String key, Object... args) {
        Locale locale = context.getLocale();
        ResourceBundle resourceBundle = application.getBundle(locale);
        String msg = resourceBundle.getString(key);
        return MessageFormat.format(msg, args);
    }

    protected void setupCalendar(List<ChildPage> childPages) throws Exception {
        List<List<String>> calendarDefinitions = new ArrayList<List<String>>();
        Color[] colors = {
                Color.RED, Color.BLUE, Color.CYAN.darker(), Color.GRAY, Color.GREEN.darker(),
                Color.ORANGE, Color.YELLOW.darker(), Color.MAGENTA.darker(), Color.PINK
            };
        int colorIndex = 0;
        for(Table table : allTables) {
            List<Column> dateColumns = new ArrayList<Column>();
            for(Column column : table.getColumns()) {
                if(column.getActualJavaType() != null &&
                   Date.class.isAssignableFrom(column.getActualJavaType())) {
                    dateColumns.add(column);
                }
            }
            if(!dateColumns.isEmpty()) {
                //["Cal 1", "db1.schema1.table1", ["column1", "column2"], Color.RED]
                Color color = colors[colorIndex++ % colors.length];
                List<String> calDef = new ArrayList<String>();
                calDef.add('"' + Util.guessToWords(table.getActualEntityName()) + '"');
                calDef.add('"' + table.getQualifiedName() + '"');
                String cols = "[";
                boolean first = true;
                for(Column column : dateColumns) {
                    if(first) {
                        first = false;
                    } else {
                        cols += ", ";
                    }
                    cols += '"' + column.getActualPropertyName() + '"';
                }
                cols += "]";
                calDef.add(cols);
                calDef.add("new java.awt.Color(" + color.getRed() + ", " + color.getGreen() +
                           ", " + color.getBlue() + ")");
                calendarDefinitions.add(calDef);
            }
        }
        if(!calendarDefinitions.isEmpty()) {
            String calendarDefinitionsStr = "[";
            calendarDefinitionsStr += StringUtils.join(calendarDefinitions, ", ");
            calendarDefinitionsStr += "]";
            File dir = new File(application.getPagesDir(), "calendar");
            if(dir.exists()) {
                SessionMessages.addWarningMessage(
                        getMessage("appwizard.error.directoryExists", dir.getAbsolutePath()));
            } else if(dir.mkdirs()) {
                CalendarConfiguration configuration = new CalendarConfiguration();
                DispatcherLogic.saveConfiguration(dir, configuration);

                Page page = new Page();
                page.setId(RandomUtil.createRandomId());
                page.setTitle("Calendar (generated)");
                page.setDescription("Calendar (generated)");

                DispatcherLogic.savePage(dir, page);
                File actionFile = new File(dir, "action.groovy");
                try {
                    TemplateEngine engine = new SimpleTemplateEngine();
                    Template template = engine.createTemplate(ApplicationWizard.class.getResource("CalendarPage.groovy"));
                    Map<String, Object> bindings = new HashMap<String, Object>();
                    bindings.put("calendarDefinitions", calendarDefinitionsStr);
                    FileWriter fw = new FileWriter(actionFile);
                    template.make(bindings).writeTo(fw);
                    IOUtils.closeQuietly(fw);
                } catch (Exception e) {
                    logger.warn("Couldn't create calendar", e);
                    SessionMessages.addWarningMessage("Couldn't create calendar: " + e);
                    return;
                }

                ChildPage childPage = new ChildPage();
                childPage.setName(dir.getName());
                childPage.setShowInNavigation(true);
                childPages.add(childPage);
            } else {
                logger.warn("Couldn't create directory {}", dir.getAbsolutePath());
                SessionMessages.addWarningMessage(
                        getMessage("appwizard.error.createDirectoryFailed", dir.getAbsolutePath()));
            }
        }
    }

    protected void setupUsers(List<ChildPage> childPages, Template template) throws Exception {
        if(!roots.contains(userTable)) {
            File dir = new File(application.getPagesDir(), userTable.getActualEntityName());
            depth = 1;
            createCrudPage(dir, userTable, childPages, template);
        }

        Configuration conf = application.getPortofinoProperties();
        List<Reference> references = (List<Reference>) children.get(userTable);
        if(references != null) {
            for(Reference ref : references) {
                depth = 1;
                Column fromColumn = ref.getActualFromColumn();
                Column toColumn = ref.getActualToColumn();
                Table fromTable = fromColumn.getTable();
                Table toTable = toColumn.getTable();
                String entityName = fromTable.getActualEntityName();
                List<Column> pkColumns = toTable.getPrimaryKey().getColumns();
                if(!pkColumns.contains(toColumn)) {
                    continue;
                }
                String linkToUserProperty = fromColumn.getActualPropertyName();
                String childQuery =
                        "from " + entityName +
                        " where " + linkToUserProperty +
                        " = %{#securityUtils.getPrincipal(1)}";
                String dirName = "my-" + entityName;
                boolean multipleRoles = isMultipleRoles(fromTable, ref, references);
                if(multipleRoles) {
                    dirName += "-as-" + linkToUserProperty;
                }
                File dir = new File(application.getPagesDir(), dirName);
                String title = Util.guessToWords(dirName);

                Map<String, String> bindings = new HashMap<String, String>();
                bindings.put("parentName", "securityUtils");
                bindings.put("parentProperty", "getPrincipal(1)");
                bindings.put("linkToParentProperty", linkToUserProperty);

                Page page = createCrudPage(
                        dir, fromTable, childQuery,
                        childPages, template, bindings, title);
                if(page != null) {
                    Group group = new Group();
                    group.setName(conf.getString(PortofinoProperties.GROUP_ANONYMOUS));
                    group.setAccessLevel(AccessLevel.DENY.name());
                    Permissions permissions = new Permissions();
                    permissions.getGroups().add(group);
                    page.setPermissions(permissions);
                    DispatcherLogic.savePage(dir, page);
                }
            }
        }
        try {
            TemplateEngine engine = new SimpleTemplateEngine();
            Template secTemplate = engine.createTemplate(ApplicationWizard.class.getResource("security.groovy"));
            Map<String, String> bindings = new HashMap<String, String>();
            bindings.put("databaseName", connectionProvider.getDatabase().getDatabaseName());
            bindings.put("userTableEntityName", userTable.getActualEntityName());
            bindings.put("userIdProperty", userIdProperty);
            bindings.put("userNameProperty", userNameProperty);
            bindings.put("passwordProperty", userPasswordProperty);

            bindings.put("groupTableEntityName",
                    groupTable != null ? groupTable.getActualEntityName() : "");
            bindings.put("groupIdProperty", StringUtils.defaultString(groupIdProperty));
            bindings.put("groupNameProperty", StringUtils.defaultString(groupNameProperty));

            bindings.put("userGroupTableEntityName",
                    userGroupTable != null ? userGroupTable.getActualEntityName() : "");
            bindings.put("groupLinkProperty", StringUtils.defaultString(groupLinkProperty));
            bindings.put("userLinkProperty", StringUtils.defaultString(userLinkProperty));
            bindings.put("adminGroupName", StringUtils.defaultString(adminGroupName));

            bindings.put("encryptionAlgorithm", encryptionAlgorithm);
            FileWriter fw = new FileWriter(new File(application.getAppScriptsDir(), "security.groovy"));
            secTemplate.make(bindings).writeTo(fw);
            IOUtils.closeQuietly(fw);
        } catch (Exception e) {
            logger.warn("Couldn't configure users", e);
            SessionMessages.addWarningMessage(getMessage("appwizard.error.userSetupFailed", e));
        }
    }

    private boolean isMultipleRoles(Table fromTable, Reference ref, List<Reference> references) {
        boolean multipleRoles = false;
        for(Reference ref2 : references) {
            if(ref2 != ref && ref2.getActualFromColumn().getTable().equals(fromTable)) {
                multipleRoles = true;
                break;
            }
        }
        return multipleRoles;
    }

    protected Page createCrudPage(File dir, Table table, List<ChildPage> childPages, Template template)
            throws Exception {
        String query = "from " + table.getActualEntityName();
        String title = Util.guessToWords(table.getActualEntityName());
        HashMap<String, String> bindings = new HashMap<String, String>();
        bindings.put("parentName", "");
        bindings.put("parentProperty", "nothing");
        bindings.put("linkToParentProperty", NO_LINK_TO_PARENT);
        return createCrudPage(dir, table, query, childPages, template, bindings, title);
    }

    protected Page createCrudPage(
            File dir, Table table, String query, List<ChildPage> childPages,
            Template template, Map<String, String> bindings, String title)
            throws Exception {
        if(dir.exists()) {
            SessionMessages.addWarningMessage(
                        getMessage("appwizard.error.directoryExists", dir.getAbsolutePath()));
            return null;
        } else if(dir.mkdirs()) {
            logger.info("Creating CRUD page {}", dir.getAbsolutePath());
            CrudConfiguration configuration = new CrudConfiguration();
            configuration.setDatabase(connectionProvider.getDatabase().getDatabaseName());

            configuration.setQuery(query);
            String variable = table.getActualEntityName();
            configuration.setVariable(variable);
            detectLargeResultSet(table, configuration);

            configuration.setName(table.getActualEntityName());
            configuration.setSearchTitle("Search " + title);
            configuration.setCreateTitle("Create " + title);
            configuration.setEditTitle("Edit " + title);
            configuration.setReadTitle(title);

            int summ = 0;
            String linkToParentProperty = bindings.get("linkToParentProperty");
            for(Column column : table.getColumns()) {
                summ = setupColumn(column, configuration, summ, linkToParentProperty);
            }

            DispatcherLogic.saveConfiguration(dir, configuration);
            Page page = new Page();
            page.setId(RandomUtil.createRandomId());
            page.setTitle(title);
            page.setDescription(title);

            List<Reference> references = (List<Reference>) children.get(table);
            if(references != null && depth < maxDepth) {
                ArrayList<ChildPage> pages = page.getDetailLayout().getChildPages();
                depth++;
                for(Reference ref : references) {
                    createChildCrudPage(dir, template, variable, references, ref, pages);
                }
                Collections.sort(pages, new Comparator<ChildPage>() {
                    public int compare(ChildPage o1, ChildPage o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                });
            }

            DispatcherLogic.savePage(dir, page);
            File actionFile = new File(dir, "action.groovy");
            FileWriter fileWriter = new FileWriter(actionFile);

            template.make(bindings).writeTo(fileWriter);
            IOUtils.closeQuietly(fileWriter);

            ChildPage childPage = new ChildPage();
            childPage.setName(dir.getName());
            childPage.setShowInNavigation(true);
            childPages.add(childPage);

            return page;
        } else {
            logger.warn("Couldn't create directory {}", dir.getAbsolutePath());
            SessionMessages.addWarningMessage(
                    getMessage("appwizard.error.createDirectoryFailed", dir.getAbsolutePath()));
            return null;
        }
    }

    public static final int MULTILINE_THRESHOLD = 200;

    protected int setupColumn
            (Column column, CrudConfiguration configuration, int columnsInSummary, String linkToParentProperty) {

        if(column.getActualJavaType() == null) {
            logger.debug("Column without a javaType, skipping: {}", column.getQualifiedName());
            return columnsInSummary;
        }

        Table table = column.getTable();
        @SuppressWarnings({"StringEquality"})
        boolean enabled =
                !(linkToParentProperty != NO_LINK_TO_PARENT &&
                column.getActualPropertyName().equals(linkToParentProperty))
                && !isUnsupportedProperty(column);
        boolean propertyIsUserPassword =
                table.getQualifiedName().equals(userTableName) &&
                column.getActualPropertyName().equals(userPasswordProperty);
        boolean inPk = isInPk(column);
        boolean inFk = isInFk(column);
        boolean inSummary =
                enabled &&
                (inPk || columnsInSummary < maxColumnsInSummary) &&
                !propertyIsUserPassword;
        boolean updatable = enabled && !column.isAutoincrement() && !inPk;
        boolean insertable = enabled && !column.isAutoincrement();

        if(!configuration.isLargeResultSet()) {
            detectBooleanColumn(table, column);
        }

        if(enabled && inPk && !inFk &&
           Number.class.isAssignableFrom(column.getActualJavaType())) {
            for(PrimaryKeyColumn pkc : table.getPrimaryKey().getPrimaryKeyColumns()) {
                if(pkc.getActualColumn().equals(column)) {
                    pkc.setGenerator(new IncrementGenerator(pkc));
                    insertable = false;
                    break;
                }
            }
        }

        if(propertyIsUserPassword) {
            Annotation annotation = new Annotation(column, Password.class.getName());
            column.getAnnotations().add(annotation);
            insertable = false;
            updatable = false;
        }

        if(!propertyIsUserPassword &&
           column.getActualJavaType() == String.class &&
           column.getLength() > MULTILINE_THRESHOLD) {
            Annotation annotation = new Annotation(column, Multiline.class.getName());
            annotation.getValues().add("true");
            column.getAnnotations().add(annotation);
        }

        CrudProperty crudProperty = new CrudProperty();
        crudProperty.setEnabled(enabled);
        crudProperty.setName(column.getActualPropertyName());
        crudProperty.setInsertable(insertable);
        crudProperty.setUpdatable(updatable);
        if(inSummary) {
            crudProperty.setInSummary(true);
            crudProperty.setSearchable(true);
            columnsInSummary++;
        }
        configuration.getProperties().add(crudProperty);

        return columnsInSummary;
    }

    protected static boolean isInPk(Column column) {
        return column.getTable().getPrimaryKey().getColumns().contains(column);
    }

    protected static boolean isInFk(Column column) {
        Table table = column.getTable();
        for(ForeignKey fk : table.getForeignKeys()) {
            for(Reference ref : fk.getReferences()) {
                if(ref.getActualFromColumn().equals(column)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isUnsupportedProperty(Column column) {
        //I blob su db non sono supportati al momento
        return column.getJdbcType() == Types.BLOB || column.getJdbcType() == Types.LONGVARBINARY;
    }

    protected void detectBooleanColumn(Table table, Column column) {
        if(column.getJdbcType() == Types.INTEGER ||
           column.getJdbcType() == Types.DECIMAL ||
           column.getJdbcType() == Types.NUMERIC) {
            logger.info(
                    "Detecting whether numeric column " + column.getQualifiedName() + " is boolean by examining " +
                    "its values...");

            //Detect booleans
            Connection connection = null;

            try {
                connection = connectionProvider.acquireConnection();
                liquibase.database.Database implementation =
                        DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
                                new JdbcConnection(connection));
                String sql =
                    "select count(" + implementation.escapeDatabaseObject(column.getColumnName()) + ") " +
                    "from " + implementation.escapeTableName(table.getSchemaName(), table.getTableName());
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql);
                setQueryTimeout(statement, 1);
                statement.setMaxRows(1);
                ResultSet rs = statement.executeQuery();
                Long count = null;
                if(rs.next()) {
                    count = safeGetLong(rs, 1);
                }

                if(count == null || count < 10) {
                    logger.info("Cannot determine if numeric column {} is boolean, count is {}",
                                column.getQualifiedName(), count);
                    return;
                }

                sql =
                    "select distinct(" + implementation.escapeDatabaseObject(column.getColumnName()) + ") " +
                    "from " + implementation.escapeTableName(table.getSchemaName(), table.getTableName());
                statement =
                        connection.prepareStatement(
                                sql);
                setQueryTimeout(statement, 1);
                statement.setMaxRows(3);
                rs = statement.executeQuery();
                int valueCount = 0;
                boolean only0and1 = true;
                while(rs.next()) {
                    valueCount++;
                    if(valueCount > 2) {
                        only0and1 = false;
                        break;
                    }
                    Long value = safeGetLong(rs, 1);
                    only0and1 &= value != null && (value == 0 || value == 1);
                }
                if(only0and1 && valueCount == 2) {
                    logger.info("Column appears to be of boolean type.");
                    column.setJavaType(Boolean.class.getName());
                } else {
                    logger.info("Column appears not to be of boolean type.");
                }
                statement.close();
            } catch (Exception e) {
                logger.error("Could not determine whether column " + column.getQualifiedName() + " is boolean", e);
            } finally {
                try {
                    if(connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    logger.error("Could not close connection", e);
                }
            }
        }
    }

    protected void detectLargeResultSet(Table table, CrudConfiguration configuration) {
        Connection connection = null;
        try {
            logger.info("Trying to detect whether table {} has many records...", table.getQualifiedName());
            connection = connectionProvider.acquireConnection();
            liquibase.database.Database implementation =
                        DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            String sql =
                    "select count(*) from " + implementation.escapeTableName(table.getSchemaName(), table.getTableName());
            PreparedStatement statement = connection.prepareStatement(sql);
            setQueryTimeout(statement, 1);
            statement.setMaxRows(1);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                Long count = safeGetLong(rs, 1);
                if(count != null) {
                    if(count > LARGE_RESULT_SET_THRESHOLD) {
                        logger.info(
                                "Table " + table.getQualifiedName() + " currently has " + count + " rows, which is bigger than " +
                                "the threshold (" + LARGE_RESULT_SET_THRESHOLD + ") for large result sets. It will be " +
                                "marked as largeResultSet = true and no autodetection based on table data will be " +
                                "attempted, in order to keep the processing time reasonable.");
                        configuration.setLargeResultSet(true);
                    } else {
                        logger.info(
                                "Table " + table.getQualifiedName() + " currently has " + count + " rows, which is smaller than " +
                                "the threshold (" + LARGE_RESULT_SET_THRESHOLD + ") for large result sets. It will be " +
                                "analyzed normally.");
                    }
                } else {
                    logger.warn("Could not determine number of records, assuming large result set");
                    configuration.setLargeResultSet(true);
                }
            }
            statement.close();
        } catch (Exception e) {
            logger.error("Could not determine count", e);
        } finally {
            try {
                if(connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error("Could not close connection", e);
            }
        }
    }

    protected void setQueryTimeout(PreparedStatement statement, int seconds) {
        try {
            statement.setQueryTimeout(seconds);
        } catch (Exception e) {
            logger.debug("setQueryTimeout not supported", e);
        }
    }

    public static Long safeGetLong(ResultSet rs, int index) throws SQLException {
        Object object = rs.getObject(index);
        if(object instanceof Number) {
            return ((Number) object).longValue();
        } else {
            return null;
        }
    }

    protected void createChildCrudPage(
            File dir, Template template, String parentName, List<Reference> references,
            Reference ref, ArrayList<ChildPage> pages)
            throws Exception {
        Column fromColumn = ref.getActualFromColumn();
        Table fromTable = fromColumn.getTable();
        String entityName = fromTable.getActualEntityName();
        String parentProperty = ref.getActualToColumn().getActualPropertyName();
        String linkToParentProperty = fromColumn.getActualPropertyName();
        String childQuery =
                "from " + entityName +
                " where " + linkToParentProperty +
                " = %{#" + parentName + "." + parentProperty + "}";
        String childDirName = entityName;
        boolean multipleRoles = isMultipleRoles(fromTable, ref, references);
        if(multipleRoles) {
            childDirName += "-as-" + linkToParentProperty;
        }
        File childDir = new File(new File(dir, "_detail"), childDirName);
        String childTitle = Util.guessToWords(childDirName);

        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("parentName", parentName);
        bindings.put("parentProperty", parentProperty);
        bindings.put("linkToParentProperty", linkToParentProperty);

        createCrudPage(
                childDir, fromTable, childQuery,
                pages, template, bindings, childTitle);
    }

    public Form getJndiCPForm() {
        return jndiCPForm;
    }

    public Form getJdbcCPForm() {
        return jdbcCPForm;
    }

    public ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    public boolean isJdbc() {
        return connectionProviderType == null || connectionProviderType.equals(JDBC);
    }

    public boolean isJndi() {
        return StringUtils.equals(connectionProviderType, JNDI);
    }

    public String getActionPath() {
        return (String) getContext().getRequest().getAttribute(ActionResolver.RESOLVED_ACTION);
    }

    public String getConnectionProviderType() {
        return connectionProviderType;
    }

    public void setConnectionProviderType(String connectionProviderType) {
        this.connectionProviderType = connectionProviderType;
    }

    public Form getConnectionProviderForm() {
        return connectionProviderForm;
    }

    public TableForm getSchemasForm() {
        return schemasForm;
    }

    public List<SelectableSchema> getSelectableSchemas() {
        return selectableSchemas;
    }

    @LabelI18N("appwizard.userTable.name")
    public String getUserTableName() {
        return userTableName;
    }

    public void setUserTableName(String userTableName) {
        this.userTableName = userTableName;
    }

    @LabelI18N("appwizard.groupTable.name")
    public String getGroupTableName() {
        return groupTableName;
    }

    public void setGroupTableName(String groupTableName) {
        this.groupTableName = groupTableName;
    }

    public Form getUserAndGroupTablesForm() {
        return userAndGroupTablesForm;
    }

    public Form getUserManagementSetupForm() {
        return userManagementSetupForm;
    }

    @LabelI18N("appwizard.userTable.nameProperty")
    public String getUserNameProperty() {
        return userNameProperty;
    }

    public void setUserNameProperty(String userNameProperty) {
        this.userNameProperty = userNameProperty;
    }

    @LabelI18N("appwizard.userTable.idProperty")
    public String getUserIdProperty() {
        return userIdProperty;
    }

    public void setUserIdProperty(String userIdProperty) {
        this.userIdProperty = userIdProperty;
    }

    @LabelI18N("appwizard.userTable.passwordProperty")
    public String getUserPasswordProperty() {
        return userPasswordProperty;
    }

    public void setUserPasswordProperty(String userPasswordProperty) {
        this.userPasswordProperty = userPasswordProperty;
    }

    public String getGroupIdProperty() {
        return groupIdProperty;
    }

    public void setGroupIdProperty(String groupIdProperty) {
        this.groupIdProperty = groupIdProperty;
    }

    @LabelI18N("appwizard.userGroupTable.name")
    public String getUserGroupTableName() {
        return userGroupTableName;
    }

    public void setUserGroupTableName(String userGroupTableName) {
        this.userGroupTableName = userGroupTableName;
    }

    public String getGroupNameProperty() {
        return groupNameProperty;
    }

    public void setGroupNameProperty(String groupNameProperty) {
        this.groupNameProperty = groupNameProperty;
    }

    @LabelI18N("appwizard.userGroupTable.groupLinkProperty")
    public String getGroupLinkProperty() {
        return groupLinkProperty;
    }

    public void setGroupLinkProperty(String groupLinkProperty) {
        this.groupLinkProperty = groupLinkProperty;
    }

    @LabelI18N("appwizard.userGroupTable.userLinkProperty")
    public String getUserLinkProperty() {
        return userLinkProperty;
    }

    public void setUserLinkProperty(String userLinkProperty) {
        this.userLinkProperty = userLinkProperty;
    }

    @LabelI18N("appwizard.groupTable.adminGroupName")
    public String getAdminGroupName() {
        return adminGroupName;
    }

    public void setAdminGroupName(String adminGroupName) {
        this.adminGroupName = adminGroupName;
    }

    @LabelI18N("appwizard.showAdvancedOptions")
    public boolean isAdvanced() {
        return advanced;
    }

    public void setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }

    @LabelI18N("appwizard.userTable.encryption")
    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public Form getAdvancedOptionsForm() {
        return advancedOptionsForm;
    }

    //Wizard implementation

    @Override
    public List<Step> getSteps() {
        List<Step> steps = new ArrayList<Step>();
        steps.add(new Step(getMessage("appwizard.step1"), getMessage("appwizard.step1.title")));
        steps.add(new Step(getMessage("appwizard.step2"), getMessage("appwizard.step2.title")));
        steps.add(new Step(getMessage("appwizard.step3"), getMessage("appwizard.step3.title")));
        if(userTable != null) {
            steps.add(new Step(getMessage("appwizard.step3a"), getMessage("appwizard.step3a.title")));
        }
        steps.add(new Step(getMessage("appwizard.step4"), getMessage("appwizard.step4.title")));
        return steps;
    }

    @Override
    public int getCurrentStepIndex() {
        return step;
    }
}
