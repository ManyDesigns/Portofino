package com.manydesigns.portofino.model.io;

import com.manydesigns.portofino.model.*;
import com.manydesigns.portofino.xml.DocumentCallback;
import com.manydesigns.portofino.xml.ElementCallback;
import com.manydesigns.portofino.xml.XmlParser;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Angelo    Lupo       - angelo.lupo@manydesigns.com
 * @author Paolo     Predonzani - paolo.predonzani@manydesigns.com
 */
public class DBParser extends XmlParser {
    private static final String DATAMODEL = "datamodel";
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
    private List<RelationshipPre> relationships;
    protected ClassLoader classLoader;

    DataModel dataModel;
    Database currentDatabase;
    Schema currentSchema;
    Table currentTable;

    public DBParser() {
        classLoader = this.getClass().getClassLoader();
    }

    public DataModel parse(String fileName) throws Exception {
        dataModel = new DataModel();
        relationships = new ArrayList<RelationshipPre>();
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream input = cl.getResourceAsStream(fileName);
        XMLStreamReader xmlStreamReader = inputFactory.createXMLStreamReader(input);
        initParser(xmlStreamReader);
        expectDocument(new DatamodelDocumentCallback());
        createRelationshipsPost();
        return dataModel;
    }

    private class DatamodelDocumentCallback implements DocumentCallback {
        public void doDocument() throws XMLStreamException {
            expectElement(DATAMODEL, 1, 1, new DatamodelCallback());
        }
    }

    private class DatamodelCallback implements ElementCallback {
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
            dataModel.getDatabases().add(currentDatabase);
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
                    "name", "columnType", "length", "scale", "nullable");
            Column column =
                    new Column(currentTable.getDatabaseName(),
                            currentTable.getSchemaName(),
                            currentTable.getTableName(),
                            attributes.get("name"),
                            attributes.get("columnType"),
                            Boolean.parseBoolean(attributes.get("nullable")),
                            Integer.parseInt(attributes.get("length")),
                            Integer.parseInt(attributes.get("scale"))
                            );
            try {
                Class javatype = Class.forName(attributes.get("javaType"));
                column.setJavaType(javatype);
            } catch (ClassNotFoundException e) {
                throw new Error(e);
            }

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



    private void createRelationshipsPost() {
        for (RelationshipPre relPre: relationships) {
            Relationship rel = new Relationship(relPre.getRelationshipName(),
                    relPre.getOnUpdate(), relPre.getOnDelete());
            final Table fromTable = getTable(relPre.getFromSchema(), relPre.getFromTable());
            final Table toTable = getTable(relPre.getToSchema(), relPre.getToTable());
            rel.setFromTable(fromTable);
            rel.setToTable(toTable);
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
        for (Database db : dataModel.getDatabases()) {
            for (Schema schema : db.getSchemas()) {
                if (schemaName.equals(schema.getSchemaName())){
                    for (Table tb : schema.getTables()) {
                        if(tableName.equals(tb.getTableName()))
                            return tb;
                    }
                }
            }

        }
        throw new Error("Tabella non presente");

    }

    private Column getColumn(Table table, String attValue) {
        for (Column col : table.getColumns()) {
            if (col.getColumnName().equals(attValue)) {
                return col;
            }
        }
        throw new Error("Colonna non presente");
    }
}

