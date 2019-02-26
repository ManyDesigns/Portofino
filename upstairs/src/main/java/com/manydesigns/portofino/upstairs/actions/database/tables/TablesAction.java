package com.manydesigns.portofino.upstairs.actions.database.tables;

import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.security.RequiresAdministrator;
import org.apache.commons.lang.ArrayUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author Alessio Stalla - alessiostalla@gmail.com
 */
@RequiresAuthentication
@RequiresAdministrator
public class TablesAction extends AbstractPageAction {

    private static final Logger logger = LoggerFactory.getLogger(TablesAction.class);

    @Autowired
    protected Persistence persistence;

    @Path("{db}/{schema}")
    @GET
    public List<Map> getTables(@PathParam("db") String db, @PathParam("schema") String schema) {
        Schema schemaObj = DatabaseLogic.findSchemaByName(persistence.getModel(), db, schema);
        List<Map> tables = new ArrayList<>();
        if(schemaObj == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        schemaObj.getTables().forEach(table -> {
            tables.add(createLeaf(table.getTableName(), table.getActualEntityName()));
        });
        tables.sort(Comparator.comparing(o -> ((String) o.get("name"))));
        return tables;
    }

    @GET
    public List<Map> getTables() {
        List<Map> treeTables = new ArrayList<>();

        String lastDatabase = null;
        String lastSchema = null;
        Map schema = null;
        Map database = null;
        List<Table> tables = getAllTables();
        for(Table table : tables) {
            if(table.getPrimaryKey() == null) {
                continue;
            }
            if(!table.getDatabaseName().equals(lastDatabase)) {
                lastDatabase = table.getDatabaseName();
                database = createNode(table.getDatabaseName(),false);
                treeTables.add(database);
                lastSchema = null;
            }
            if(!table.getSchemaName().equals(lastSchema)) {
                String changelogFileNameTemplate = "{0}-changelog.xml";
                String changelogFileName = MessageFormat.format(
                        changelogFileNameTemplate, table.getDatabaseName() + "-" + table.getSchemaName());
                File changelogFile = new File(persistence.getAppDbsDir(), changelogFileName);
                String schemaDescr = table.getSchemaName();

                lastSchema = table.getSchemaName();
                schema = createNode(schemaDescr, changelogFile.isFile());
                ((List)database.get("children")).add(schema);
            }
            ((List)schema.get("children")).add(createLeaf(table.getTableName(), table.getActualEntityName()));
        }

        return treeTables;
    }

    @Path("{db}/{schema}/{table}")
    @GET
    public Map getTableInfo(
            @PathParam("db") String db, @PathParam("schema") String schema, @PathParam("table") String tableName) {
        Table table = DatabaseLogic.findTableByName(persistence.getModel(), db, schema, tableName);
        if(table == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        Map result = new HashMap();
        result.put("table", table);
        List<Map> typeInfo = new ArrayList<>();
        Type[] types = persistence.getConnectionProvider(db).getTypes();
        for(Column column : table.getColumns()) {
            Type type = null;
            for (Type candidate : types) {
                if (candidate.getJdbcType() == column.getJdbcType() &&
                        candidate.getTypeName().equalsIgnoreCase(column.getColumnType())) {
                    type = candidate;
                    break;
                }
            }
            if(type == null) {
                for (Type candidate : types) {
                    if (candidate.getJdbcType() == column.getJdbcType()) {
                        type = candidate;
                        break;
                    }
                }
            }
            Integer precision = column.getLength();
            Class[] javaTypes = type.getAvailableJavaTypes(precision);
            Integer scale = column.getScale();
            Map info = new HashMap();
            info.put("type", type);

            //Default
            Class defaultJavaType = Type.getDefaultJavaType(column.getJdbcType(), column.getColumnType(), precision, scale);
            if(defaultJavaType == null) {
                defaultJavaType = Object.class;
            }
            info.put("default", describeType(defaultJavaType));
            typeInfo.add(info);

            //Available
            List availableTypes = new ArrayList();
            info.put("types", availableTypes);
            //Existing
            try {
                Class existingType = Class.forName(column.getJavaType());
                if(!ArrayUtils.contains(javaTypes, existingType)) {
                    availableTypes.add(describeType(existingType));
                }
            } catch (Exception e) {
                logger.error("Invalid Java type", e);
            }
            for (Class c : javaTypes) {
                availableTypes.add(describeType(c));
            }
        }
        result.put("types", typeInfo);
        return result;
    }

    @Path("{db}/{schema}/{table}")
    @PUT
    public void saveTable(
            @PathParam("db") String db, @PathParam("schema") String schema, @PathParam("table") String tableName,
            Table table) throws IOException, JAXBException {
        Table existing = DatabaseLogic.findTableByName(persistence.getModel(), db, schema, tableName);
        if(existing == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        existing.setEntityName(table.getEntityName());
        existing.setJavaClass(table.getJavaClass());
        for(Column column : table.getColumns()) {
            Column c2 = DatabaseLogic.findColumnByName(existing, column.getColumnName());
            BeanUtils.copyProperties(column, c2);
            c2.setTable(existing);
        }
        persistence.initModel();
        persistence.saveXmlModel();
    }

    protected Map describeType(Class type) {
        Map description = new HashMap();
        description.put("name", type.getName());
        description.put("simpleName", getSimpleName(type));
        return description;
    }

    protected String getSimpleName(Class javaType) {
        if(javaType.isArray()) {
            return getSimpleName(javaType.getComponentType()) + "[]";
        } else if(javaType.isPrimitive()) {
            return javaType.getName();
        } else if(javaType.getPackage().getName().startsWith("java.")) {
            return javaType.getSimpleName();
        } else {
            return javaType.getName();
        }
    }

    private Map createNode(
            String name, boolean liquibase) {
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("liquibase", liquibase);
        map.put("children", new ArrayList<>());
        return map;
    }

    private Map createLeaf(String name, String entityName) {
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("entityName", entityName);
        return map;
    }

    public List<Table> getAllTables() {
        List<Table> tables = DatabaseLogic.getAllTables(persistence.getModel());
        Collections.sort(tables, (o1, o2) -> {
            int dbComp = o1.getDatabaseName().compareToIgnoreCase(o2.getDatabaseName());
            if(dbComp == 0) {
                int schemaComp = o1.getSchemaName().compareToIgnoreCase(o2.getSchemaName());
                if(schemaComp == 0) {
                    return o1.getTableName().compareToIgnoreCase(o2.getTableName());
                } else {
                    return schemaComp;
                }
            } else {
                return dbComp;
            }
        });
        return tables;
    }

}
