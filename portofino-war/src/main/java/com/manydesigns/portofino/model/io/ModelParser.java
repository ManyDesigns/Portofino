package com.manydesigns.portofino.model.io;

import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.datamodel.*;
import com.manydesigns.portofino.model.portlets.Portlet;
import com.manydesigns.portofino.model.site.SiteNode;
import com.manydesigns.portofino.model.usecases.Access;
import com.manydesigns.portofino.model.usecases.UseCase;
import com.manydesigns.portofino.model.usecases.UseCaseProperty;
import com.manydesigns.portofino.xml.DocumentCallback;
import com.manydesigns.portofino.xml.ElementCallback;
import com.manydesigns.portofino.xml.XmlParser;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Angelo    Lupo       - angelo.lupo@manydesigns.com
 * @author Paolo     Predonzani - paolo.predonzani@manydesigns.com
 */
public class ModelParser extends XmlParser {
    private static final String MODEL = "model";

    private static final String DATABASES = "databases";
    private static final String DATABASE = "database";
    private static final String SCHEMAS = "schemas";
    private static final String SCHEMA = "schema";
    private static final String TABLES = "tables";
    private static final String TABLE = "table";
    private static final String COLUMNS = "columns";
    private static final String COLUMN = "column";
    private static final String PRIMARY_KEY = "primaryKey";
    private static final String RELATIONSHIPS = "relationships";
    private static final String RELATIONSHIP = "relationship";
    private static final String REFERENCE = "reference";

    private static final String SITENODES = "siteNodes";
    private static final String SITENODE = "siteNode";
    private static final String CHILDNODES = "childNodes";

    private static final String PORTLETS = "portlets";
    private static final String PORTLET = "portlet";

    private static final String USECASES = "useCases";
    private static final String USECASE = "useCase";
    private static final String PROPERTIES = "properties";
    private static final String PROPERTY = "property";

    private List<RelationshipPre> relationships;

    Model model;
    Database currentDatabase;
    Schema currentSchema;
    Table currentTable;
    UseCase currentUseCase;

    public ModelParser() {}

    public Model parse(String fileName) throws Exception {
        model = new Model();
        relationships = new ArrayList<RelationshipPre>();
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        InputStream input = ReflectionUtil.getResourceAsStream(fileName);
        XMLStreamReader xmlStreamReader = inputFactory.createXMLStreamReader(input);
        initParser(xmlStreamReader);
        expectDocument(new ModelDocumentCallback());
        createRelationshipsPost();
        return model;
    }

    private class ModelDocumentCallback implements DocumentCallback {
        public void doDocument() throws XMLStreamException {
            expectElement(MODEL, 1, 1, new ModelCallback());
        }
    }

