package com.manydesigns.portofino.model.io;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.annotations.ModelAnnotation;
import com.manydesigns.portofino.model.datamodel.*;
import com.manydesigns.portofino.model.portlets.Portlet;
import com.manydesigns.portofino.model.site.SiteNode;
import com.manydesigns.portofino.model.usecases.UseCase;
import com.manydesigns.portofino.model.usecases.UseCaseProperty;
import com.manydesigns.portofino.xml.CharactersCallback;
import com.manydesigns.portofino.xml.DocumentCallback;
import com.manydesigns.portofino.xml.ElementCallback;
import com.manydesigns.portofino.xml.XmlParser;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Angelo    Lupo       - angelo.lupo@manydesigns.com
 * @author Paolo     Predonzani - paolo.predonzani@manydesigns.com
 */
public class ModelParser extends XmlParser {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    public static final String MODEL = "model";

    public static final String DATABASES = "databases";

    public static final String DATABASE = "database";
    public static final String DATABASE_DATABASENAME = "databaseName"; // required

    public static final String SCHEMAS = "schemas";

    public static final String SCHEMA = "schema";
    public static final String SCHEMA_SCHEMANAME = "schemaName"; // required

    public static final String TABLES = "tables";

    public static final String TABLE = "table";
    public static final String TABLE_TABLENAME = "tableName"; // required
    public static final String TABLE_JAVACLASS = "javaClass";
    public static final String TABLE_MANYTOMANY = "manyToMany";

    public static final String COLUMNS = "columns";

    public static final String COLUMN = "column";
    public static final String COLUMN_COLUMNNAME = "columnName"; // required
    public static final String COLUMN_COLUMNTYPE = "columnType"; // required
    public static final String COLUMN_LENGTH = "length"; // required
    public static final String COLUMN_SCALE = "scale"; // required
    public static final String COLUMN_NULLABLE = "nullable"; // required
    public static final String COLUMN_SEARCHABLE = "searchable"; // required
    public static final String COLUMN_AUTOINCREMENT = "autoincrement"; // required
    public static final String COLUMN_PROPERTYNAME = "propertyName";
    public static final String COLUMN_JAVATYPE = "javaType";

    public static final String PRIMARYKEY = "primaryKey";
    public static final String PRIMARYKEY_PRIMARYKEYNAME = "primaryKeyName"; // required
    public static final String PRIMARYKEY_CLASSNAME = "className";

    public static final String PRIMARYKEYCOLUMN = COLUMN;
    public static final String PRIMARYKEYCOLUMN_COLUMNNAME = COLUMN_COLUMNNAME; // required

    public static final String FOREIGNKEYS = "foreignKeys";

    public static final String FOREIGNKEY = "foreignKey";
    public static final String FOREIGNKEY_FOREIGNKEYNAME = "foreignKeyName"; // required
    public static final String FOREIGNKEY_TODATABASE = "toDatabase"; // required
    public static final String FOREIGNKEY_TOSCHEMA = "toSchema"; // required
    public static final String FOREIGNKEY_TOTABLE = "toTable"; // required
    public static final String FOREIGNKEY_ONUPDATE = "onUpdate"; // required
    public static final String FOREIGNKEY_ONDELETE = "onDelete"; // required
    public static final String FOREIGNKEY_MANYPROPERTYNAME = "manyPropertyName";
    public static final String FOREIGNKEY_ONEPROPERTYNAME = "onePropertyName";

    public static final String REFERENCES = "references";

    public static final String REFERENCE = "reference";
    public static final String REFERENCE_FROMCOLUMN = "fromColumn"; // required
    public static final String REFERENCE_TOCOLUMN = "toColumn"; // required

    public static final String SITENODES = "siteNodes";

    public static final String SITENODE = "siteNode";
    public static final String SITENODE_TYPE = "type"; // required
    public static final String SITENODE_URL = "url"; // required
    public static final String SITENODE_TITLE = "title"; // required
    public static final String SITENODE_DESCRIPTION = "description"; // required

    public static final String CHILDNODES = "childNodes";

    public static final String PORTLETS = "portlets";

    public static final String PORTLET = "portlet";
    public static final String PORTLET_NAME = "name"; // required
    public static final String PORTLET_TYPE = "type"; // required
    public static final String PORTLET_TITLE = "title"; // required
    public static final String PORTLET_LEGEND = "legend"; // required
    public static final String PORTLET_DATABASE = "database"; // required
    public static final String PORTLET_SQL = "sql"; // required
    public static final String PORTLET_URLEXPRESSION = "urlExpression"; // required

    public static final String USECASES = "useCases";

    public static final String USECASE = "useCase";
    public static final String USECASE_NAME = "name"; // required
    public static final String USECASE_TITLE = "title"; // required
    public static final String USECASE_TABLE = "table"; // required
    public static final String USECASE_FILTER = "filter"; // required

    public static final String PROPERTIES = "properties";

    public static final String PROPERTY = "property";
    public static final String PROPERTY_NAME = "name"; // required

    public static final String ANNOTATIONS = "annotations";

    public static final String ANNOTATION = "annotation";
    public static final String ANNOTATION_TYPE = "type"; // required

