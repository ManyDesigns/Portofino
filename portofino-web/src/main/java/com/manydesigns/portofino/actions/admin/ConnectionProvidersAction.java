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

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.actions.admin.appwizard.ApplicationWizard;
import com.manydesigns.portofino.actions.forms.ConnectionProviderForm;
import com.manydesigns.portofino.actions.forms.ConnectionProviderTableForm;
import com.manydesigns.portofino.actions.forms.SelectableSchema;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.database.platforms.DatabasePlatform;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.security.RequiresAdministrator;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.ActionResolver;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@RequiresAdministrator
@UrlBinding("/actions/admin/connection-providers")
public class ConnectionProvidersAction extends AbstractActionBean implements AdminAction {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

//    public List<ConnectionProvider> connectionProviders;
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

    @Inject(RequestAttributes.APPLICATION)
    public Application application;

    @Inject(RequestAttributes.MODEL)
    public Model model;

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
                .configNRows(model.getDatabases().size())
                .configHrefTextFormat("databaseName", hrefFormat)
                .configMode(Mode.VIEW)
                .build();
        tableForm.setSelectable(true);
        tableForm.setKeyGenerator(OgnlTextFormat.create("%{databaseName}"));

        List<ConnectionProviderTableForm> tableFormObj = new ArrayList<ConnectionProviderTableForm>();
        for(Database database : model.getDatabases()) {
            ConnectionProvider connectionProvider =
                    database.getConnectionProvider();
            tableFormObj.add(new ConnectionProviderTableForm(
                    database.getDatabaseName(),
                    connectionProvider.getDescription(),
                    connectionProvider.getStatus()));
        }
        tableForm.readFromObject(tableFormObj);

        // database platforms
        DatabasePlatformsManager manager =
                application.getDatabasePlatformsManager();
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

