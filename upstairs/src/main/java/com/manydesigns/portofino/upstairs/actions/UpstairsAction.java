package com.manydesigns.portofino.upstairs.actions;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.elements.annotations.Password;
import com.manydesigns.elements.messages.RequestMessages;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.actions.ActionDescriptor;
import com.manydesigns.portofino.actions.ActionLogic;
import com.manydesigns.portofino.actions.Group;
import com.manydesigns.portofino.actions.Permissions;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.resourceactions.AbstractResourceAction;
import com.manydesigns.portofino.resourceactions.ActionInstance;
import com.manydesigns.portofino.resourceactions.crud.configuration.CrudProperty;
import com.manydesigns.portofino.resourceactions.crud.configuration.database.CrudConfiguration;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.security.SecurityLogic;
import com.manydesigns.portofino.spring.PortofinoContextLoaderListener;
import com.manydesigns.portofino.upstairs.ModuleInfo;
import com.manydesigns.portofino.upstairs.actions.support.TableInfo;
import com.manydesigns.portofino.upstairs.actions.support.WizardInfo;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.*;
import java.util.*;

import static com.manydesigns.portofino.modules.ResourceActionsModule.ACTIONS_DIRECTORY;

/**
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAuthentication
@RequiresAdministrator
public class UpstairsAction extends AbstractResourceAction {
    public static final String copyright = "Copyright (C) 2005-2020 ManyDesigns srl";

    public final static Logger logger = LoggerFactory.getLogger(UpstairsAction.class);

    @Autowired
    ConfigurableApplicationContext applicationContext;

    @Autowired
    Persistence persistence;

    @Autowired
    @Qualifier(ACTIONS_DIRECTORY)
    FileObject actionsDirectory;

    @SuppressWarnings({"RedundantStringConstructorCall"})
    public static final String NO_LINK_TO_PARENT = new String();
    public static final int LARGE_RESULT_SET_THRESHOLD = 10000;
    public static final int MULTILINE_THRESHOLD = 256;
    protected int maxDepth = 5;
    protected int maxColumnsInSummary = 5;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("version", PortofinoProperties.getPortofinoVersion());
        List<ModuleInfo> modules = new ArrayList<>();
        for(Module module : applicationContext.getBeansOfType(Module.class).values()) {
            ModuleInfo view = new ModuleInfo();
            view.moduleClass = module.getClass().getName();
            view.name = module.getName();
            view.status = module.getStatus().name();
            view.version = module.getModuleVersion();
            modules.add(view);
        }
        info.put("modules", modules);
        return info;
    }

    @POST
    @Path("restart")
    public void restartApplication() throws Exception {
        codeBase.clear();
        OgnlUtils.clearCache();
        ServletContext servletContext = context.getServletContext();
        PortofinoContextLoaderListener.get(servletContext).refresh();
    }

    @POST
    @Path("application")
    public List<Map> createApplication(WizardInfo wizard) throws Exception {
        List<Map> createdPages = new ArrayList<>();
        String strategy = wizard.strategy;
        switch (strategy) {
            case "automatic":
            case "manual":
                String databaseName = (String) (wizard.connectionProvider).get("name");
                List<TableInfo> tables = wizard.tables;
                Database database = DatabaseLogic.findDatabaseByName(persistence.getModel(), databaseName);
                if(database == null) {
                    throw new WebApplicationException("The database does not exist: " + databaseName);
                }
                TemplateEngine engine = new SimpleTemplateEngine();
                Template template = engine.createTemplate(
                        UpstairsAction.class.getResource("/com/manydesigns/portofino/upstairs/wizard/CrudAction.groovy"));
                Table userTable = getTable(wizard.usersTable);
                Column userPasswordColumn = getColumn(userTable, wizard.userPasswordProperty);
                boolean userCrudCreated = false;
                for(TableInfo tableInfo : tables) {
                    if(tableInfo.selected) {
                        Table tableRef = tableInfo.table;
                        String tableName = tableRef.getTableName();
                        Table table = DatabaseLogic.findTableByName(persistence.getModel(), databaseName, tableInfo.schema, tableName);
                        if(table == null) {
                            logger.warn("Table not found: {}", tableRef.getQualifiedName());
                            RequestMessages.addErrorMessage("Table not found: " + tableRef.getQualifiedName());
                            continue;
                        }
                        if(table == userTable) {
                            userCrudCreated = true;
                        }
                        FileObject dir = actionsDirectory.resolveFile(table.getActualEntityName());
                        createCrudAction(database.getConnectionProvider(), dir, table, template, userTable, userPasswordColumn, createdPages);
                    }
                }
                if(userTable != null) {
                    if(!userCrudCreated) {
                        FileObject dir = actionsDirectory.resolveFile(userTable.getActualEntityName());
                        createCrudAction(database.getConnectionProvider(), dir, userTable, template, userTable, userPasswordColumn, createdPages);
                    }
                    setupUserPages(database.getConnectionProvider(), template, userTable, createdPages);
                }
                break;
            case "none":
                break;
            default:
                throw new WebApplicationException("Invalid strategy: " + strategy);
        }
        return createdPages;
    }

    @POST
    @Path("application/security")
    public void createSecurityGrooyv(WizardInfo wizard) {
        String databaseName = (String) (wizard.connectionProvider).get("name");
        Database database = DatabaseLogic.findDatabaseByName(persistence.getModel(), databaseName);
        if(database == null) {
            throw new WebApplicationException("The database does not exist: " + databaseName);
        }
        Table userTable = getTable(wizard.usersTable);
        if(userTable != null) {
            try {
                setupSecurityGroovy(database.getConnectionProvider(), userTable, wizard);
            } catch (Exception e) {
                logger.error("Couldn't configure users", e);
                throw new WebApplicationException(e);
            }
        }
    }

    @Nullable
    public Column getColumn(Table table, Column column) {
        if (table != null && column != null) {
            return DatabaseLogic.findColumnByName(table, column.getColumnName());
        } else {
            return null;
        }
    }

    protected Table getTable(TableInfo tableInfo) {
        if(tableInfo == null) {
            return null;
        }
        return DatabaseLogic.findTableByName(persistence.getModel(), tableInfo.database, tableInfo.schema, tableInfo.table.getTableName());
    }

    protected ActionDescriptor createCrudAction(
            ConnectionProvider connectionProvider, FileObject dir, Table table, Template template,
            Table userTable, Column userPasswordColumn, List<Map> createdPages) throws Exception {
        String query = "from " + table.getActualEntityName() + " order by id desc";
        HashMap<String, String> bindings = new HashMap<>();
        bindings.put("generatedClassName",  "CrudAction_" + RandomUtil.createRandomId());
        bindings.put("parentName", "");
        bindings.put("parentProperty", "nothing");
        bindings.put("linkToParentProperty", NO_LINK_TO_PARENT);
        return createCrudAction(connectionProvider, dir, table, query, template, bindings, userTable, userPasswordColumn, createdPages, 1);
    }

    protected ActionDescriptor createCrudAction(
            ConnectionProvider connectionProvider,
            FileObject dir, Table table, String query,
            Template template, Map<String, String> bindings, Table userTable, Column userPasswordColumn, List<Map> createdPages, int depth)
            throws Exception {
        if(dir.exists()) {
            RequestMessages.addWarningMessage(
                    ElementsThreadLocals.getText("directory.exists.page.not.created._", dir.getName().getPath()));
        } else {
            dir.createFolder();
            logger.info("Creating CRUD action {}", dir.getName().getPath());
            CrudConfiguration configuration = new CrudConfiguration();
            configuration.setDatabase(table.getDatabaseName());
            configuration.setupDefaults();
            configuration.setQuery(query);
            String variable = table.getActualEntityName();
            configuration.setVariable(variable);
            detectLargeResultSet(connectionProvider, table, configuration);

            configuration.setName(table.getActualEntityName());

            int summ = 0;
            String linkToParentProperty = bindings.get("linkToParentProperty");
            for(Column column : table.getColumns()) {
                summ = setupColumn(connectionProvider, column, configuration, summ, linkToParentProperty, column.equals(userPasswordColumn));
            }

            ActionLogic.saveConfiguration(dir, configuration);
            ActionDescriptor action = new ActionDescriptor();
            ActionLogic.saveActionDescriptor(dir, action);
            FileObject actionFile = dir.resolveFile("action.groovy");
            try(Writer fileWriter = new OutputStreamWriter(actionFile.getContent().getOutputStream())) {
                template.make(bindings).writeTo(fileWriter);
            }

            logger.debug("Creating _detail directory");
            FileObject detailDir = dir.resolveFile(ActionInstance.DETAIL);
            if(detailDir.exists() && detailDir.getType() != FileType.FOLDER) {
                logger.warn("Invalid detail directory {}", detailDir.getName().getPath());
                RequestMessages.addWarningMessage(
                        ElementsThreadLocals.getText("invalid.detail.directory", detailDir.getName().getPath()));
            } else {
                detailDir.createFolder();
            }

            String path = dir.getName().getBaseName();
            FileObject parent = dir.getParent().getParent(); //two because of _detail
            for(int i = 1; i < depth; i++) {
                path = parent.getName().getBaseName() + "/" + ActionInstance.DETAIL + "/" + path;
                parent = parent.getParent().getParent();
            }
            Map<String, Object> pageInfo = new HashMap<>();
            pageInfo.put("path", path);
            pageInfo.put("detail", depth > 1);
            pageInfo.put("type", "crud");
            pageInfo.put("title", Util.guessToWords(dir.getName().getBaseName()));
            createdPages.add(pageInfo);

            if(depth < maxDepth) {
                List<Reference> children = computeChildren(table);
                for(Reference ref : children) {
                    createChildCrudAction(connectionProvider, dir, template, variable, children, ref, userTable, userPasswordColumn, createdPages, depth);
                }
            }
            return action;
        }
        return null;
    }

    protected List<Reference> computeChildren(Table table) {
        List<Reference> children = new ArrayList<>();
        table.getSchema().getDatabase().getAllTables().forEach(t -> {
            if(t.equals(table)) {
                return; //Skip self references
            }
            t.getForeignKeys().forEach(f -> {
                if(f.getToTable().equals(table)) {
                    children.addAll(f.getReferences());
                }
            });
            t.getSelectionProviders().forEach(p -> {
                for(Reference ref : p.getReferences()) {
                    Column column = ref.getActualToColumn();
                    if(column != null && column.getTable().equals(table)) {
                        children.add(ref);
                    }
                }
            });
        });
        return children;
    }

    protected void createChildCrudAction(
            ConnectionProvider connectionProvider,
            FileObject dir, Template template, String parentName, Collection<Reference> references,
            Reference ref, Table userTable, Column userPasswordColumn, List<Map> createdPages, int depth)
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
        if (multipleRoles) {
            childDirName += "-as-" + linkToParentProperty;
        }
        FileObject childDir = dir.resolveFile(ActionInstance.DETAIL).resolveFile(childDirName);

        Map<String, String> bindings = new HashMap<>();
        bindings.put("generatedClassName",  "CrudAction_" + RandomUtil.createRandomId());
        bindings.put("parentName", parentName);
        bindings.put("parentProperty", parentProperty);
        bindings.put("linkToParentProperty", linkToParentProperty);

        createCrudAction(
                connectionProvider, childDir, fromTable, childQuery, template, bindings, userTable, userPasswordColumn, createdPages, depth + 1);
    }

    protected boolean isMultipleRoles(Table fromTable, Reference ref, Collection<Reference> references) {
        boolean multipleRoles = false;
        for (Reference ref2 : references) {
            if (ref2 != ref && ref2.getActualFromColumn().getTable().equals(fromTable)) {
                multipleRoles = true;
                break;
            }
        }
        return multipleRoles;
    }

    protected int setupColumn
            (ConnectionProvider connectionProvider, Column column, CrudConfiguration configuration,
             int columnsInSummary, String linkToParentProperty, boolean isPassword) {

        if (column.getActualJavaType() == null) {
            logger.debug("Column without a javaType, skipping: {}", column.getQualifiedName());
            return columnsInSummary;
        }

        Table table = column.getTable();
        @SuppressWarnings({"StringEquality"})
        boolean enabled =
                !(linkToParentProperty != NO_LINK_TO_PARENT &&
                        column.getActualPropertyName().equals(linkToParentProperty))
                        && !isUnsupportedProperty(column);
        boolean inPk = DatabaseLogic.isInPk(column);
        boolean inFk = DatabaseLogic.isInFk(column);
        boolean inSummary =
                enabled &&
                        (inPk || columnsInSummary < maxColumnsInSummary) &&
                        !isPassword;
        boolean updatable = enabled && !column.isAutoincrement() && !inPk;
        boolean insertable = enabled && !column.isAutoincrement();

        if (!configuration.isLargeResultSet()) {
            detectBooleanColumn(connectionProvider, table, column);
        }

        if (enabled && inPk && !inFk &&
                Number.class.isAssignableFrom(column.getActualJavaType()) &&
                !column.isAutoincrement()) {
            for (PrimaryKeyColumn pkc : table.getPrimaryKey().getPrimaryKeyColumns()) {
                if (pkc.getActualColumn().equals(column)) {
                    pkc.setGenerator(new IncrementGenerator(pkc));
                    insertable = false;
                    break;
                }
            }
        }

        if (isPassword) {
            Annotation annotation = DatabaseLogic.findAnnotation(column, Password.class);
            if (annotation == null) {
                column.getAnnotations().add(new Annotation(column, Password.class.getName()));
            }
            insertable = false;
            updatable = false;
        }

        if (!isPassword &&
                column.getActualJavaType() == String.class &&
                (column.getLength() == null || column.getLength() > MULTILINE_THRESHOLD)) {
            Annotation annotation = DatabaseLogic.findAnnotation(column, Multiline.class);
            if (annotation == null) {
                annotation = new Annotation(column, Multiline.class.getName());
                annotation.setProperty("value", "true");
                column.getAnnotations().add(annotation);
            }
        }

        CrudProperty crudProperty = new CrudProperty();
        crudProperty.setEnabled(enabled);
        crudProperty.setName(column.getActualPropertyName());
        crudProperty.setInsertable(insertable);
        crudProperty.setUpdatable(updatable);
        if (inSummary) {
            crudProperty.setInSummary(true);
            crudProperty.setSearchable(true);
            columnsInSummary++;
        }
        configuration.getProperties().add(crudProperty);

        return columnsInSummary;
    }

    protected boolean isUnsupportedProperty(Column column) {
        //TODO actually we do support database blobs now!
        return column.getJdbcType() == Types.BLOB || column.getJdbcType() == Types.LONGVARBINARY;
    }

    protected final Set<Column> detectedBooleanColumns = new HashSet<>();

    protected void detectBooleanColumn(ConnectionProvider connectionProvider, Table table, Column column) {
        if(detectedBooleanColumns.contains(column)) {
            return;
        }
        if(column.getJdbcType() == Types.INTEGER || column.getJdbcType() == Types.DECIMAL || column.getJdbcType() == Types.NUMERIC) {
            logger.info(
                    "Detecting whether numeric column " + column.getQualifiedName() + " is boolean by examining " +
                            "its values...");

            //Detect booleans
            Connection connection = null;

            try {
                connection = connectionProvider.acquireConnection();
                liquibase.database.Database implementation =
                        DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
                String sql =
                        "select count(" + implementation.escapeColumnName(null, null, null, column.getColumnName()) + ") " +
                                "from " + implementation.escapeTableName(null, table.getSchemaName(), table.getTableName());
                PreparedStatement statement = connection.prepareStatement(sql);
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
                statement = connection.prepareStatement(sql);
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

    protected void detectLargeResultSet(ConnectionProvider connectionProvider, Table table, CrudConfiguration configuration) {
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

    protected void setupUserPages(
            ConnectionProvider connectionProvider, Template template, Table userTable, List<Map> createdPages) throws Exception {
        List<Reference> references = computeChildren(userTable);
        if(references != null) {
            for(Reference ref : references) {
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
                FileObject dir = actionsDirectory.resolveFile(dirName);

                Map<String, String> bindings = new HashMap<>();
                bindings.put("generatedClassName",  "UserManagementCrudAction");
                bindings.put("parentName", "securityUtils");
                bindings.put("parentProperty", "primaryPrincipal.id");
                bindings.put("linkToParentProperty", linkToUserProperty);

                ActionDescriptor action = createCrudAction(
                        connectionProvider, dir, fromTable, childQuery, template, bindings, null, null,
                        createdPages, 1);
                if(action != null) {
                    Group group = new Group();
                    group.setName(SecurityLogic.getAnonymousGroup(portofinoConfiguration));
                    group.setAccessLevel(AccessLevel.DENY.name());
                    Permissions permissions = new Permissions();
                    permissions.getGroups().add(group);
                    action.setPermissions(permissions);
                    ActionLogic.saveActionDescriptor(dir, action);
                }
            }
        }
    }

    protected void setupSecurityGroovy(ConnectionProvider connectionProvider, Table userTable, WizardInfo wizard) throws Exception {
        TemplateEngine engine = new SimpleTemplateEngine();
        Template template = engine.createTemplate(
                UpstairsAction.class.getResource("/com/manydesigns/portofino/upstairs/wizard/Security.groovy"));
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("databaseName", connectionProvider.getDatabase().getDatabaseName());
        bindings.put("userTableEntityName", userTable.getActualEntityName());
        bindings.put("userIdProperty", getPropertyName(userTable, wizard.userIdProperty));
        bindings.put("userNameProperty", getPropertyName(userTable, wizard.userNameProperty));
        bindings.put("passwordProperty", getPropertyName(userTable, wizard.userPasswordProperty));
        bindings.put("userEmailProperty", StringUtils.defaultString(getPropertyName(userTable, wizard.userEmailProperty)));
        bindings.put("userTokenProperty", StringUtils.defaultString(getPropertyName(userTable, wizard.userTokenProperty)));

        Table groupsTable = getTable(wizard.groupsTable);
        bindings.put("groupTableEntityName", groupsTable != null ? groupsTable.getActualEntityName() : "");
        bindings.put("groupIdProperty", StringUtils.defaultString(getPropertyName(groupsTable, wizard.groupIdProperty)));
        bindings.put("groupNameProperty", StringUtils.defaultString(getPropertyName(groupsTable, wizard.groupNameProperty)));

        Table userGroupTable = getTable(wizard.userGroupTable);
        bindings.put("userGroupTableEntityName",
                userGroupTable != null ? userGroupTable.getActualEntityName() : "");
        bindings.put("groupLinkProperty", StringUtils.defaultString(getPropertyName(userGroupTable, wizard.groupLinkProperty)));
        bindings.put("userLinkProperty", StringUtils.defaultString(getPropertyName(userGroupTable, wizard.userLinkProperty)));
        bindings.put("adminGroupName", StringUtils.defaultString(wizard.adminGroupName));

        bindings.put("hashIterations", "1");
        String[] algoAndEncoding = wizard.encryptionAlgorithm.split(":");
        bindings.put("hashAlgorithm", '"' + algoAndEncoding[0] + '"');
        switch (algoAndEncoding[1]) {
            case "plaintext":
                bindings.put("hashFormat", "null");
                break;
            case "hex":
                bindings.put("hashFormat", "new org.apache.shiro.crypto.hash.format.HexFormat()");
                break;
            case "base64":
                bindings.put("hashFormat", "new org.apache.shiro.crypto.hash.format.Base64Format()");
                break;
            default:
                throw new IllegalArgumentException("Unsupported encoding: " + algoAndEncoding[1]);
        }
        FileObject codeBaseRoot = actionsDirectory.getParent().resolveFile("classes");
        FileObject securityGroovyFile = codeBaseRoot.resolveFile("Security.groovy");
        try(Writer fw = new OutputStreamWriter(securityGroovyFile.getContent().getOutputStream())) {
            template.make(bindings).writeTo(fw);
            logger.info("Security.groovy written to " + securityGroovyFile.getParent().getName().getPath());
        }
    }

    protected String getPropertyName(Table table, Column column) {
        column = getColumn(table, column);
        if(column == null) {
            return null;
        } else {
            return column.getActualPropertyName();
        }
    }

}
