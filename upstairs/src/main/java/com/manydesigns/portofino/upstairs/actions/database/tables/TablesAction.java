package com.manydesigns.portofino.upstairs.actions.database.tables;

import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.database.Schema;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.security.RequiresAdministrator;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.File;
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
            return tables;
        }
        schemaObj.getTables().forEach(table -> {
            tables.add(createLeaf(table.getTableName(), table.getActualEntityName()));
        });
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