    public static final String VALUE = "value";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    Model model;
    Database currentDatabase;
    Schema currentSchema;
    Table currentTable;
    Column currentColumn;
    PrimaryKey currentPk;
    ForeignKey currentFk;
    UseCase currentUseCase;

    Collection<ModelAnnotation> currentModelAnnotations;
    ModelAnnotation currentModelAnnotation;


    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger = LogUtil.getLogger(ModelParser.class);

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    public ModelParser() {}

    //--------------------------------------------------------------------------
    // Parsing
    //--------------------------------------------------------------------------

    public Model parse(String resourceName) throws Exception {
        InputStream inputStream = ReflectionUtil.getResourceAsStream(resourceName);
        return parse(inputStream);
    }

    public Model parse(File file) throws Exception {
        LogUtil.infoMF(logger, "Parsing file: {0}", file.getAbsolutePath());
        InputStream input = new FileInputStream(file);
        return parse(input);
    }

    private Model parse(InputStream inputStream) throws XMLStreamException {
        model = new Model();
        initParser(inputStream);
        expectDocument(new ModelDocumentCallback());
        model.init();
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
            expectElement(DATABASES, 0, 1, new DatabasesCallback());
            expectElement(SITENODES, 0, 1, new SiteNodesCallback());
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
            checkRequiredAttributes(attributes, DATABASE_DATABASENAME);
            currentDatabase = new Database(attributes.get(DATABASE_DATABASENAME));
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
            checkRequiredAttributes(attributes, SCHEMA_SCHEMANAME);
            currentSchema =
                    new Schema(currentDatabase, attributes.get(SCHEMA_SCHEMANAME));
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
            checkRequiredAttributes(attributes, TABLE_TABLENAME);
            currentTable =
                    new Table(currentSchema, attributes.get(TABLE_TABLENAME));
            String m2m = attributes.get(TABLE_MANYTOMANY);
            if (m2m!=null) {
                currentTable.setM2m(Boolean.parseBoolean(m2m));
            }
            String javaClassName = attributes.get(TABLE_JAVACLASS);
            currentTable.setJavaClassName(javaClassName);
            currentSchema.getTables().add(currentTable);

            expectElement(COLUMNS, 1, 1, new ColumnsCallback());
            expectElement(PRIMARYKEY, 0, 1, new PrimaryKeyCallback());
            expectElement(FOREIGNKEYS, 0, 1, new ForeignKeysCallback());

            currentModelAnnotations = currentTable.getModelAnnotations();
            expectElement(ANNOTATIONS, 0, 1, new AnnotationsCallback());
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
                    COLUMN_COLUMNNAME, COLUMN_COLUMNTYPE, COLUMN_LENGTH, COLUMN_SCALE,
                    COLUMN_NULLABLE, COLUMN_SEARCHABLE, COLUMN_AUTOINCREMENT);
            String columnName = attributes.get(COLUMN_COLUMNNAME);
            currentColumn = new Column(currentTable,
                    columnName,
                    attributes.get(COLUMN_COLUMNTYPE),
                    Boolean.parseBoolean(attributes.get(COLUMN_NULLABLE)),
                    Boolean.parseBoolean(attributes.get(COLUMN_AUTOINCREMENT)),
                    Integer.parseInt(attributes.get(COLUMN_LENGTH)),
                    Integer.parseInt(attributes.get(COLUMN_SCALE)),
                    Boolean.parseBoolean(attributes.get(COLUMN_SEARCHABLE))
                    );

            String propertyName = attributes.get(COLUMN_PROPERTYNAME);
            currentColumn.setPropertyName(propertyName);

            String javaTypeName = attributes.get(COLUMN_JAVATYPE);
            currentColumn.setJavaTypeName(javaTypeName);

            currentTable.getColumns().add(currentColumn);