    private class ModelCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(DATABASES, 1, 1, new DatabasesCallback());
            expectElement(SITENODES, 1, 1, new SiteNodesCallback());
            expectElement(PORTLETS, 0, 1, new PortletsCallback());
            expectElement(USECASES, 0, 1, new UseCasesCallback());
        }
    }

    //**************************************************************************
    // datamodel/databases
    //**************************************************************************

    private class DatabasesCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(DATABASE, 1, null, new DatabaseCallback());
        }
    }

    private class DatabaseCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            checkRequiredAttributes(attributes, "name");
            currentDatabase = new Database(attributes.get("name"));
            model.getDatabases().add(currentDatabase);
            expectElement(SCHEMAS, 1, 1, new SchemasCallback());

        }
    }

    private class SchemasCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(SCHEMA, 0, null, new SchemaCallback());
        }
    }

    private class SchemaCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            checkRequiredAttributes(attributes, "name");
            currentSchema =
                    new Schema(currentDatabase.getDatabaseName(),
                            attributes.get("name"));
            currentDatabase.getSchemas().add(currentSchema);
            expectElement(TABLES, 0, 1, new TablesCallback());
        }
    }

    private class TablesCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(TABLE, 0, null, new TableCallback());
        }
    }

    private class TableCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            checkRequiredAttributes(attributes, "name");
            currentTable =
                    new Table(currentSchema.getDatabaseName(),
                            currentSchema.getSchemaName(),
                            attributes.get("name"));
            String m2m = attributes.get("manyToMany");
            if (m2m!=null) {
                currentTable.setM2m(Boolean.parseBoolean(m2m));
            }
            String className = attributes.get("class");
            if (className!=null){
                currentTable.setClassName(className);
            }
            currentSchema.getTables().add(currentTable);
            expectElement(COLUMNS, 1, 1, new ColumnsCallback());
            expectElement(PRIMARY_KEY, 1, 1, new PrimaryKeyCallback());
            expectElement(RELATIONSHIPS, 0, 1, new RelationshipsCallback());
        }
    }

    private class ColumnsCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(COLUMN, 1, null, new ColumnCallback());
        }
    }

    private class ColumnCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            checkRequiredAttributes(attributes,
                    "name", "columnType", "length", "scale",
                    "nullable", "searchable");
            String columnName = attributes.get("name");
            Column column =
                    new Column(currentTable.getDatabaseName(),
                            currentTable.getSchemaName(),
                            currentTable.getTableName(),
                            columnName,
                            attributes.get("columnType"),
                            Boolean.parseBoolean(attributes.get("nullable")),
                            Boolean.parseBoolean(attributes.get("autoincrement")),
                            Integer.parseInt(attributes.get("length")),
                            Integer.parseInt(attributes.get("scale")),
                            Boolean.parseBoolean(attributes.get("searchable"))
                            );

            String propertyName = attributes.get("propertyName");
            if (propertyName == null) {
                propertyName = columnName;
            }
            column.setPropertyName(propertyName);

            Class javatype = ReflectionUtil.loadClass(attributes.get("javaType"));
            column.setJavaType(javatype);

            currentTable.getColumns().add(column);
        }
    }

    private class PrimaryKeyCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            checkRequiredAttributes(attributes, "name");
            PrimaryKey pk = new PrimaryKey(currentTable.getDatabaseName(),
                            currentTable.getSchemaName(),
                            currentTable.getTableName(),
                            attributes.get("name"));
            currentTable.setPrimaryKey(pk);
            pk.setClassName(attributes.get("className"));
            expectElement(COLUMN, 1, null, new PrimaryKeyColumnCallback());
        }
    }

    private class PrimaryKeyColumnCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            checkRequiredAttributes(attributes, "name");
            String columnName = attributes.get("name");
            Column column = getColumn(currentTable, columnName);
            currentTable.getPrimaryKey().getColumns().add(column);
        }
    }

    private class RelationshipsCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(RELATIONSHIP, 1, null, new RelationshipCallback());
        }
    }

    private class RelationshipCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            checkRequiredAttributes(attributes,
                    "name", "toSchema", "toTable", "onUpdate", "onDelete");
            RelationshipPre rel =
                    new RelationshipPre(
                            currentTable.getDatabaseName(),
                            currentTable.getDatabaseName(),
                            currentTable.getSchemaName(),
                            attributes.get("toSchema"),
                            currentTable.getTableName(),
                            attributes.get("toTable"),
                            attributes.get("name"),
                            attributes.get("onUpdate"),
                            attributes.get("onDelete"));
            
            String manyPropertyName = attributes.get("manyPropertyName");
            if (manyPropertyName == null) {
                manyPropertyName = rel.getRelationshipName();
            }
            rel.setManyPropertyName(manyPropertyName);

            String onePropertyName = attributes.get("onePropertyName");
            if (onePropertyName == null) {
                onePropertyName = rel.getRelationshipName();
            }
            rel.setOnePropertyName(onePropertyName);
            relationships.add(rel);
            expectElement(REFERENCE, 1, null, new ReferenceCallback());
        }
    }

    private class ReferenceCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            RelationshipPre currRel = relationships.get(relationships.size()-1);
            checkRequiredAttributes(attributes,
                                "fromColumn", "toColumn");
            ReferencePre referencePre = new ReferencePre(
                attributes.get("fromColumn"),
                attributes.get("toColumn"));
            currRel.getReferences().add(referencePre);            
        }
    }

    //**************************************************************************
    // Site nodes
    //**************************************************************************

    private class SiteNodesCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(SITENODE, 1, null,
                    new SiteNodeCallback(model.getSiteNodes()));
        }
    }

    private class SiteNodeCallback implements ElementCallback {
        private final List<SiteNode> parentNodes;

        private SiteNodeCallback(List<SiteNode> parentNodes) {
            this.parentNodes = parentNodes;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            checkRequiredAttributes(attributes,
                    "url", "title", "description");
            String type = attributes.get("type");
            if (type == null) {
                type = "simple";
            }
            String url = attributes.get("url");
            String title = attributes.get("title");
            String description = attributes.get("description");
            SiteNode currentSiteNode =
                    new SiteNode(type, url, title, description);
            parentNodes.add(currentSiteNode);
            expectElement(CHILDNODES, 0, 1,
                    new ChildNodesCallback(currentSiteNode.getChildNodes()));
        }
    }

    private class ChildNodesCallback implements ElementCallback {
        private final List<SiteNode> parentNodes;

        private ChildNodesCallback(List<SiteNode> parentNodes) {
            this.parentNodes = parentNodes;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(SITENODE, 1, null, new SiteNodeCallback(parentNodes));
        }
    }


    //**************************************************************************
    // Portlets
    //**************************************************************************

    private class PortletsCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(PORTLET, 0, null, new PortletCallback());
        }
    }

    private class PortletCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            checkRequiredAttributes(attributes,
                    "name", "type", "title", "legend", "database",
                    "sql", "urlExpression");
            String name = attributes.get("name");
            String type = attributes.get("type");
            String title = attributes.get("title");
            String legend = attributes.get("legend");
            String database = attributes.get("database");
            String sql = attributes.get("sql");
            String urlExpression = attributes.get("urlExpression");
            Portlet portlet =
                    new Portlet(name, type, title, legend, database,
                            sql, urlExpression);
            model.getPortlets().add(portlet);
        }
    }



    //**************************************************************************
    // Use cases
    //**************************************************************************

    private class UseCasesCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(USECASE, 0, null, new UseCaseCallback());
        }
    }

    private class UseCaseCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            checkRequiredAttributes(attributes,
                    "name", "title", "table", "filter");
            String name = attributes.get("name");
            String title = attributes.get("title");
            String tableName = attributes.get("table");
            String filter = attributes.get("filter");
            currentUseCase = new UseCase(name, title, tableName, filter);
            Table table = model.findTableByQualifiedName(tableName);
            currentUseCase.setTable(table);
            model.getUseCases().add(currentUseCase);
            expectElement(PROPERTIES, 0, 1, new PropertiesCallback());
        }
    }

    private class PropertiesCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(PROPERTY, 0, null, new PropertyCallback());
        }
    }

    private class PropertyCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            checkRequiredAttributes(attributes,
                    "name", "inSummary", "searchable", "access");
            String name = attributes.get("name");
            String inSummary = attributes.get("inSummary");
            String searchable = attributes.get("searchable");
            String access = attributes.get("access");
            UseCaseProperty useCaseProperty =
                    new UseCaseProperty(
                            name,
                            Boolean.parseBoolean(inSummary),
                            Boolean.parseBoolean(searchable),
                            Access.parseAccess(access));
            String label = attributes.get("label");
            useCaseProperty.setLabel(label);
            currentUseCase.getProperties().add(useCaseProperty);
        }
    }


    //**************************************************************************
    // utility methods
    //**************************************************************************

    private void createRelationshipsPost() {
        for (RelationshipPre relPre: relationships) {
            Relationship rel = new Relationship(relPre.getRelationshipName(),
                    relPre.getOnUpdate(), relPre.getOnDelete());
            final Table fromTable = getTable(relPre.getFromSchema(), relPre.getFromTable());
            final Table toTable = getTable(relPre.getToSchema(), relPre.getToTable());
            rel.setFromTable(fromTable);
            rel.setToTable(toTable);
            rel.setManyPropertyName(relPre.getManyPropertyName());
            rel.setOnePropertyName(relPre.getOnePropertyName());
            fromTable.getManyToOneRelationships().add(rel);
            toTable.getOneToManyRelationships().add(rel);

            for (ReferencePre refPre: relPre.getReferences()) {
                Reference ref = new Reference(getColumn(fromTable, refPre.getFromColumn()),
                        getColumn(toTable, refPre.getToColumn()));
                rel.getReferences().add(ref);
            }
        }
    }

    private Table getTable(String schemaName, String tableName) {
        for (Database db : model.getDatabases()) {
            for (Schema schema : db.getSchemas()) {
                if (schemaName.equals(schema.getSchemaName())){
                    for (Table tb : schema.getTables()) {
                        if(tableName.equals(tb.getTableName()))
                            return tb;
                    }
                }
            }

        }
        throw new Error(MessageFormat.format("Tabella {0} non presente", tableName));

    }

    private Column getColumn(Table table, String attValue) {
        for (Column col : table.getColumns()) {
            if (col.getColumnName().equals(attValue)) {
                return col;
            }
        }
        throw new Error(MessageFormat.format("Colonna {0} non presente", attValue));
    }
}

