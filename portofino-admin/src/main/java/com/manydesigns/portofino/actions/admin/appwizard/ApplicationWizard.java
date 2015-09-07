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

package com.manydesigns.portofino.actions.admin.appwizard;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.manydesigns.elements.ElementsThreadLocals;
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
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.portofino.actions.admin.database.forms.ConnectionProviderForm;
import com.manydesigns.portofino.actions.admin.database.forms.SelectableSchema;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.database.platforms.DatabasePlatform;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsRegistry;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.modules.PageactionsModule;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.calendar.configuration.CalendarConfiguration;
import com.manydesigns.portofino.pageactions.crud.configuration.database.CrudConfiguration;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudProperty;
import com.manydesigns.portofino.pages.ChildPage;
import com.manydesigns.portofino.pages.Group;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.pages.Permissions;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.sync.DatabaseSyncer;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import net.sourceforge.stripes.action.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAuthentication
@RequiresAdministrator
@UrlBinding(ApplicationWizard.URL_BINDING)
public class ApplicationWizard extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final String URL_BINDING = "/actions/admin/wizard";

    public static final String JDBC = "JDBC";
    public static final String JNDI = "JNDI";
    @SuppressWarnings({"RedundantStringConstructorCall"})
    public static final String NO_LINK_TO_PARENT = new String();
    public static final int LARGE_RESULT_SET_THRESHOLD = 10000;

    protected int step = 0;

    //Forms and fields
    protected SelectField connectionProviderField;
    protected Form jndiCPForm;
    protected Form jdbcCPForm;
    protected Form connectionProviderForm;

    protected Form userAndGroupTablesForm;
    protected Form userManagementSetupForm;

    protected String connectionProviderType;
    protected String connectionProviderName;
    protected ConnectionProvider connectionProvider;
    protected Database database;

    public TableForm schemasForm;
    protected List<SelectableSchema> selectableSchemas;
    public TableForm rootsForm;
    protected List<SelectableRoot> selectableRoots = new ArrayList<SelectableRoot>();

    protected String generationStrategy = "AUTO";

    protected BooleanField generateCalendarField;
    protected boolean generateCalendar = true;

    //Users
    protected String userTableName;
    protected String groupTableName;
    protected String userGroupTableName;

    protected String userNameProperty;
    protected String userEmailProperty;
    protected String userTokenProperty;
    protected String userIdProperty;
    protected String userPasswordProperty;
    protected String encryptionAlgorithm;
    protected String groupIdProperty;
    protected String groupNameProperty;
    protected String groupLinkProperty;
    protected String userLinkProperty;
    protected String adminGroupName;

    protected List<Table> roots;
    protected ListMultimap<Table, Reference> children;
    protected List<Table> allTables;
    protected Table userTable;
    protected Table groupTable;
    protected Table userGroupTable;
    protected int maxColumnsInSummary = 5;
    protected int maxDepth = 5;
    protected int depth;

    private final String databaseSessionKey = getClass().getName() + ".database";

    public final static String[] JDBC_CP_FIELDS =
            {"databaseName", "driver", "url", "username", "password" };

    public final static String[] JNDI_CP_FIELDS = { "databaseName", "jndiResource" };

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(DatabaseModule.PERSISTENCE)
    public Persistence persistence;

    @Inject(PageactionsModule.PAGES_DIRECTORY)
    public File pagesDir;

    @Inject(BaseModule.APPLICATION_DIRECTORY)
    public File appDir;

    public static final Logger logger = LoggerFactory.getLogger(ApplicationWizard.class);

    @DefaultHandler
    @Button(list = "select-schemas", key="previous", order = 1 , icon = Button.ICON_LEFT)
    public Resolution start() {
        buildCPForms();
        context.getRequest().getSession().removeAttribute(databaseSessionKey);
        return createConnectionProviderForm();
    }

    protected Resolution createConnectionProviderForm() {
        step = 0;
        return new ForwardResolution("/m/admin/wizard/connection-provider.jsp");
    }

    @Before
    public void prepare() {
        connectionProviderName = context.getRequest().getParameter("connectionProviderName");
    }

    public List<Database> getActiveDatabases() {
        List<Database> dbs = new ArrayList<Database>();
        for(Database db : persistence.getModel().getDatabases()) {
            ConnectionProvider cp = db.getConnectionProvider();
            if(!ConnectionProvider.STATUS_ERROR.equals(cp.getStatus())) {
                dbs.add(db);
            }
        }
        return dbs;
    }

    protected void buildCPForms() {
        DefaultSelectionProvider connectionProviderSP = new DefaultSelectionProvider("connectionProviderName");
        for(Database db : getActiveDatabases()) {
            ConnectionProvider cp = db.getConnectionProvider();
            connectionProviderSP.appendRow(
                    db.getDatabaseName(),
                    db.getDatabaseName() + " (" + cp.getDatabasePlatform().getDescription() + ")",
                    true);
        }

        ClassAccessor classAccessor = JavaClassAccessor.getClassAccessor(ApplicationWizard.class);
        try {
            connectionProviderField =
                    new SelectField(
                            classAccessor.getProperty("connectionProviderName"),
                            connectionProviderSP,
                            Mode.EDIT,
                            null);
            connectionProviderField.setLabel(ElementsThreadLocals.getText("use.an.existing.database.connection"));
            connectionProviderField.setComboLabel("--");
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }

        jndiCPForm = new FormBuilder(ConnectionProviderForm.class)
                            .configFields(JNDI_CP_FIELDS)
                            .configPrefix("jndi")
                            .configMode(Mode.CREATE)
                            .build();

        DefaultSelectionProvider driverSelectionProvider = new DefaultSelectionProvider("name");
        // database platforms
        DatabasePlatformsRegistry manager =
                persistence.getDatabasePlatformsRegistry();
        DatabasePlatform[] databasePlatforms = manager.getDatabasePlatforms();
        for(DatabasePlatform dp : databasePlatforms) {
            if(DatabasePlatform.STATUS_OK.equals(dp.getStatus())) {
                driverSelectionProvider.appendRow(dp.getStandardDriverClassName(), dp.getDescription(), true);
            }
        }

        jdbcCPForm = new FormBuilder(ConnectionProviderForm.class)
                            .configFields(JDBC_CP_FIELDS)
                            .configPrefix("jdbc")
                            .configMode(Mode.CREATE)
                            .configSelectionProvider(driverSelectionProvider, "driver")
                            .build();

        jdbcCPForm.findFieldByPropertyName("driver").setHelp(ElementsThreadLocals.getText("additional.drivers.can.be.downloaded"));

        //Handle back
        jndiCPForm.readFromRequest(context.getRequest());
        jdbcCPForm.readFromRequest(context.getRequest());
        connectionProviderField.readFromObject(this);
        connectionProviderField.readFromRequest(context.getRequest());
    }

    @Button(list = "user-management", key="previous", order = 1 , icon = Button.ICON_LEFT)
    public Resolution backToSelectSchemas() {
        context.getRequest().getSession().removeAttribute(databaseSessionKey);
        return configureConnectionProvider();
    }

    @Button(list = "connection-provider", key = "return.to.pages", order = 0  , icon = Button.ICON_HOME)
    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

    @Button(list = "connection-provider", key="next", order = 1, type = Button.TYPE_PRIMARY , icon = Button.ICON_RIGHT , iconBefore = false)
    public Resolution configureConnectionProvider() {
        buildCPForms();
        if(connectionProviderField.validate()) {
            connectionProviderField.writeToObject(this);
        } else {
            return createConnectionProviderForm();
        }
        if(!isNewConnectionProvider()) {
            connectionProvider =
                    DatabaseLogic.findDatabaseByName(
                            persistence.getModel(),
                            connectionProviderName).getConnectionProvider();
            return afterCreateConnectionProvider();
        }
        if(JDBC.equals(connectionProviderType)) {
            JdbcConnectionProvider jdbcConnectionProvider = new JdbcConnectionProvider();
            //Fill with dummy values so the form overwrites them (and doesn't try to write on the Configuration which
            //is not available and anyway should not be modified right now)
            jdbcConnectionProvider.setUrl("replace me");
            jdbcConnectionProvider.setUsername("replace me");
            jdbcConnectionProvider.setPassword("replace me");
            connectionProvider = jdbcConnectionProvider;
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
            Database existingDatabase =
                    DatabaseLogic.findDatabaseByName(persistence.getModel(), edit.getDatabaseName());
            if(existingDatabase != null) {
                SessionMessages.addErrorMessage(ElementsThreadLocals.getText("there.is.already.a.database.named._", edit.getDatabaseName()));
                return createConnectionProviderForm();
            }
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
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("couldnt.read.schema.names.from.db._", e));
            return createConnectionProviderForm();
        }
        return selectSchemasForm();
    }

    protected Resolution selectSchemasForm() {
        step = 1;
        return new ForwardResolution("/m/admin/wizard/select-schemas.jsp");
    }

    protected void configureEditSchemas() throws Exception {
        connectionProvider.init(persistence.getDatabasePlatformsRegistry());
        Connection conn = connectionProvider.acquireConnection();
        logger.debug("Reading database metadata");
        DatabaseMetaData metadata = conn.getMetaData();
        List<String> schemaNamesFromDb =
                connectionProvider.getDatabasePlatform().getSchemaNames(metadata);
        connectionProvider.releaseConnection(conn);

        selectableSchemas = new ArrayList<SelectableSchema>(schemaNamesFromDb.size());
        for(String schemaName : schemaNamesFromDb) {
            SelectableSchema schema = new SelectableSchema(schemaName, schemaNamesFromDb.size() == 1);
            selectableSchemas.add(schema);
        }
        schemasForm = new TableFormBuilder(SelectableSchema.class)
                .configFields("selected", "schemaName")
                .configMode(Mode.EDIT)
                .configNRows(selectableSchemas.size())
                .configPrefix("schemas_")
                .build();
        schemasForm.readFromObject(selectableSchemas);
        //Handle back
        schemasForm.readFromRequest(context.getRequest());
    }

    @Buttons({
        @Button(list = "select-schemas", key="next", order = 2, type = Button.TYPE_PRIMARY , icon = Button.ICON_RIGHT , iconBefore = false ),
        @Button(list = "select-user-fields", key="previous", order = 1 , icon = Button.ICON_LEFT)
    })
    public Resolution selectSchemas() {
        configureConnectionProvider();
        schemasForm.readFromRequest(context.getRequest());
        if(schemasForm.validate()) {
            schemasForm.writeToObject(selectableSchemas);
            boolean atLeastOneSelected = isAtLeastOneSchemaSelected();
            if(atLeastOneSelected) {
                if (configureModelSchemas(false) == null) {
                    return selectSchemasForm();
                }
                return afterSelectSchemas();
            } else {
                SessionMessages.addErrorMessage(ElementsThreadLocals.getText("select.at.least.a.schema"));
                return selectSchemasForm();
            }
        }
        return selectSchemasForm();
    }

    protected boolean isAtLeastOneSchemaSelected() {
        boolean atLeastOneSelected = false;
        for(SelectableSchema schema : selectableSchemas) {
            if(schema.selected) {
                atLeastOneSelected = true;
                break;
            }
        }
        return atLeastOneSelected;
    }

    protected void updateModelFailed(Exception e) {
        logger.error("Could not update model", e);
        SessionMessages.addErrorMessage(
                ElementsThreadLocals.getText("could.not.save.model._", ExceptionUtils.getRootCauseMessage(e)));
        if(isNewConnectionProvider()) {
            persistence.getModel().getDatabases().remove(connectionProvider.getDatabase());
        }
        persistence.initModel();
    }

    protected Database configureModelSchemas(boolean alwaysUseExistingModel) {
        Model refModel;
        if(!alwaysUseExistingModel && isNewConnectionProvider()) {
            refModel = new Model();
        } else {
            refModel = persistence.getModel();
        }
        List<Schema> tempSchemas = new ArrayList<Schema>();
        Database database = connectionProvider.getDatabase();
        for(SelectableSchema schema : selectableSchemas) {
            Schema modelSchema = DatabaseLogic.findSchemaByName(database, schema.schemaName);
            if(schema.selected) {
                if(modelSchema == null) {
                    modelSchema = new Schema();
                    modelSchema.setSchemaName(schema.schemaName);
                    modelSchema.setDatabase(database);
                    database.getSchemas().add(modelSchema);
                    tempSchemas.add(modelSchema);
                }
            }
        }

        this.database = (Database) context.getRequest().getSession().getAttribute(databaseSessionKey);
        if(this.database != null) {
            return this.database;
        }

        Database targetDatabase;
        DatabaseSyncer dbSyncer = new DatabaseSyncer(connectionProvider);
        try {
            synchronized (persistence) {
                targetDatabase = dbSyncer.syncDatabase(refModel);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("error.in.database.synchronization._", e));
            return null;
        } finally {
            database.getSchemas().removeAll(tempSchemas);
            connectionProvider.setDatabase(database); //Restore
        }
        Model model = new Model();
        model.getDatabases().add(targetDatabase);
        model.init();
        this.database = targetDatabase;
        context.getRequest().getSession().setAttribute(databaseSessionKey, this.database);
        return targetDatabase;
    }

    public boolean isNewConnectionProvider() {
        return StringUtils.isEmpty(connectionProviderName);
    }

    public Resolution afterSelectSchemas() {
        children = ArrayListMultimap.create();
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
            FieldSet fieldSet = new FieldSet(ElementsThreadLocals.getText("users.and.groups.tables"), 1, mode);
            fieldSet.add(userTableField);
            fieldSet.add(groupTableField);
            fieldSet.add(userGroupTableField);
            userAndGroupTablesForm.add(fieldSet);
            //Handle back
            userAndGroupTablesForm.readFromRequest(context.getRequest());
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }

        return userManagementForm();
    }

    protected Resolution userManagementForm() {
        step = 2;
        return new ForwardResolution("/m/admin/wizard/user-management.jsp");
    }

    @Button(list = "user-management", key="next", order = 2, type = Button.TYPE_PRIMARY , icon = Button.ICON_RIGHT , iconBefore = false )
    public Resolution setupUserManagement() {
        selectSchemas();

        userAndGroupTablesForm.readFromRequest(context.getRequest());
        userAndGroupTablesForm.writeToObject(this);
        if(!StringUtils.isEmpty(userTableName)) {
            Model tmpModel = new Model();
            tmpModel.getDatabases().add(database);
            String[] name = DatabaseLogic.splitQualifiedTableName(userTableName);
            userTable = DatabaseLogic.findTableByName(tmpModel, name[0], name[1], name[2]);

            if(!StringUtils.isEmpty(groupTableName)) {
                name = DatabaseLogic.splitQualifiedTableName(groupTableName);
                groupTable = DatabaseLogic.findTableByName(tmpModel, name[0], name[1], name[2]);
            }

            if(!StringUtils.isEmpty(userGroupTableName)) {
                name = DatabaseLogic.splitQualifiedTableName(userGroupTableName);
                userGroupTable = DatabaseLogic.findTableByName(tmpModel, name[0], name[1], name[2]);
            }

            createUserManagementSetupForm();
            return selectUserFieldsForm();
        } else {
            return selectTablesForm();
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
                "plaintext:plaintext",
                ElementsThreadLocals.getText("plain.text"),
                true);
        algoSelectionProvider.appendRow(
                "MD5:base64",
                ElementsThreadLocals.getText("md5.base64.encoded"),
                true);
        algoSelectionProvider.appendRow(
                "MD5:hex",
                ElementsThreadLocals.getText("md5.hex.encoded"),
                true);
        algoSelectionProvider.appendRow(
                "SHA-1:base64",
                ElementsThreadLocals.getText("sha1.base64.encoded.portofino3"),
                true);
        algoSelectionProvider.appendRow(
                "SHA-1:hex",
                ElementsThreadLocals.getText("sha1.hex.encoded"),
                true);
        algoSelectionProvider.appendRow(
                "SHA-256:base64",
                ElementsThreadLocals.getText("sha256.base64.encoded"),
                true);
        algoSelectionProvider.appendRow(
                "SHA-256:hex",
                ElementsThreadLocals.getText("sha256.hex.encoded"),
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

            propertyAccessor = classAccessor.getProperty("userEmailProperty");
            Field userEmailPropertyField = new SelectField(propertyAccessor, userSelectionProvider, mode, "");
            userEmailPropertyField.setRequired(false);

            propertyAccessor = classAccessor.getProperty("userTokenProperty");
            Field userTokenPropertyField = new SelectField(propertyAccessor, userSelectionProvider, mode, "");
            userTokenPropertyField.setRequired(false);

            FieldSet uFieldSet = new FieldSet(ElementsThreadLocals.getText("users.table.setup"), 1, mode);
            uFieldSet.add(userIdPropertyField);
            uFieldSet.add(userNamePropertyField);
            uFieldSet.add(userPasswordPropertyField);
            uFieldSet.add(encryptionAlgorithmField);
            uFieldSet.add(userEmailPropertyField);
            uFieldSet.add(userTokenPropertyField);
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

                FieldSet gFieldSet = new FieldSet(ElementsThreadLocals.getText("groups.tables.setup"), 1, mode);
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

    @Button(list = "select-user-fields", key="next", order = 2, type = Button.TYPE_PRIMARY , icon = Button.ICON_RIGHT, iconBefore = false )
    public Resolution selectUserFields() {
        setupUserManagement();
        if(userTable != null) {
            userManagementSetupForm.readFromRequest(context.getRequest());
            if(userManagementSetupForm.validate()) {
                userManagementSetupForm.writeToObject(this);
                return selectTablesForm();
            } else {
                return selectUserFieldsForm();
            }
        } else {
            return selectTablesForm();
        }
    }

    protected Resolution selectUserFieldsForm() {
        step = 3;
        return new ForwardResolution("/m/admin/wizard/select-user-fields.jsp");
    }

    protected Resolution selectTablesForm() {
        step = (userTable == null) ? 3 : 4;
        setupCalendarField();
        return new ForwardResolution("/m/admin/wizard/select-tables.jsp");
    }

    protected void setupCalendarField() {
        ClassAccessor classAccessor = JavaClassAccessor.getClassAccessor(ApplicationWizard.class);
        try {
            generateCalendarField =
                    new BooleanField(classAccessor.getProperty("generateCalendar"), Mode.EDIT);
            generateCalendarField.setLabel(ElementsThreadLocals.getText("generate.a.calendar.page"));
            generateCalendarField.readFromObject(this);
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    protected List<Table> determineRoots(Multimap<Table, Reference> children, List<Table> allTables) {
        List<Table> roots = new ArrayList<Table>();
        for(SelectableSchema selectableSchema : selectableSchemas) {
            if(selectableSchema.selected) {
                Schema schema = DatabaseLogic.findSchemaByName(database, selectableSchema.schemaName);
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

            boolean removed = false; //Did we already remove the table from the list of roots?
            boolean selected = false; //Is the table selected as a root? Note that selected => known
            boolean known = false; //Is the table in the list of selectable roots?

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
                        if(column != null && column.getTable() != table) {
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

    @Button(list = "select-tables", key="next", order = 2, type = Button.TYPE_PRIMARY , icon = Button.ICON_RIGHT , iconBefore = false)
    public Resolution selectTables() {
        selectUserFields();

        rootsForm.readFromRequest(context.getRequest());
        rootsForm.writeToObject(selectableRoots);
        //Recalc roots
        afterSelectSchemas();

        if(roots.isEmpty()) {
            SessionMessages.addWarningMessage(ElementsThreadLocals.getText("no.page.will.be.generated"));
        }

        return buildAppForm();
    }

    @Button(list = "select-tables", key="previous", order = 1 , icon = Button.ICON_LEFT )
    public Resolution goBackFromSelectTables() {
        selectUserFields();
        if(userTable == null) {
            return userManagementForm();
        } else {
            return selectUserFieldsForm();
        }
    }

    protected Resolution buildAppForm() {
        step = (userTable == null) ? 4 : 5;
        setupCalendarField();
        generateCalendarField.readFromRequest(context.getRequest());
        generateCalendarField.writeToObject(this);
        return new ForwardResolution("/m/admin/wizard/build-app.jsp");
    }

    @Button(list = "build-app", key="previous", order = 1 , icon = Button.ICON_LEFT)
    public Resolution returnToSelectTables() {
        selectTables();
        return selectTablesForm();
    }

    @Button(list = "build-app", key="finish", order = 2, type = Button.TYPE_PRIMARY)
    public Resolution buildApplication() {
        selectTables();
        Database oldDatabase =
                DatabaseLogic.findDatabaseByName(persistence.getModel(), database.getDatabaseName());
        if(oldDatabase != null) {
            persistence.getModel().getDatabases().remove(oldDatabase);
        }
        persistence.getModel().getDatabases().add(database);
        connectionProvider.setDatabase(database);
        database.setConnectionProvider(connectionProvider);
        try {
            persistence.initModel();
        } catch (Exception e) {
            updateModelFailed(e);
            return buildAppForm();
        }
        if(!generationStrategy.equals("NO")) {
            if(generationStrategy.equals("AUTO")) {
                generateCalendar = true;
            }

            try {
                TemplateEngine engine = new SimpleTemplateEngine();
                Template template = engine.createTemplate(ApplicationWizard.class.getResource("CrudPage.groovy"));
                List<ChildPage> childPages = new ArrayList<ChildPage>();
                for(Table table : roots) {
                    File dir = new File(pagesDir, table.getActualEntityName());
                    depth = 1;
                    createCrudPage(dir, table, childPages, template);
                }
                if(userTable != null) {
                    setupUserPages(childPages, template);
                }
                if(generateCalendar) {
                    setupCalendar(childPages);
                }
                Page rootPage = DispatcherLogic.getPage(pagesDir);
                Collections.sort(childPages, new Comparator<ChildPage>() {
                    public int compare(ChildPage o1, ChildPage o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                });
                rootPage.getLayout().getChildPages().addAll(childPages);
                DispatcherLogic.savePage(pagesDir, rootPage);
            } catch (Exception e) {
                logger.error("Error while creating pages", e);
                SessionMessages.addErrorMessage(ElementsThreadLocals.getText("could.not.create.pages._", e));
                return buildAppForm();
            }
        }
        if(userTable != null) {
            setupUsers();
        }
        try {
            persistence.initModel();
            persistence.saveXmlModel();
        } catch (Exception e) {
            updateModelFailed(e);
            return buildAppForm();
        }
        if(userTable != null) {
            SecurityUtils.getSubject().logout();
            context.getRequest().getSession().invalidate();
            SessionMessages.addWarningMessage(ElementsThreadLocals.getText("user.management.has.been.configured.please.edit.security.groovy"));
            //ShiroUtils.clearCache(SecurityUtils.getSubject().getPrincipals());
        }
        XhtmlBuffer messageBuffer = new XhtmlBuffer();
        messageBuffer.writeNoHtmlEscape(ElementsThreadLocals.getText("application.created"));
        SessionMessages.addInfoMessage(messageBuffer);
        context.getRequest().getSession().removeAttribute(databaseSessionKey);
        return new RedirectResolution("/");
    }

    protected void setupCalendar(List<ChildPage> childPages) throws Exception {
        List<List<String>> calendarDefinitions = new ArrayList<List<String>>();
        Color[] colors = {
                Color.RED, new Color(64, 128, 255), Color.CYAN.darker(), Color.GRAY, Color.GREEN.darker(),
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
            String baseName = "calendar-" + connectionProvider.getDatabase().getDatabaseName();
            File dir = new File(pagesDir, baseName);
            int retries = 1;
            while(dir.exists()) {
                retries++;
                dir = new File(pagesDir, baseName + "-" + retries);
            }
            if(dir.mkdirs()) {
                CalendarConfiguration configuration = new CalendarConfiguration();
                DispatcherLogic.saveConfiguration(dir, configuration);

                Page page = new Page();
                page.setId(RandomUtil.createRandomId());
                String calendarTitle = "Calendar (" + connectionProvider.getDatabase().getDatabaseName() + ")";
                if(retries > 1) {
                    calendarTitle += " - " + retries;
                }
                page.setTitle(calendarTitle);
                page.setDescription(calendarTitle);

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
                        ElementsThreadLocals.getText("couldnt.create.directory", dir.getAbsolutePath()));
            }
        }
    }

    protected void setupUserPages(List<ChildPage> childPages, Template template) throws Exception {
        if(!roots.contains(userTable)) {
            File dir = new File(pagesDir, userTable.getActualEntityName());
            depth = 1;
            createCrudPage(dir, userTable, childPages, template);
        }

        Configuration conf = portofinoConfiguration;
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
                        " = %{#securityUtils.primaryPrincipal.id}" +
                        " order by id desc";
                String dirName = "my-" + entityName;
                boolean multipleRoles = isMultipleRoles(fromTable, ref, references);
                if(multipleRoles) {
                    dirName += "-as-" + linkToUserProperty;
                }
                File dir = new File(pagesDir, dirName);
                String title = Util.guessToWords(dirName);

                Map<String, String> bindings = new HashMap<String, String>();
                bindings.put("parentName", "securityUtils");
                bindings.put("parentProperty", "primaryPrincipal.id");
                bindings.put("linkToParentProperty", linkToUserProperty);

                Page page = createCrudPage(
                        dir, fromTable, childQuery,
                        childPages, template, bindings, title);
                if(page != null) {
                    Group group = new Group();
                    group.setName(SecurityLogic.getAnonymousGroup(conf));
                    group.setAccessLevel(AccessLevel.DENY.name());
                    Permissions permissions = new Permissions();
                    permissions.getGroups().add(group);
                    page.setPermissions(permissions);
                    DispatcherLogic.savePage(dir, page);
                }
            }
        }
    }

    protected void setupUsers() {
        try {
            TemplateEngine engine = new SimpleTemplateEngine();
            Template secTemplate = engine.createTemplate(ApplicationWizard.class.getResource("Security.groovy"));
            Map<String, String> bindings = new HashMap<String, String>();
            bindings.put("databaseName", connectionProvider.getDatabase().getDatabaseName());
            bindings.put("userTableEntityName", userTable.getActualEntityName());
            bindings.put("userIdProperty", userIdProperty);
            bindings.put("userNameProperty", userNameProperty);
            bindings.put("passwordProperty", userPasswordProperty);
            bindings.put("userEmailProperty", userEmailProperty);
            bindings.put("userTokenProperty", userTokenProperty);

            bindings.put("groupTableEntityName",
                    groupTable != null ? groupTable.getActualEntityName() : "");
            bindings.put("groupIdProperty", StringUtils.defaultString(groupIdProperty));
            bindings.put("groupNameProperty", StringUtils.defaultString(groupNameProperty));

            bindings.put("userGroupTableEntityName",
                    userGroupTable != null ? userGroupTable.getActualEntityName() : "");
            bindings.put("groupLinkProperty", StringUtils.defaultString(groupLinkProperty));
            bindings.put("userLinkProperty", StringUtils.defaultString(userLinkProperty));
            bindings.put("adminGroupName", StringUtils.defaultString(adminGroupName));

            bindings.put("hashIterations", "1");
            String[] algoAndEncoding = encryptionAlgorithm.split(":");
            bindings.put("hashAlgorithm", '"' + algoAndEncoding[0] + '"');
            if(algoAndEncoding[1].equals("plaintext")) {
                bindings.put("hashFormat", "null");
            } else if(algoAndEncoding[1].equals("hex")) {
                bindings.put("hashFormat", "new org.apache.shiro.crypto.hash.format.HexFormat()");
            } else if(algoAndEncoding[1].equals("base64")) {
                bindings.put("hashFormat", "new org.apache.shiro.crypto.hash.format.Base64Format()");
            }
            File gcp = (File) context.getServletContext().getAttribute(BaseModule.GROOVY_CLASS_PATH);
            FileWriter fw = new FileWriter(new File(gcp, "Security.groovy"));
            secTemplate.make(bindings).writeTo(fw);
            IOUtils.closeQuietly(fw);
        } catch (Exception e) {
            logger.warn("Couldn't configure users", e);
            SessionMessages.addWarningMessage(ElementsThreadLocals.getText("couldnt.set.up.user.management._", e));
        }
    }

    private boolean isMultipleRoles(Table fromTable, Reference ref, Collection<Reference> references) {
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
        String query = "from " + table.getActualEntityName() + " order by id desc";
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
                        ElementsThreadLocals.getText("directory.exists.page.not.created._", dir.getAbsolutePath()));
            return null;
        } else if(dir.mkdirs()) {
            logger.info("Creating CRUD page {}", dir.getAbsolutePath());
            CrudConfiguration configuration = new CrudConfiguration();
            configuration.setDatabase(connectionProvider.getDatabase().getDatabaseName());
            configuration.setupDefaults();

            configuration.setQuery(query);
            String variable = table.getActualEntityName();
            configuration.setVariable(variable);
            detectLargeResultSet(table, configuration);

            configuration.setName(table.getActualEntityName());

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

            Collection<Reference> references = children.get(table);
            if(references != null && depth < maxDepth) {
                ArrayList<ChildPage> pages = page.getDetailLayout().getChildPages();
                depth++;
                for(Reference ref : references) {
                    createChildCrudPage(dir, template, variable, references, ref, pages);
                }
                depth--;
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

            logger.debug("Creating _detail directory");
            File detailDir = new File(dir, PageInstance.DETAIL);
            if(!detailDir.isDirectory() && !detailDir.mkdir()) {
                logger.warn("Could not create detail directory {}", detailDir.getAbsolutePath());
                SessionMessages.addWarningMessage(
                    ElementsThreadLocals.getText("couldnt.create.directory", detailDir.getAbsolutePath()));
            }

            ChildPage childPage = new ChildPage();
            childPage.setName(dir.getName());
            childPage.setShowInNavigation(true);
            childPages.add(childPage);

            return page;
        } else {
            logger.warn("Couldn't create directory {}", dir.getAbsolutePath());
            SessionMessages.addWarningMessage(
                    ElementsThreadLocals.getText("couldnt.create.directory", dir.getAbsolutePath()));
            return null;
        }
    }

    public static final int MULTILINE_THRESHOLD = 256;

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
        boolean inPk = DatabaseLogic.isInPk(column);
        boolean inFk = DatabaseLogic.isInFk(column);
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
           Number.class.isAssignableFrom(column.getActualJavaType()) &&
           !column.isAutoincrement()) {
            for(PrimaryKeyColumn pkc : table.getPrimaryKey().getPrimaryKeyColumns()) {
                if(pkc.getActualColumn().equals(column)) {
                    pkc.setGenerator(new IncrementGenerator(pkc));
                    insertable = false;
                    break;
                }
            }
        }

        if(propertyIsUserPassword) {
            Annotation annotation = DatabaseLogic.findAnnotation(column, Password.class);
            if(annotation == null) {
                column.getAnnotations().add(new Annotation(column, Password.class.getName()));
            }
            insertable = false;
            updatable = false;
        }

        if(!propertyIsUserPassword &&
           column.getActualJavaType() == String.class &&
           (column.getLength() == null || column.getLength() > MULTILINE_THRESHOLD) &&
           isNewConnectionProvider()) {
            Annotation annotation = DatabaseLogic.findAnnotation(column, Multiline.class);
            if(annotation == null) {
                annotation = new Annotation(column, Multiline.class.getName());
                annotation.getValues().add("true");
                column.getAnnotations().add(annotation);
            }
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

    protected boolean isUnsupportedProperty(Column column) {
        //I blob su db non sono supportati al momento
        return column.getJdbcType() == Types.BLOB || column.getJdbcType() == Types.LONGVARBINARY;
    }

    protected final Set<Column> detectedBooleanColumns = new HashSet<Column>();

    protected void detectBooleanColumn(Table table, Column column) {
        if(detectedBooleanColumns.contains(column)) {
            return;
        }
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
                    "select count(" + implementation.escapeColumnName(null, null, null, column.getColumnName()) + ") " +
                    "from " + implementation.escapeTableName(null, table.getSchemaName(), table.getTableName());
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
                    "select distinct(" + implementation.escapeColumnName(null, null, null, column.getColumnName()) + ") " +
                    "from " + implementation.escapeTableName(null, table.getSchemaName(), table.getTableName());
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
                logger.debug("Could not determine whether column " + column.getQualifiedName() + " is boolean", e);
                logger.info("Could not determine whether column " + column.getQualifiedName() + " is boolean");
            } finally {
                try {
                    if(connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    logger.error("Could not close connection", e);
                }
            }
            detectedBooleanColumns.add(column);
        }
    }

    protected final Map<Table, Boolean> largeResultSet = new HashMap<Table, Boolean>();

    protected void detectLargeResultSet(Table table, CrudConfiguration configuration) {
        Boolean lrs = largeResultSet.get(table);
        if(lrs != null) {
            configuration.setLargeResultSet(lrs);
            return;
        }

        Connection connection = null;
        try {
            logger.info("Trying to detect whether table {} has many records...", table.getQualifiedName());
            connection = connectionProvider.acquireConnection();
            liquibase.database.Database implementation =
                        DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            String sql =
                    "select count(*) from " + implementation.escapeTableName(null, table.getSchemaName(), table.getTableName());
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
        largeResultSet.put(table, configuration.isLargeResultSet());
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
            File dir, Template template, String parentName, Collection<Reference> references,
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
                " = %{#" + parentName + "." + parentProperty + "}" +
                " order by id desc";
        String childDirName = entityName;
        boolean multipleRoles = isMultipleRoles(fromTable, ref, references);
        if(multipleRoles) {
            childDirName += "-as-" + linkToParentProperty;
        }
        File childDir = new File(new File(dir, PageInstance.DETAIL), childDirName);
        String childTitle = Util.guessToWords(childDirName);

        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("parentName", parentName);
        bindings.put("parentProperty", parentProperty);
        bindings.put("linkToParentProperty", linkToParentProperty);

        createCrudPage(
                childDir, fromTable, childQuery,
                pages, template, bindings, childTitle);
    }

    public SelectField getConnectionProviderField() {
        return connectionProviderField;
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

    public String getConnectionProviderName() {
        return connectionProviderName;
    }

    public void setConnectionProviderName(String connectionProviderName) {
        this.connectionProviderName = connectionProviderName;
    }

    public boolean isJdbc() {
        return connectionProviderType == null || connectionProviderType.equals(JDBC);
    }

    public boolean isJndi() {
        return StringUtils.equals(connectionProviderType, JNDI);
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

    @LabelI18N("users.table")
    public String getUserTableName() {
        return userTableName;
    }

    public void setUserTableName(String userTableName) {
        this.userTableName = userTableName;
    }

    @LabelI18N("groups.table")
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

    @LabelI18N("username.property")
    public String getUserNameProperty() {
        return userNameProperty;
    }

    public void setUserNameProperty(String userNameProperty) {
        this.userNameProperty = userNameProperty;
    }

    @LabelI18N("email.property")
    public String getUserEmailProperty() {
        return userEmailProperty;
    }

    public void setUserEmailProperty(String userEmailProperty) {
        this.userEmailProperty = userEmailProperty;
    }

    @LabelI18N("token.property")
    public String getUserTokenProperty() {
        return userTokenProperty;
    }

    public void setUserTokenProperty(String userTokenProperty) {
        this.userTokenProperty = userTokenProperty;
    }

    @LabelI18N("user.id.property")
    public String getUserIdProperty() {
        return userIdProperty;
    }

    public void setUserIdProperty(String userIdProperty) {
        this.userIdProperty = userIdProperty;
    }

    @LabelI18N("password.property")
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

    @LabelI18N("user-group.join.table")
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

    @LabelI18N("property.that.links.to.group")
    public String getGroupLinkProperty() {
        return groupLinkProperty;
    }

    public void setGroupLinkProperty(String groupLinkProperty) {
        this.groupLinkProperty = groupLinkProperty;
    }

    @LabelI18N("property.that.links.to.user")
    public String getUserLinkProperty() {
        return userLinkProperty;
    }

    public void setUserLinkProperty(String userLinkProperty) {
        this.userLinkProperty = userLinkProperty;
    }

    @LabelI18N("name.of.the.administrators.group")
    public String getAdminGroupName() {
        return adminGroupName;
    }

    public void setAdminGroupName(String adminGroupName) {
        this.adminGroupName = adminGroupName;
    }

    @LabelI18N("password.encryption.algorithm")
    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public String getGenerationStrategy() {
        return generationStrategy;
    }

    public void setGenerationStrategy(String generationStrategy) {
        this.generationStrategy = generationStrategy;
    }

    public boolean isGenerateCalendar() {
        return generateCalendar;
    }

    public void setGenerateCalendar(boolean generateCalendar) {
        this.generateCalendar = generateCalendar;
    }

    public BooleanField getGenerateCalendarField() {
        return generateCalendarField;
    }

    public Persistence getPersistence() {
        return persistence;
    }

    //Wizard implementation

    public static class Step {
        public final String number;
        public final String title;

        public Step(String number, String title) {
            this.number = number;
            this.title = title;
        }

        public String getNumber() {
            return number;
        }

        public String getTitle() {
            return title;
        }
    }

    @Override
    public Resolution preparePage() {
        return null;
    }

    public List<Step> getSteps() {
        List<Step> steps = new ArrayList<Step>();
        steps.add(new Step("1", ElementsThreadLocals.getText("connect.to.your.database")));
        steps.add(new Step("2", ElementsThreadLocals.getText("select.the.database.schemas.to.import")));
        steps.add(new Step("3", ElementsThreadLocals.getText("set.up.user.management")));
        if(userTable != null) {
            steps.add(new Step("3a", ElementsThreadLocals.getText("customize.user.management")));
        }
        steps.add(new Step("4", ElementsThreadLocals.getText("generate.pages")));
        steps.add(new Step("5", ElementsThreadLocals.getText("build.the.application")));
        return steps;
    }

    public int getCurrentStepIndex() {
        return step;
    }
}
