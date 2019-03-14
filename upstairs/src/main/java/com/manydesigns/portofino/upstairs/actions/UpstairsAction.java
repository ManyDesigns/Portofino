package com.manydesigns.portofino.upstairs.actions;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.elements.annotations.Password;
import com.manydesigns.elements.messages.RequestMessages;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageInstance;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudProperty;
import com.manydesigns.portofino.pageactions.crud.configuration.database.CrudConfiguration;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.pages.PageLogic;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.upstairs.ModuleInfo;
import com.manydesigns.portofino.upstairs.actions.support.TableInfo;
import com.manydesigns.portofino.upstairs.actions.support.WizardInfo;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.util.*;

import static com.manydesigns.portofino.spring.PortofinoSpringConfiguration.ACTIONS_DIRECTORY;

/**
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAuthentication
@RequiresAdministrator
public class UpstairsAction extends AbstractPageAction {
    public static final String copyright = "Copyright (C) 2005-2017 ManyDesigns srl";

    public final static Logger logger = LoggerFactory.getLogger(UpstairsAction.class);

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CodeBase codeBase;

    @Autowired
    Persistence persistence;

    @Autowired
    @Qualifier(ACTIONS_DIRECTORY)
    File actionsDirectory;

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
        if(applicationContext instanceof ConfigurableApplicationContext) {
            //Spring enhances @Configuration classes. To do so it loads them by name from its classloader.
            //Thus, replacing the classloader with a fresh one has also the side-effect of making Spring reload the user
            //SpringConfiguration class, provided it already existed and was annotated with @Configuration.
            //Note that Spring won't pick up new @Bean methods. It will also barf badly on removed @Bean methods,
            //effectively crashing the application. Changing the return value and even the return type is fine.
            ((DefaultResourceLoader) applicationContext).setClassLoader(codeBase.asClassLoader());
            ((ConfigurableApplicationContext) applicationContext).refresh();
        }
    }

    @POST
    @Path("application")
    public void createApplication(WizardInfo wizard) throws Exception {
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
                Template template = engine.createTemplate(UpstairsAction.class.getResource("/com/manydesigns/portofino/upstairs/wizard/CrudPage.groovy"));
                for(TableInfo tableInfo : tables) {
                    if(tableInfo.selected) {
                        Table tableRef = tableInfo.table;
                        String tableName = tableRef.getTableName();
                        Table table = DatabaseLogic.findTableByName(persistence.getModel(), databaseName, tableRef.getSchemaName(), tableName);
                        if(table == null) {
                            logger.warn("Table not found: {}", tableRef.getQualifiedName());
                            RequestMessages.addErrorMessage("Table not found: " + tableRef.getQualifiedName());
                            continue;
                        }
                        File dir = new File(actionsDirectory, table.getActualEntityName());
                        //depth = 1;
                        String userTableName = null, userPasswordColumn = null;
                        Table usersTable = wizard.usersTable;
                        if(usersTable != null) {
                            userTableName = usersTable.getQualifiedName();
                        }
                        Column usersPasswordProperty = wizard.userPasswordProperty;
                        if(usersPasswordProperty != null) {
                            userPasswordColumn = usersPasswordProperty.getColumnName();
                        }
                        createCrudPage(database.getConnectionProvider(), dir, table, template, userTableName, userPasswordColumn);
                    }
                }
                /*if(userTable != null) {
                    setupUserPages(childPages, template);
                }*/
                break;
            case "none":
                break;
            default:
                throw new WebApplicationException("Invalid strategy: " + strategy);
        }
    }

    protected void createCrudPage(
            ConnectionProvider connectionProvider, File dir, Table table, Template template,
            String userTableName, String userPasswordColumn) throws Exception {
        String query = "from " + table.getActualEntityName() + " order by id desc";
        HashMap<String, String> bindings = new HashMap<>();
        bindings.put("parentName", "");
        bindings.put("parentProperty", "nothing");
        bindings.put("linkToParentProperty", NO_LINK_TO_PARENT);
        createCrudPage(connectionProvider, dir, table, query, template, bindings, userTableName, userPasswordColumn, 1);
    }

    protected void createCrudPage(
            ConnectionProvider connectionProvider,
            File dir, Table table, String query,
            Template template, Map<String, String> bindings, String userTableName, String userPasswordColumn, int depth)
            throws Exception {
        if(dir.exists()) {
            RequestMessages.addWarningMessage(
                    ElementsThreadLocals.getText("directory.exists.page.not.created._", dir.getAbsolutePath()));
            return;
        } else if(dir.mkdirs()) {
            logger.info("Creating CRUD page {}", dir.getAbsolutePath());
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
                summ = setupColumn(connectionProvider, column, configuration, summ, linkToParentProperty, userTableName, userPasswordColumn);
            }

            FileObject directory = VFS.getManager().toFileObject(dir);
            PageLogic.saveConfiguration(directory, configuration);
            Page page = new Page();

            Collection<Reference> references = children.get(table);
            if(references != null && depth < maxDepth) {
                for(Reference ref : references) {
                    createChildCrudPage(dir, template, variable, references, ref, pages, depth);
                }
            }

            PageLogic.savePage(directory, page);
            File actionFile = new File(dir, "action.groovy");
            try(FileWriter fileWriter = new FileWriter(actionFile)) {
                template.make(bindings).writeTo(fileWriter);
            }

            logger.debug("Creating _detail directory");
            File detailDir = new File(dir, PageInstance.DETAIL);
            if(!detailDir.isDirectory() && !detailDir.mkdir()) {
                logger.warn("Could not create detail directory {}", detailDir.getAbsolutePath());
                RequestMessages.addWarningMessage(
                        ElementsThreadLocals.getText("couldnt.create.directory", detailDir.getAbsolutePath()));
            }
        } else {
            logger.warn("Couldn't create directory {}", dir.getAbsolutePath());
            RequestMessages.addWarningMessage(
                    ElementsThreadLocals.getText("couldnt.create.directory", dir.getAbsolutePath()));
        }
    }

    protected int setupColumn
            (ConnectionProvider connectionProvider, Column column, CrudConfiguration configuration,
             int columnsInSummary, String linkToParentProperty, String userTableName, String userPasswordColumn) {

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
                column.getColumnName().equals(userPasswordColumn);
        boolean inPk = DatabaseLogic.isInPk(column);
        boolean inFk = DatabaseLogic.isInFk(column);
        boolean inSummary =
                enabled &&
                        (inPk || columnsInSummary < maxColumnsInSummary) &&
                        !propertyIsUserPassword;
        boolean updatable = enabled && !column.isAutoincrement() && !inPk;
        boolean insertable = enabled && !column.isAutoincrement();

        if(!configuration.isLargeResultSet()) {
            detectBooleanColumn(connectionProvider, table, column);
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
           (column.getLength() == null || column.getLength() > MULTILINE_THRESHOLD)) {
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
        //TODO actually we do support database blobs now!
        return column.getJdbcType() == Types.BLOB || column.getJdbcType() == Types.LONGVARBINARY;
    }

    protected final Set<Column> detectedBooleanColumns = new HashSet<>();

    protected void detectBooleanColumn(ConnectionProvider connectionProvider, Table table, Column column) {
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

}
