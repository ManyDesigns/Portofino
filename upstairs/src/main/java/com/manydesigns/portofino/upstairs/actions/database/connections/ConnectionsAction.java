package com.manydesigns.portofino.upstairs.actions.database.connections;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.RequestMessages;
import com.manydesigns.elements.util.FormUtil;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.upstairs.actions.database.connections.support.ConnectionProviderDetail;
import com.manydesigns.portofino.upstairs.actions.database.connections.support.ConnectionProviderSummary;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
        Connection conn = connectionProvider.acquireConnection();
        logger.debug("Reading database metadata");
        DatabaseMetaData metadata = conn.getMetaData();
        List<String[]> schemaNamesFromDb =
                connectionProvider.getDatabasePlatform().getSchemaNames(metadata);
        List<Schema> selectedSchemas = connectionProvider.getDatabase().getSchemas();
        for(String[] schemaName : schemaNamesFromDb) {
            boolean selected = false;
            for(Schema schema : selectedSchemas) {
                if(schemaName[1].equalsIgnoreCase(schema.getSchema())) {
                    selected = true;
                    break;
                }
            }
            js.object();
            js.key("catalog").value(schemaName[0]);
            js.key("name").value(schemaName[1]);
            js.key("selected").value(selected);
            js.endObject();
        }
        js.endArray();
        js.endObject();
        return js.toString();
    }

    @PUT
    @Path("{databaseName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveConnection(@PathParam("databaseName") String databaseName) {
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
        form.readFromRequest(context.getRequest());
        //TODO schema select
        if(form.validate()) {
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

    @POST
    @Path("{databaseName}/:synchronize")
    public void synchronize(@PathParam("databaseName") String databaseName) throws Exception {
        persistence.syncDataModel(databaseName);
        persistence.initModel();
        persistence.saveXmlModel();
    }


}
