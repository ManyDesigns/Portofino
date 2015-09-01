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
import com.manydesigns.elements.configuration.CommonsConfigurationUtils;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.portofino.actions.admin.appwizard.ApplicationWizard;
import com.manydesigns.portofino.actions.admin.database.forms.ConnectionProviderForm;
import com.manydesigns.portofino.actions.admin.database.forms.ConnectionProviderTableForm;
import com.manydesigns.portofino.actions.admin.database.forms.SelectableSchema;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.database.platforms.DatabasePlatform;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsRegistry;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import net.sourceforge.stripes.action.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@RequiresAuthentication
@RequiresAdministrator
@UrlBinding(ConnectionProvidersAction.URL_BINDING)
public class ConnectionProvidersAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final String URL_BINDING = "/actions/admin/connection-providers";

    public ConnectionProvider connectionProvider;
    public DatabasePlatform[] databasePlatforms;
    public DatabasePlatform databasePlatform;

    protected ConnectionProviderForm connectionProviderForm;
    public TableForm tableForm;
    public Form form;
    public Form detectedValuesForm;
    public TableForm schemasForm;
    public TableForm databasePlatformsTableForm;

    public String databaseName;

    public String[] selection;
    protected List<SelectableSchema> selectableSchemas;

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(DatabaseModule.PERSISTENCE)
    Persistence persistence;

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    Configuration configuration;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(ConnectionProvidersAction.class);

    //--------------------------------------------------------------------------
    // Action events
    //--------------------------------------------------------------------------

    @DefaultHandler
    public Resolution execute() {
        if (databaseName == null) {
            return search();
        } else {
            return read();
        }
    }

    public Resolution search() {
        OgnlTextFormat hrefFormat =
                OgnlTextFormat.create(
                        "/actions/admin/connection-providers?" +
                                "databaseName=%{databaseName}");
        hrefFormat.setUrl(true);

        tableForm = new TableFormBuilder(ConnectionProviderTableForm.class)
                .configFields("databaseName", "description", "status")
                .configNRows(persistence.getModel().getDatabases().size())
                .configHrefTextFormat("databaseName", hrefFormat)
                .configMode(Mode.VIEW)
                .build();
        tableForm.setSelectable(true);
        tableForm.setKeyGenerator(OgnlTextFormat.create("%{databaseName}"));

        List<ConnectionProviderTableForm> tableFormObj = new ArrayList<ConnectionProviderTableForm>();
        for(Database database : persistence.getModel().getDatabases()) {
            ConnectionProvider connectionProvider =
                    database.getConnectionProvider();
            tableFormObj.add(new ConnectionProviderTableForm(
                    database.getDatabaseName(),
                    connectionProvider.getDescription(),
                    connectionProvider.getStatus()));
        }
        tableForm.readFromObject(tableFormObj);

        // database platforms
        DatabasePlatformsRegistry manager =
                persistence.getDatabasePlatformsRegistry();
        databasePlatforms = manager.getDatabasePlatforms();
        databasePlatformsTableForm =
                new TableFormBuilder(DatabasePlatform.class)
                        .configFields("description",
                                "standardDriverClassName",
                                "status")
                        .configNRows(databasePlatforms.length)
                        .configMode(Mode.VIEW)
                        .build();
        databasePlatformsTableForm.readFromObject(databasePlatforms);

        return new ForwardResolution("/m/admin/connectionProviders/list.jsp");
    }

    public Resolution read() {
        connectionProvider = persistence.getConnectionProvider(databaseName);
        databasePlatform = connectionProvider.getDatabasePlatform();
        connectionProviderForm = new ConnectionProviderForm(connectionProvider.getDatabase());

        buildConnectionProviderForm(Mode.VIEW);
        form.readFromObject(connectionProviderForm);

        if (ConnectionProvider.STATUS_CONNECTED
                .equals(connectionProvider.getStatus())) {
            configureDetected();
        }

        return new ForwardResolution("/m/admin/connectionProviders/read.jsp");
    }

    public final static String[] jdbcViewFields = {"databaseName", "driver",
                            "url", "username", "password",
                            "status", "errorMessage", "lastTested"};

    public final static String[] jdbcEditFields = {"databaseName", "driver",
                            "url", "username", "password", "hibernateDialect", "trueString", "falseString"
                            };

    public final static String[] jndiViewFields = {"databaseName", "jndiResource",
                            "status", "errorMessage", "lastTested"};

    public final static String[] jndiEditFields = {"databaseName", "jndiResource", "hibernateDialect", "trueString", "falseString"};

    private void buildConnectionProviderForm(Mode mode) {
        String [] fields;
        if (connectionProvider instanceof JdbcConnectionProvider) {
            fields = (mode == Mode.VIEW)
                    ? jdbcViewFields
                    : jdbcEditFields;
        } else if (connectionProvider instanceof JndiConnectionProvider) {
            fields = (mode == Mode.VIEW)
                    ? jndiViewFields
                    : jndiEditFields;
        } else {
            throw new InternalError("Unknown connection provider type: " +
                    connectionProvider.getClass().getName());
        }
        form = new FormBuilder(ConnectionProviderForm.class)
                    .configFields(fields)
                    .configMode(mode)
                    .build();
    }

    protected void configureDetected() {
        detectedValuesForm = new FormBuilder(JdbcConnectionProvider.class)
                .configFields(
                        "databaseProductName",
                        "databaseProductVersion",
                        "databaseMajorMinorVersion",
                        "driverName",
                        "driverVersion",
                        "driverMajorMinorVersion",
                        "JDBCMajorMinorVersion"
                        )
                .configMode(Mode.VIEW)
                .build();
        detectedValuesForm.readFromObject(connectionProvider);
    }

    protected void configureEditSchemas() {
        try {
            Connection conn = connectionProvider.acquireConnection();
            logger.debug("Reading database metadata");
            DatabaseMetaData metadata = conn.getMetaData();
            List<String> schemaNamesFromDb =
                    connectionProvider.getDatabasePlatform().getSchemaNames(metadata);
            connectionProvider.releaseConnection(conn);

            List<Schema> selectedSchemas = connectionProvider.getDatabase().getSchemas();

            selectableSchemas = new ArrayList<SelectableSchema>(schemaNamesFromDb.size());
            for(String schemaName : schemaNamesFromDb) {
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
                    .build();
            schemasForm.readFromObject(selectableSchemas);
        } catch (Exception e) {
            logger.error("Coulnd't read schema names from db", e);
        }
    }

    @Button(list = "connectionProviders-read", key = "test", order = 3 , type = Button.TYPE_WARNING , icon = Button.ICON_FLASH )
    public Resolution test() {
        connectionProvider = persistence.getConnectionProvider(databaseName);
        connectionProvider.init(persistence.getDatabasePlatformsRegistry());
        String status = connectionProvider.getStatus();
        if (ConnectionProvider.STATUS_CONNECTED.equals(status)) {
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("connection.tested.successfully"));
        } else {
            SessionMessages.addErrorMessage(
                    ElementsThreadLocals.getText(
                            "connection.failed.status._.error.message._",
                            status, connectionProvider.getErrorMessage()));
        }
        return new RedirectResolution(this.getClass())
                .addParameter("databaseName", databaseName);
    }

    @Button(list = "connectionProviders-search", key = "create.new", order = 1 , type = Button.TYPE_SUCCESS , icon = Button.ICON_PLUS )
    public Resolution create() {
        return new RedirectResolution(ApplicationWizard.class);
    }

    @Button(list = "connectionProviders-read", key = "edit", order = 2 , icon = Button.ICON_EDIT , type = Button.TYPE_DEFAULT)
    public Resolution edit() {
        connectionProvider = persistence.getConnectionProvider(databaseName);
        databasePlatform = connectionProvider.getDatabasePlatform();
        connectionProviderForm = new ConnectionProviderForm(connectionProvider.getDatabase());

        buildConnectionProviderForm(Mode.EDIT);
        form.readFromObject(connectionProviderForm);

        configureEditSchemas();

        return new ForwardResolution("/m/admin/connectionProviders/edit.jsp");
    }

    @Button(list = "connectionProviders-edit", key = "update", order = 1, type = Button.TYPE_PRIMARY)
    public Resolution update() {
        connectionProvider = persistence.getConnectionProvider(databaseName);
        databasePlatform = connectionProvider.getDatabasePlatform();
        Database database = connectionProvider.getDatabase();
        connectionProviderForm = new ConnectionProviderForm(database);

        buildConnectionProviderForm(Mode.EDIT);
        form.readFromObject(connectionProviderForm);

        form.readFromRequest(context.getRequest());

        configureEditSchemas();
        boolean schemasValid = true;
        if(schemasForm != null){
            schemasForm.readFromRequest(context.getRequest());
            schemasValid = schemasForm.validate();
        }
        if (form.validate() && schemasValid) {
            if(schemasForm != null) {
                schemasForm.writeToObject(selectableSchemas);
                List<Schema> selectedSchemas = database.getSchemas();
                List<String> selectedSchemaNames = new ArrayList<String>(selectedSchemas.size());
                for(Schema schema : selectedSchemas) {
                    selectedSchemaNames.add(schema.getSchemaName().toLowerCase());
                }
                for(SelectableSchema schema : selectableSchemas) {
                    if(schema.selected && !selectedSchemaNames.contains(schema.schemaName.toLowerCase())) {
                        Schema modelSchema = new Schema();
                        modelSchema.setSchemaName(schema.schemaName);
                        modelSchema.setDatabase(database);
                        database.getSchemas().add(modelSchema);
                    } else if(!schema.selected && selectedSchemaNames.contains(schema.schemaName.toLowerCase())) {
                        Schema toBeRemoved = null;
                        for(Schema aSchema : database.getSchemas()) {
                            if(aSchema.getSchemaName().equalsIgnoreCase(schema.schemaName)) {
                                toBeRemoved = aSchema;
                                break;
                            }
                        }
                        if(toBeRemoved != null) {
                            database.getSchemas().remove(toBeRemoved);
                        }
                    }
                }
            }
            form.writeToObject(connectionProviderForm);
            try {
                connectionProvider.init(persistence.getDatabasePlatformsRegistry());
                persistence.initModel();
                persistence.saveXmlModel();
                CommonsConfigurationUtils.save(configuration);
                SessionMessages.addInfoMessage(ElementsThreadLocals.getText("connection.provider.updated.successfully"));
            } catch (Exception e) {
                String msg = "Cannot save model: " +
                        ExceptionUtils.getRootCauseMessage(e);
                SessionMessages.addErrorMessage(msg);
                logger.error(msg, e);
            }
            return new RedirectResolution(this.getClass()).addParameter("databaseName", databaseName);
        } else {
            return new ForwardResolution("/m/admin/connectionProviders/edit.jsp");
        }
    }

    @Buttons({
        @Button(list = "connectionProviders-edit", key = "cancel", order = 2),
        @Button(list = "connectionProviders-create", key = "cancel", order = 2)
    })
    public Resolution cancel() {
        return execute();
    }

    @Button(list = "connectionProviders-read", key = "delete", order = 6 , type = Button.TYPE_DANGER, icon = Button.ICON_TRASH )
    public Resolution delete() {
        String[] databaseNames = new String[] {databaseName};
        try {
            doDelete(databaseNames);
            persistence.initModel();
            persistence.saveXmlModel();
        } catch (Exception e) {
            String msg = "Cannot save model: " +
                    ExceptionUtils.getRootCauseMessage(e);
            logger.error(msg, e);
            SessionMessages.addErrorMessage(msg);
        }
        return new RedirectResolution(this.getClass());
    }

    @Button(list = "connectionProviders-search", key = "delete", order = 2 , type = Button.TYPE_DANGER, icon = Button.ICON_TRASH )
    public Resolution bulkDelete() {
        if(null!=selection && 0!=selection.length){
            try {
                doDelete(selection);
                persistence.initModel();
                persistence.saveXmlModel();
            } catch (Exception e) {
                String msg = "Cannot save model: " +
                        ExceptionUtils.getRootCauseMessage(e);
                logger.error(msg, e);
                SessionMessages.addErrorMessage(msg);
            }
        } else {
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("no.connection.providers.selected"));
        }
        return new RedirectResolution(this.getClass());
    }

    protected void doDelete(String[] databaseNames) {
        for (String current : databaseNames) {
            if (current == null) {
                continue;
            }
            Database database =
                    DatabaseLogic.findDatabaseByName(persistence.getModel(), current);
            if (database == null) {
                SessionMessages.addWarningMessage(
                        "Delete failed. Connection provider not found: " + current);
            } else {
                persistence.getModel().getDatabases().remove(database);
                SessionMessages.addInfoMessage(
                        "Connection provider deleted successfully: " + current);
            }
        }
    }

    @Button(list = "connectionProviders-read", key = "synchronize", order = 4 , type = Button.TYPE_PRIMARY, icon = Button.ICON_RELOAD )
    public Resolution sync() {
        try {
            persistence.syncDataModel(databaseName);
            persistence.initModel();
            persistence.saveXmlModel();
            SessionMessages.addInfoMessage(
                    "Connection provider synchronized correctly");
        } catch (Exception e) {
            logger.error("Errore in sincronizzazione", e);
            SessionMessages.addErrorMessage(
                    "Synchronization error: " +
                            ExceptionUtils.getRootCauseMessage(e));
        }
        return new RedirectResolution(getClass())
                .addParameter("databaseName", databaseName);
    }

    @Button(list = "connectionProviders-read", key = "run.wizard", order = 5 , type = Button.TYPE_INFO)
    public Resolution runWizard() {
        ConnectionProvider connectionProvider = persistence.getConnectionProvider(databaseName);
        return new RedirectResolution(ApplicationWizard.class)
                .addParameter("connectionProviderName", databaseName)
                .addParameter("configureConnectionProvider")
                .addParameter("connectionProviderType", connectionProvider instanceof JdbcConnectionProvider ? "JDBC" : "JNDI");
    }

    @Buttons({
        @Button(list = "connectionProviders-read", key = "return.to.list", order = 1 , icon = Button.ICON_LEFT),
        @Button(list = "connectionProviders-select-type-content-buttons", key = "return.to.list", order = 1, icon = Button.ICON_LEFT)
    })
    public Resolution returnToList() {
        return new RedirectResolution(ConnectionProvidersAction.class);
    }

    @Button(list = "connectionProviders-search", key = "return.to.pages", order = 3  , icon = Button.ICON_HOME)
    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public Form getDetectedValuesForm() {
        return detectedValuesForm;
    }

    public TableForm getSchemasForm() {
        return schemasForm;
    }

    public ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }
}
