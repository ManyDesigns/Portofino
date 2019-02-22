package com.manydesigns.portofino.upstairs.actions.database.connections;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.json.JsonKeyValueAccessor;
import com.manydesigns.elements.messages.RequestMessages;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.util.FormUtil;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.upstairs.actions.database.connections.support.ConnectionProviderDetail;
import com.manydesigns.portofino.upstairs.actions.database.connections.support.ConnectionProviderSummary;
import com.manydesigns.portofino.upstairs.actions.database.connections.support.SelectableSchema;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alessio Stalla - alessiostalla@gmail.com
 */
@RequiresAuthentication
@RequiresAdministrator
public class ConnectionsAction extends AbstractPageAction {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionsAction.class);

    @Autowired
    protected Persistence persistence;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ConnectionProviderSummary> list() {
        List<ConnectionProviderSummary> list = new ArrayList<>();
        persistence.getModel().getDatabases().forEach(database -> {
            ConnectionProvider connectionProvider = database.getConnectionProvider();
            list.add(new ConnectionProviderSummary(
                    database.getDatabaseName(), connectionProvider.getDescription(), connectionProvider.getStatus()));
        });
        return list;
    }

    @GET
    @Path("{databaseName}")
    @Produces(MediaType.APPLICATION_JSON)
    public String describeConnection(@PathParam("databaseName") String databaseName) throws Exception {
        ConnectionProvider connectionProvider = persistence.getConnectionProvider(databaseName);
        ConnectionProviderDetail cp = new ConnectionProviderDetail(connectionProvider);
        Form form = new FormBuilder(ConnectionProviderDetail.class).build();
        form.readFromObject(cp);
        if (ConnectionProvider.STATUS_CONNECTED.equals(connectionProvider.getStatus())) {
            //TODO configureDetected();
        }
        JSONStringer js = new JSONStringer();
        js.object();
        FormUtil.writeToJson(form, js);
        js.key("schemas").array();
        for(SelectableSchema schema: getSchemas(connectionProvider)) {
            js.object();
            js.key("catalog").value(schema.catalogName);
            js.key("name").value(schema.schemaName);
            js.key("schema").value(schema.schema);
            js.key("selected").value(schema.selected);
            js.endObject();
        }
        js.endArray();
        js.endObject();
        return js.toString();
    }

    @PUT
    @Path("{databaseName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveConnection(@PathParam("databaseName") String databaseName, String jsonInput) {
        ConnectionProvider connectionProvider = persistence.getConnectionProvider(databaseName);
        ConnectionProviderDetail cp = new ConnectionProviderDetail(connectionProvider);
        Form form = new FormBuilder(ConnectionProviderDetail.class).configMode(Mode.EDIT).build();
        if(cp.getJndiResource() != null) {
            Field jndiResource = form.findFieldByPropertyName("jndiResource");
            form.get(0).clear();
            form.get(0).add(jndiResource);
        } else {
            Field jndiResource = form.findFieldByPropertyName("jndiResource");
            form.get(0).remove(jndiResource);
        }
        form.readFromObject(cp);
        JSONObject jsonObject = new JSONObject(jsonInput);
        FormUtil.readFromJson(form, jsonObject);
        if(form.validate()) {
            JSONArray schemasJson = jsonObject.getJSONArray("schemas");
            updateSchemas(connectionProvider, schemasJson);
            form.writeToObject(cp);
            try {
                connectionProvider.init(persistence.getDatabasePlatformsRegistry());
                persistence.initModel();
                persistence.saveXmlModel();
                return Response.ok(form).build();
            } catch (Exception e) {
                String msg = "Cannot save model: " + ExceptionUtils.getRootCauseMessage(e);
                logger.error(msg, e);
                RequestMessages.addErrorMessage(msg);
                return Response.serverError().entity(form).build();
            }
        }
        return Response.serverError().entity(form).build();
    }

    public void updateSchemas(ConnectionProvider connectionProvider, JSONArray schemasJson) {
        Database database = connectionProvider.getDatabase();
        List<Schema> selectedSchemas = database.getSchemas();
        List<String> selectedSchemaNames = new ArrayList<>(selectedSchemas.size());
        for(Schema schema : selectedSchemas) {
            selectedSchemaNames.add(schema.getSchema().toLowerCase());
        }
        for(Object schemaObject : schemasJson) {
            JSONObject schema = (JSONObject) schemaObject;
            boolean selected = schema.getBoolean("selected");
            String schemaName = schema.getString("schema").toLowerCase();
            if(selected) {
                if(selectedSchemaNames.contains(schemaName)) {
                    for(Schema modelSchema : database.getSchemas()) {
                        if(modelSchema.getSchema().equalsIgnoreCase(schemaName)) {
                            if(!schema.isNull("name")) {
                                modelSchema.setSchemaName(schema.getString("name"));
                            }
                            break;
                        }
                    }
                } else {
                    Schema modelSchema = new Schema();
                    if(!schema.isNull("catalog")) {
                        modelSchema.setCatalog(schema.getString("catalog"));
                    }
                    if(!schema.isNull("name")) {
                        modelSchema.setSchemaName(schema.getString("name"));
                    }
                    modelSchema.setSchema(schemaName);
                    modelSchema.setDatabase(database);
                    database.getSchemas().add(modelSchema);
                }
            } else if(selectedSchemaNames.contains(schemaName)) {
                Schema toBeRemoved = null;
                for(Schema aSchema : database.getSchemas()) {
                    if(aSchema.getSchema().equalsIgnoreCase(schemaName)) {
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

    protected List<SelectableSchema> getSchemas(ConnectionProvider connectionProvider) throws Exception {
        Connection conn = connectionProvider.acquireConnection();
        logger.debug("Reading database metadata");
        DatabaseMetaData metadata = conn.getMetaData();
        List<String[]> schemaNamesFromDb =
                connectionProvider.getDatabasePlatform().getSchemaNames(metadata);
        connectionProvider.releaseConnection(conn);

        List<Schema> selectedSchemas = connectionProvider.getDatabase().getSchemas();

        List<SelectableSchema> selectableSchemas = new ArrayList<>(schemaNamesFromDb.size());
        for(String[] schemaName : schemaNamesFromDb) {
            boolean selected = false;
            for(Schema schema : selectedSchemas) {
                if(schemaName[1].equalsIgnoreCase(schema.getSchema())) {
                    selected = true;
                    break;
                }
            }
            SelectableSchema schema = new SelectableSchema(schemaName[0], schemaName[1], schemaName[1], selected);
            selectableSchemas.add(schema);
        }
        return selectableSchemas;
    }

    @DELETE
    @Path("{databaseName}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteConnection(@PathParam("databaseName") String databaseName) throws IOException, JAXBException {
        Database database =
                DatabaseLogic.findDatabaseByName(persistence.getModel(), databaseName);
        if (database == null) {
            throw new WebApplicationException("Delete failed. Connection provider not found: " + databaseName);
        } else {
            persistence.getModel().getDatabases().remove(database);
            persistence.initModel();
            persistence.saveXmlModel();
            logger.info("Database {} deleted", databaseName);
        }
    }

    @POST
    @Path("{databaseName}/:synchronize")
    public void synchronize(@PathParam("databaseName") String databaseName) throws Exception {
        persistence.syncDataModel(databaseName);
        persistence.initModel();
        persistence.saveXmlModel();
        RequestMessages.addInfoMessage("Model synchronized");
    }

    @POST
    @Path("{databaseName}/:test")
    public String[] test(@PathParam("databaseName") String databaseName) {
        ConnectionProvider connectionProvider = persistence.getConnectionProvider(databaseName);
        connectionProvider.init(persistence.getDatabasePlatformsRegistry());
        return new String[] { connectionProvider.getStatus(), connectionProvider.getErrorMessage() };
    }

}
