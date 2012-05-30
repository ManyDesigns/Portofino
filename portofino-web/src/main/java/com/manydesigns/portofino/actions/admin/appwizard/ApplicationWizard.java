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
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.actions.admin.AdminAction;
import com.manydesigns.portofino.actions.admin.ConnectionProvidersAction;
import com.manydesigns.portofino.actions.forms.ConnectionProviderForm;
import com.manydesigns.portofino.actions.forms.SelectableSchema;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.pageactions.PageActionLogic;
import com.manydesigns.portofino.pageactions.crud.CrudAction;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudConfiguration;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.sync.DatabaseSyncer;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.ActionResolver;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@UrlBinding("/actions/admin/wizard")
public class ApplicationWizard extends AbstractActionBean implements AdminAction {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected Form jndiCPForm;
    protected Form jdbcCPForm;
    protected Form connectionProviderForm;

    protected String connectionProviderType;
    protected ConnectionProvider connectionProvider;

    public TableForm schemasForm;
    protected List<SelectableSchema> selectableSchemas;

    protected List<Table> roots;

//**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(RequestAttributes.APPLICATION)
    public Application application;

    @Inject(RequestAttributes.MODEL)
    public Model model;


    public static final Logger logger = LoggerFactory.getLogger(ApplicationWizard.class);

    @DefaultHandler
    public Resolution start() {
        buildCPForms();
        return createSelectionProviderForm();
    }

    protected Resolution createSelectionProviderForm() {
        return new ForwardResolution("/layouts/admin/appwizard/create-connection-provider.jsp");
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
    }

    @Button(list = "create-connection-provider", key="commons.next")
    public Resolution createConnectionProvider() {
        buildCPForms();
        if("JDBC".equals(connectionProviderType)) {
            connectionProvider = new JdbcConnectionProvider();
            connectionProviderForm = jdbcCPForm;
        } else if("JNDI".equals(connectionProviderType)) {
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
            return createSelectionProviderForm();
        }
    }

    public Resolution afterCreateConnectionProvider() {
        try {
            configureEditSchemas();
        } catch (Exception e) {
            logger.error("Coulnd't read schema names from db", e);
            SessionMessages.addErrorMessage("Coulnd't read schema names from db: " + e);
            return createSelectionProviderForm();
        }
        return selectSchemasForm();
    }

    protected Resolution selectSchemasForm() {
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
    }

    @Button(list = "select-schemas", key="commons.next")
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
                return afterSelectSchemas();
            } else {
                SessionMessages.addErrorMessage("Select at least a schema");
                return selectSchemasForm();
            }
        }
        return selectSchemasForm();
    }

    public Resolution afterSelectSchemas() {
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
            targetDatabase = dbSyncer.syncDatabase(model);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            SessionMessages.addErrorMessage(e.toString());
            return selectSchemasForm();
        }
        connectionProvider.setDatabase(targetDatabase);
        connectionProvider.init(application.getDatabasePlatformsManager(), application.getAppDir());
        Model model = new Model();
        model.getDatabases().add(targetDatabase);
        model.init();
        roots = determineRoots();
        //TODO user table
        return buildAppForm();
    }

    protected Resolution buildAppForm() {
        return new ForwardResolution("/layouts/admin/appwizard/build-app.jsp");
    }

    protected List<Table> determineRoots() {
        List<Table> roots = new ArrayList<Table>();
        for(SelectableSchema selectableSchema : selectableSchemas) {
            if(selectableSchema.selected) {
                Schema schema = DatabaseLogic.findSchemaByName(
                        connectionProvider.getDatabase(), selectableSchema.schemaName);
                roots.addAll(schema.getTables());
            }
        }
        Set<Table> referencedTables = new HashSet<Table>();
        for(Table table : roots) {
            for(ForeignKey fk : table.getForeignKeys()) {
                for(Reference ref : fk.getReferences()) {
                    referencedTables.add(ref.getActualToColumn().getTable());
                }
            }
            for(ModelSelectionProvider sp : table.getSelectionProviders()) {
                for(Reference ref : sp.getReferences()) {
                    referencedTables.add(ref.getActualToColumn().getTable());
                }
            }
        }
        roots.removeAll(referencedTables);
        return roots;
    }

    @Button(list = "build-app", key="wizard.finish")
    public Resolution buildApplication() {
        selectSchemas();
        application.getModel().getDatabases().add(connectionProvider.getDatabase());
        application.initModel();
        try {
            application.saveXmlModel();
            String scriptTemplate = PageActionLogic.getScriptTemplate(CrudAction.class);
            for(Table table : roots) {
                File dir = new File(application.getPagesDir(), table.getActualEntityName());
                //TODO dir.exists()???
                if(dir.exists() || dir.mkdirs()) {
                    CrudConfiguration configuration = new CrudConfiguration();
                    configuration.setDatabase(connectionProvider.getDatabase().getDatabaseName());
                    configuration.setQuery("from " + table.getActualEntityName());
                    configuration.setVariable(table.getActualEntityName());
                    DispatcherLogic.saveConfiguration(dir, configuration);
                    Page page = new Page();
                    page.setId(RandomUtil.createRandomId());
                    page.setTitle(table.getActualEntityName());
                    page.setDescription(table.getActualEntityName());
                    //TODO children
                    DispatcherLogic.savePage(dir, page);
                    File actionFile = new File(dir, "action.groovy");
                    FileWriter fileWriter = new FileWriter(actionFile);
                    IOUtils.write(scriptTemplate, fileWriter);
                    IOUtils.closeQuietly(fileWriter);
                } else {
                    logger.error("Couldn't create directory {}", dir.getAbsolutePath());
                    SessionMessages.addErrorMessage("Couldn't create directory " + dir.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            logger.error("Errore in sincronizzazione", e);
            SessionMessages.addErrorMessage(
                    "Synchronization error: " +
                            ExceptionUtils.getRootCauseMessage(e));
            return buildAppForm();
        }
        return new RedirectResolution("/");
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
        return connectionProvider == null || connectionProvider instanceof JdbcConnectionProvider;
    }

    public boolean isJndi() {
        return connectionProvider instanceof JndiConnectionProvider;
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
}
