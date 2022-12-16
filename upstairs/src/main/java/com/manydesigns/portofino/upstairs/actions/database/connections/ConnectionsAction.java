/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.upstairs.actions.database.connections;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.RequestMessages;
import com.manydesigns.elements.util.FormUtil;
import com.manydesigns.portofino.database.model.*;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.resourceactions.AbstractResourceAction;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.upstairs.actions.database.connections.support.ConnectionProviderDetail;
import com.manydesigns.portofino.upstairs.actions.database.connections.support.ConnectionProviderSummary;
import com.manydesigns.portofino.upstairs.actions.database.connections.support.ExcludeFromWizard;
import com.manydesigns.portofino.upstairs.actions.database.connections.support.SelectableSchema;
import com.manydesigns.portofino.upstairs.actions.support.TableInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
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
import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * @author Alessio Stalla - alessiostalla@gmail.com
 */
@RequiresAuthentication
@RequiresAdministrator
public class ConnectionsAction extends AbstractResourceAction {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionsAction.class);

    @Autowired
    protected Persistence persistence;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ConnectionProviderSummary> list() {
        List<ConnectionProviderSummary> list = new ArrayList<>();
        persistence.getDatabases().forEach(database -> {
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
        return connectionWithSchemas(databaseName, connectionProvider, form);
    }

    public String connectionWithSchemas(String databaseName, ConnectionProvider connectionProvider, Form form) throws Exception {
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

            Schema dbSchema = DatabaseLogic.findSchemaByName(
                    persistence.getDatabases(), databaseName, schema.schemaName);
            FileObject changelogFile = persistence.getLiquibaseChangelogFile(dbSchema);
            js.key("liquibase").value(changelogFile != null && changelogFile.getType() == FileType.FILE);
            js.endObject();
        }
        js.endArray();
        js.endObject();
        return js.toString();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createConnection(String jsonInput) {
        JSONObject jsonObject = new JSONObject(jsonInput);
        Database database = new Database();
        database.setDatabaseName(jsonObject.getString("databaseName"));
        ConnectionProvider connectionProvider;
        if(jsonObject.isNull("jndiResource")) {
            JdbcConnectionProvider jdbcConnectionProvider = new JdbcConnectionProvider();
            //Fill with dummy values so the form overwrites them (and doesn't try to write on the Configuration which
            //is not available and anyway should not be modified right now)
            jdbcConnectionProvider.setUrl("replace me");
            jdbcConnectionProvider.setUsername("replace me");
            jdbcConnectionProvider.setPassword("replace me");
            connectionProvider = jdbcConnectionProvider;
        } else {
            connectionProvider = new JndiConnectionProvider();
        }
        connectionProvider.setDatabase(database);
        database.setConnectionProvider(connectionProvider);
        return saveConnectionProvider(connectionProvider, jsonObject, this::doCreateConnectionProvider);
    }

    protected Response doCreateConnectionProvider(ConnectionProvider connectionProvider, Form form) {
        String databaseName = connectionProvider.getDatabase().getDatabaseName();
        if(persistence.getConnectionProvider(databaseName) != null) {
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(ElementsThreadLocals.getText("the.database.already.exists"))
                    .build();
        }
        persistence.addDatabase(connectionProvider.getDatabase());
        persistence.initModel();
        try {
            String connectionsWithSchemas =
                    connectionWithSchemas(connectionProvider.getDatabase().getDatabaseName(), connectionProvider, form);
            persistence.saveModel();
            return Response.created(new URI(getActionPath() + "/" + databaseName)).entity(connectionsWithSchemas).build();
        } catch (Exception e) {
            persistence.removeDatabase(connectionProvider.getDatabase());
            persistence.initModel();
            try {
                persistence.saveModel();
            } catch (Exception ex) {
                logger.error("Cannot save restored model", ex);
            }
            RequestMessages.addErrorMessage(e.getLocalizedMessage());
            throw new WebApplicationException(e);
        }
    }

    @PUT
    @Path("{databaseName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveConnection(@PathParam("databaseName") String databaseName, String jsonInput) {
        ConnectionProvider connectionProvider = persistence.getConnectionProvider(databaseName);
        return saveConnectionProvider(connectionProvider, new JSONObject(jsonInput), this::doSaveConnectionProvider);
    }

    protected Response saveConnectionProvider(
            ConnectionProvider connectionProvider, JSONObject jsonObject,
            BiFunction<ConnectionProvider, Form, Response> handler) {
        ConnectionProviderDetail cp = new ConnectionProviderDetail(connectionProvider);
        Form form = new FormBuilder(ConnectionProviderDetail.class).configMode(Mode.EDIT).build();
        Field jndiResource = form.findFieldByPropertyName("jndiResource");
        if(connectionProvider instanceof JndiConnectionProvider) {
            form.get(0).clear();
            form.get(0).add(jndiResource);
        } else {
            form.get(0).remove(jndiResource);
        }
        form.readFromObject(cp);
        FormUtil.readFromJson(form, jsonObject);
        if(form.validate()) {
            if(jsonObject.has("schemas")) {
                JSONArray schemasJson = jsonObject.getJSONArray("schemas");
                updateSchemas(connectionProvider, schemasJson,
                        (database, schema) -> database.getSchemas().remove(schema));
            }
            form.writeToObject(cp);
            return handler.apply(connectionProvider, form);
        } else {
            return Response.serverError().entity(form).build();
        }
    }

    public Response doSaveConnectionProvider(ConnectionProvider connectionProvider, Form form) {
        connectionProvider.init(persistence.getDatabasePlatformsRegistry());
        persistence.initModel();
        try {
            persistence.saveModel();
            String connectionsWithSchemas =
                    connectionWithSchemas(connectionProvider.getDatabase().getDatabaseName(), connectionProvider, form);
            return Response.ok(connectionsWithSchemas).build();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @PUT
    @Path("{databaseName}/schemas")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TableInfo> selectSchemas(@PathParam("databaseName") String databaseName, String jsonInput) throws Exception {
        ConnectionProvider connectionProvider = persistence.getConnectionProvider(databaseName);
        if(connectionProvider == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        updateSchemas(connectionProvider, new JSONArray(jsonInput), (database, schema) -> schema.ensureAnnotation(ExcludeFromWizard.class));
        persistence.syncDataModel(databaseName);
        persistence.initModel();
        persistence.saveModel();
        logger.info("Schemas for database {} updated", databaseName);
        List<TableInfo> tableInfos = determineRoots(connectionProvider.getDatabase().getSchemas());
        tableInfos.sort(Comparator.comparing(t -> t.table.getQualifiedName()));
        return tableInfos;
    }

    protected List<TableInfo> determineRoots(List<Schema> schemas) {
        List<TableInfo> roots = new ArrayList<>();
        Multimap<Table, Reference> children = ArrayListMultimap.create();
        for(Schema schema : schemas) {
            if(!schema.getAnnotation(ExcludeFromWizard.class).isPresent()) {
                for(Table table : schema.getTables()) {
                    roots.add(new TableInfo(table));
                }
            }
        }
        for(Iterator<TableInfo> it = roots.iterator(); it.hasNext();) {
            TableInfo tableInfo = it.next();
            Table table = tableInfo.table;
            //Exclude tables with no primary key
            if(table.getPrimaryKey() == null) {
                it.remove();
                continue;
            }
            //Exclude Liquibase tables
            if("databasechangeloglock".equalsIgnoreCase(table.getTableName())) {
                it.remove();
                continue;
            }

            for(ForeignKey fk : table.getForeignKeys()) {
                for(Reference ref : fk.getReferences()) {
                    Column column = ref.getActualToColumn();
                    if(column.getTable() != table) {
                        children.put(column.getTable(), ref);
                        //TODO potrebbe essere un ciclo nel grafo...
                        tableInfo.root = false;
                    }
                }
            }
            for(ModelSelectionProvider sp : table.getSelectionProviders()) {
                for(Reference ref : sp.getReferences()) {
                    Column column = ref.getActualToColumn();
                    if(column != null && column.getTable() != table) {
                        children.put(column.getTable(), ref);
                        //TODO potrebbe essere un ciclo nel grafo...
                        tableInfo.root = false;
                    }
                }
            }
        }
        roots.forEach(i -> {
            Collection<Reference> c = children.get(i.table);
            if(c != null) {
                i.children.addAll(c);
            }
        });
        return roots;
    }

    public void updateSchemas(
            ConnectionProvider connectionProvider, JSONArray schemasJson, BiConsumer<Database, Schema> actionOnNonSelected) {
        Database database = connectionProvider.getDatabase();
        List<Schema> schemas = database.getSchemas();
        List<String> schemaNames = new ArrayList<>(schemas.size());
        for(Schema schema : schemas) {
            schemaNames.add(schema.getActualSchemaName());
        }
        for(Object schemaObject : schemasJson) {
            JSONObject schema = (JSONObject) schemaObject;
            boolean selected = schema.getBoolean("selected");
            String logicalName = schema.getString("schema");
            String physicalName = schema.optString("name");
            if(StringUtils.isEmpty(physicalName)) {
                physicalName = logicalName;
            }
            if(selected) {
                if(!schemaNames.contains(physicalName)) {
                    Schema modelSchema = new Schema();
                    modelSchema.setConfiguration(portofinoConfiguration.getProperties());
                    modelSchema.setDatabase(database);
                    modelSchema.setSchemaName(logicalName);
                    if(!schema.isNull("catalog")) {
                        modelSchema.setCatalog(schema.getString("catalog"));
                    }
                    modelSchema.setActualSchemaName(physicalName);
                    database.getSchemas().add(modelSchema);
                } else {
                    for(Schema aSchema : database.getSchemas()) {
                        if(aSchema.getActualSchemaName().equals(physicalName)) {
                            aSchema.getAnnotations().removeIf(
                                    a -> a.getType().equals(ExcludeFromWizard.class.getName()));
                            break;
                        }
                    }
                }
            } else if(schemaNames.contains(physicalName)) {
                for(Schema aSchema : database.getSchemas()) {
                    if(aSchema.getActualSchemaName().equals(physicalName)) {
                        actionOnNonSelected.accept(database, aSchema);
                        break;
                    }
                }
            }
        }
    }

    protected List<SelectableSchema> getSchemas(ConnectionProvider connectionProvider) throws Exception {
        try(Connection conn = connectionProvider.acquireConnection()) {
            logger.debug("Reading database metadata");
            DatabaseMetaData metadata = conn.getMetaData();
            List<String[]> schemaNamesFromDb =
                    connectionProvider.getDatabasePlatform().getSchemaNames(metadata);

            List<Schema> selectedSchemas = connectionProvider.getDatabase().getSchemas();

            List<SelectableSchema> selectableSchemas = new ArrayList<>(schemaNamesFromDb.size());
            for(String[] schemaName : schemaNamesFromDb) {
                boolean selected = schemaNamesFromDb.size() == 1;
                String logicalName = schemaName[1].toLowerCase();
                String physicalName = schemaName[1];
                if(physicalName.equals(logicalName)) {
                    physicalName = null;
                }
                for(Schema schema : selectedSchemas) {
                    if(schemaName[1].equals(schema.getActualSchemaName())) {
                        selected = true;
                        logicalName = schema.getSchemaName();
                        if(!schemaName[1].equals(logicalName)) {
                            physicalName = schemaName[1];
                        }
                        break;
                    }
                }
                SelectableSchema schema = new SelectableSchema(schemaName[0], physicalName, logicalName, selected);
                selectableSchemas.add(schema);
            }
            return selectableSchemas;
        }
    }

    @DELETE
    @Path("{databaseName}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteConnection(@PathParam("databaseName") String databaseName) throws Exception {
        Database database =
                DatabaseLogic.findDatabaseByName(persistence.getDatabases(), databaseName);
        if (database == null) {
            throw new WebApplicationException("Delete failed. Connection provider not found: " + databaseName);
        } else {
            persistence.removeDatabase(database);
            persistence.initModel();
            persistence.saveModel();
            logger.info("Database {} deleted", databaseName);
        }
    }

    @POST
    @Path("{databaseName}/:synchronize")
    public void synchronize(@PathParam("databaseName") String databaseName) throws Exception {
        if(DatabaseLogic.findDatabaseByName(persistence.getDatabases(), databaseName) == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        persistence.syncDataModel(databaseName);
        persistence.initModel();
        persistence.saveModel();
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