        return new ForwardResolution("/layouts/admin/connectionProviders/list.jsp");
    }

    public Resolution read() {
        connectionProvider = application.getConnectionProvider(databaseName);
        databasePlatform = connectionProvider.getDatabasePlatform();
        connectionProviderForm = new ConnectionProviderForm(connectionProvider.getDatabase());

        buildConnectionProviderForm(Mode.VIEW);
        form.readFromObject(connectionProviderForm);

        if (ConnectionProvider.STATUS_CONNECTED
                .equals(connectionProvider.getStatus())) {
            configureDetected();
        }

        return new ForwardResolution("/layouts/admin/connectionProviders/read.jsp");
    }

    public final static String[] jdbcViewFields = {"databaseName", "driver",
                            "url", "username", "password",
                            "status", "errorMessage", "lastTested"};

    public final static String[] jdbcEditFields = {"databaseName", "driver",
                            "url", "username", "password"
                            };

    public final static String[] jndiViewFields = {"databaseName", "jndiResource",
                            "status", "errorMessage", "lastTested"};

    public final static String[] jndiEditFields = {"databaseName", "jndiResource"};

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

    @Button(list = "connectionProviders-read", key = "layouts.admin.connectionProviders.list.test", order = 3)
    public Resolution test() {
        connectionProvider = application.getConnectionProvider(databaseName);
        connectionProvider.init(application.getDatabasePlatformsManager(), application.getAppDir());
        String status = connectionProvider.getStatus();
        if (ConnectionProvider.STATUS_CONNECTED.equals(status)) {
            SessionMessages.addInfoMessage(getMessage("connectionProviders.test.successful"));
        } else {
            SessionMessages.addErrorMessage(
                    getMessage("connectionProviders.test.failed",
                               status, connectionProvider.getErrorMessage()));
        }
        return new RedirectResolution(this.getClass())
                .addParameter("databaseName", databaseName);
    }

    @Button(list = "connectionProviders-search", key = "commons.create", order = 1)
    public Resolution create() {
        return new RedirectResolution(ApplicationWizard.class);
    }

    @Button(list = "connectionProviders-read", key = "commons.edit", order = 2)
    public Resolution edit() {
        connectionProvider = application.getConnectionProvider(databaseName);
        databasePlatform = connectionProvider.getDatabasePlatform();
        connectionProviderForm = new ConnectionProviderForm(connectionProvider.getDatabase());

        buildConnectionProviderForm(Mode.EDIT);
        form.readFromObject(connectionProviderForm);

        configureEditSchemas();

        return new ForwardResolution("/layouts/admin/connectionProviders/edit.jsp");
    }

    @Button(list = "connectionProviders-edit", key = "commons.update", order = 1)
    public Resolution update() {
        connectionProvider = application.getConnectionProvider(databaseName);
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
                connectionProvider.init(application.getDatabasePlatformsManager(), application.getAppDir());
                application.initModel();
                application.saveXmlModel();
                SessionMessages.addInfoMessage(getMessage("connectionProviders.update.successful"));
            } catch (Exception e) {
                String msg = "Cannot save model: " +
                        ExceptionUtils.getRootCauseMessage(e);
                SessionMessages.addErrorMessage(msg);
                logger.error(msg, e);
            }
        }
        return new RedirectResolution(this.getClass())
                .addParameter("databaseName", databaseName);
    }

    @Buttons({
        @Button(list = "connectionProviders-edit", key = "commons.cancel", order = 2),
        @Button(list = "connectionProviders-create", key = "commons.cancel", order = 2)
    })
    public Resolution cancel() {
        return execute();
    }

    @Button(list = "connectionProviders-read", key = "commons.delete", order = 5)
    public Resolution delete() {
        String[] databaseNames = new String[] {databaseName};
        try {
            doDelete(databaseNames);
            application.initModel();
            application.saveXmlModel();
        } catch (Exception e) {
            String msg = "Cannot save model: " +
                    ExceptionUtils.getRootCauseMessage(e);
            logger.error(msg, e);
            SessionMessages.addErrorMessage(msg);
        }
        return new RedirectResolution(this.getClass());
    }

    @Button(list = "connectionProviders-search", key = "commons.delete", order = 2)
    public Resolution bulkDelete() {
        if(null!=selection && 0!=selection.length){
            try {
                doDelete(selection);
                application.initModel();
                application.saveXmlModel();
            } catch (Exception e) {
                String msg = "Cannot save model: " +
                        ExceptionUtils.getRootCauseMessage(e);
                logger.error(msg, e);
                SessionMessages.addErrorMessage(msg);
            }
        } else {
            SessionMessages.addInfoMessage(getMessage("connectionProviders.delete.noneSelected"));
        }
        return new RedirectResolution(this.getClass());
    }

    protected void doDelete(String[] databaseNames) {
        for (String current : databaseNames) {
            if (current == null) {
                continue;
            }
            Database database =
                    DatabaseLogic.findDatabaseByName(model, current);
            if (database == null) {
                SessionMessages.addWarningMessage(
                        "Delete failed. Connection provider not found: " + current);
            } else {
                model.getDatabases().remove(database);
                SessionMessages.addInfoMessage(
                        "Connection provider deleted successfully: " + current);
            }
        }
    }

    @Button(list = "connectionProviders-read", key = "layouts.admin.connectionProviders.list.synchronize", order = 4)
    public Resolution sync() {
        try {
            application.syncDataModel(databaseName);
            application.initModel();
            application.saveXmlModel();
            DispatcherLogic.clearConfigurationCache();
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

    @Buttons({
        @Button(list = "connectionProviders-read", key = "commons.returnToList", order = 1),
        @Button(list = "connectionProviders-select-type-content-buttons", key = "commons.returnToList", order = 1)
    })
    public Resolution returnToList() {
        return new RedirectResolution(ConnectionProvidersAction.class);
    }

    @Button(list = "connectionProviders-search", key = "commons.returnToPages", order = 3)
    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

    public String getActionPath() {
        return (String) getContext().getRequest().getAttribute(ActionResolver.RESOLVED_ACTION);
    }

    protected String getMessage(String key, Object... args) {
        Locale locale = context.getLocale();
        ResourceBundle resourceBundle = application.getBundle(locale);
        String msg = resourceBundle.getString(key);
        return MessageFormat.format(msg, args);
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