            currentModelAnnotations = currentColumn.getModelAnnotations();
            expectElement(ANNOTATIONS, 0, 1, new AnnotationsCallback());
        }
    }

    private class PrimaryKeyCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            checkRequiredAttributes(attributes, PRIMARYKEY_PRIMARYKEYNAME);
            currentPk = new PrimaryKey(currentTable, attributes.get(PRIMARYKEY_PRIMARYKEYNAME));
            currentTable.setPrimaryKey(currentPk);
            currentPk.setClassName(attributes.get(PRIMARYKEY_CLASSNAME));

            expectElement(PRIMARYKEYCOLUMN, 1, null, new PrimaryKeyColumnCallback());
        }
    }

    private class PrimaryKeyColumnCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            checkRequiredAttributes(attributes, PRIMARYKEYCOLUMN_COLUMNNAME);
            String columnName = attributes.get(PRIMARYKEYCOLUMN_COLUMNNAME);
            PrimaryKeyColumn pkColumn =
                    new PrimaryKeyColumn(currentPk, columnName);
            currentPk.getPrimaryKeyColumns().add(pkColumn);
        }
    }

    private class ForeignKeysCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(FOREIGNKEY, 1, null, new ForeignKeyCallback());
        }
    }

    private class ForeignKeyCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            checkRequiredAttributes(attributes,
                    FOREIGNKEY_FOREIGNKEYNAME, FOREIGNKEY_TODATABASE,
                    FOREIGNKEY_TOSCHEMA, FOREIGNKEY_TOTABLE,
                    FOREIGNKEY_ONUPDATE, FOREIGNKEY_ONDELETE);
            currentFk = new ForeignKey(
                    currentTable,
                    attributes.get(FOREIGNKEY_FOREIGNKEYNAME),
                    attributes.get(FOREIGNKEY_TODATABASE),
                    attributes.get(FOREIGNKEY_TOSCHEMA),
                    attributes.get(FOREIGNKEY_TOTABLE),
                    attributes.get(FOREIGNKEY_ONUPDATE),
                    attributes.get(FOREIGNKEY_ONDELETE));
            
            String manyPropertyName = attributes.get(FOREIGNKEY_MANYPROPERTYNAME);
            if (manyPropertyName == null) {
                manyPropertyName = currentFk.getForeignKeyName();
            }
            currentFk.setManyPropertyName(manyPropertyName);

            String onePropertyName = attributes.get(FOREIGNKEY_ONEPROPERTYNAME);
            if (onePropertyName == null) {
                onePropertyName = currentFk.getForeignKeyName();
            }
            currentFk.setOnePropertyName(onePropertyName);
            currentTable.getForeignKeys().add(currentFk);

            expectElement(REFERENCES, 1, 1, new ReferencesCallback());

            currentModelAnnotations = currentFk.getModelAnnotations();
            expectElement(ANNOTATIONS, 0, 1, new AnnotationsCallback());
        }
    }

    private class ReferencesCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(REFERENCE, 1, null, new ReferenceCallback());
        }
    }

    private class ReferenceCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            checkRequiredAttributes(attributes,
                    REFERENCE_FROMCOLUMN, REFERENCE_TOCOLUMN);
            Reference reference = new Reference(currentFk,
                    attributes.get(REFERENCE_FROMCOLUMN),
                    attributes.get(REFERENCE_TOCOLUMN));
            currentFk.getReferences().add(reference);
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
                    SITENODE_TYPE, SITENODE_URL,
                    SITENODE_TITLE, SITENODE_DESCRIPTION);
            String type = attributes.get(SITENODE_TYPE);
            String url = attributes.get(SITENODE_URL);
            String title = attributes.get(SITENODE_TITLE);
            String description = attributes.get(SITENODE_DESCRIPTION);
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
                    PORTLET_NAME, PORTLET_TYPE,
                    PORTLET_TITLE, PORTLET_LEGEND, PORTLET_DATABASE,
                    PORTLET_SQL, PORTLET_URLEXPRESSION);
            String name = attributes.get(PORTLET_NAME);
            String type = attributes.get(PORTLET_TYPE);
            String title = attributes.get(PORTLET_TITLE);
            String legend = attributes.get(PORTLET_LEGEND);
            String database = attributes.get(PORTLET_DATABASE);
            String sql = attributes.get(PORTLET_SQL);
            String urlExpression = attributes.get(PORTLET_URLEXPRESSION);
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
                    USECASE_NAME, USECASE_TITLE, USECASE_TABLE, USECASE_FILTER);
            String name = attributes.get(USECASE_NAME);
            String title = attributes.get(USECASE_TITLE);
            String tableName = attributes.get(USECASE_TABLE);
            String filter = attributes.get(USECASE_FILTER);
            currentUseCase = new UseCase(name, title, tableName, filter);
            Table table = model.findTableByQualifiedName(tableName);
            currentUseCase.setTable(table);
            model.getUseCases().add(currentUseCase);
            expectElement(PROPERTIES, 0, 1, new PropertiesCallback());
            
            currentModelAnnotations = currentUseCase.getAnnotations();
            expectElement(ANNOTATIONS, 0, 1, new AnnotationsCallback());
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
            checkRequiredAttributes(attributes, PROPERTY_NAME);
            String name = attributes.get(PROPERTY_NAME);
            UseCaseProperty useCaseProperty = new UseCaseProperty(name);
            currentUseCase.getProperties().add(useCaseProperty);

            currentModelAnnotations = useCaseProperty.getAnnotations();
            expectElement(ANNOTATIONS, 0, 1, new AnnotationsCallback());
        }
    }

    private class AnnotationsCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(ANNOTATION, 0, null, new AnnotationCallback());
        }
    }

    private class AnnotationCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            checkRequiredAttributes(attributes, ANNOTATION_TYPE);
            String type = attributes.get(ANNOTATION_TYPE);
            currentModelAnnotation = new ModelAnnotation(type);
            currentModelAnnotations.add(currentModelAnnotation);
            expectElement(VALUE, 0, null, new AnnotationValueCallback());
        }
    }

    private class AnnotationValueCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectCharacters(new AnnotationValueCharactersCallback());
        }
    }

    private class AnnotationValueCharactersCallback
            implements CharactersCallback {
        public void doCharacters(String text) throws XMLStreamException {
            currentModelAnnotation.getValues().add(text);
        }
    }

}

