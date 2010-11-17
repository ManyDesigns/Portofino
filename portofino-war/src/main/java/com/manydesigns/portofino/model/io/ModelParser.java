package com.manydesigns.portofino.model.io;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.annotations.ModelAnnotation;
import com.manydesigns.portofino.model.datamodel.*;
import com.manydesigns.portofino.model.site.*;
import com.manydesigns.portofino.model.site.usecases.Button;
import com.manydesigns.portofino.model.site.usecases.UseCase;
import com.manydesigns.portofino.model.site.usecases.UseCaseProperty;
import com.manydesigns.portofino.model.selectionproviders.ModelSelectionProvider;
import com.manydesigns.portofino.model.selectionproviders.SelectionProperty;
import com.manydesigns.portofino.model.site.SiteNode;
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
import java.util.Stack;
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
    public static final String SCHEMAS = "schemas";
    public static final String SCHEMA = "schema";
    public static final String TABLES = "tables";
    public static final String TABLE = "table";
    public static final String COLUMNS = "columns";
    public static final String COLUMN = "column";
    public static final String PRIMARYKEY = "primaryKey";
    public static final String PRIMARYKEYCOLUMN = COLUMN;
    public static final String FOREIGNKEYS = "foreignKeys";
    public static final String FOREIGNKEY = "foreignKey";
    public static final String REFERENCES = "references";
    public static final String REFERENCE = "reference";
    public static final String CHILDNODES = "childNodes";
    public static final String PORTLETS = "portlets";
    public static final String PORTLET = "portlet";
    public static final String USECASES = "useCases";
    public static final String USECASE = "useCase";
    public static final String BUTTONS = "buttons";
    public static final String BUTTON = "button";
    public static final String SUBUSECASE = "subUseCases";
    public static final String PROPERTIES = "properties";
    public static final String PROPERTY = "property";
    public static final String ANNOTATIONS = "annotations";
    public static final String ANNOTATION = "annotation";
    public static final String VALUE = "value";
    public static final String ROOTNODE = "rootNode";
    public static final String DOCUMENTNODE = "documentNode";
    public static final String FOLDER = "folder";
    public static final String CUSTOMNODE = "customNode";
    public static final String CUSTOMFOLDER = "customFolder";
    public static final String USECASENODE = "useCaseNode";
    public static final String PORTLETNODE = "portletNode";
    public static final String PERMISSIONS = "permissions";
    public static final String ALLOW = "allow";
    public static final String DENY = "deny";
    public static final String GROUP = "group";
    public static final String NAME = "name";
    public static final String SELECTIONPROVIDERS = "selectionProviders";
    public static final String SELECTIONPROVIDER = "selectionProvider";
    public static final String SELECTIONPROPERTIES = "selectionProperties";
    public static final String SELECTIONPROPERTY = "selectionProperty";

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
    Stack<UseCase> useCaseStack;

    Collection<ModelAnnotation> currentModelAnnotations;
    ModelAnnotation currentModelAnnotation;
    ModelSelectionProvider currentModelSelectionProvider;


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
        useCaseStack = new Stack<UseCase>();
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
            expectElement(ROOTNODE, 0, 1, new RootNodeCallback());
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
            currentDatabase = new Database();
            checkAndSetAttributes(currentDatabase, attributes);
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
            currentSchema = new Schema(currentDatabase);
            checkAndSetAttributes(currentSchema, attributes);
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
            currentTable = new Table(currentSchema);
            checkAndSetAttributes(currentTable, attributes);
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
            currentColumn = new Column(currentTable);
            checkAndSetAttributes(currentColumn, attributes);
            currentTable.getColumns().add(currentColumn);

            currentModelAnnotations = currentColumn.getModelAnnotations();
            expectElement(ANNOTATIONS, 0, 1, new AnnotationsCallback());
        }
    }

    private class PrimaryKeyCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            currentPk = new PrimaryKey(currentTable);
            checkAndSetAttributes(currentPk, attributes);
            currentTable.setPrimaryKey(currentPk);

            expectElement(PRIMARYKEYCOLUMN, 1, null, new PrimaryKeyColumnCallback());
        }
    }

    private class PrimaryKeyColumnCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            PrimaryKeyColumn pkColumn = new PrimaryKeyColumn(currentPk);
            checkAndSetAttributes(pkColumn, attributes);
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
            currentFk = new ForeignKey(currentTable);
            checkAndSetAttributes(currentFk, attributes);
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
            Reference reference = new Reference(currentFk);
            checkAndSetAttributes(reference, attributes);
            currentFk.getReferences().add(reference);
        }
    }

    //**************************************************************************
    // Site nodes
    //**************************************************************************

    private class RootNodeCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            RootNode root = new RootNode();
            model.setRoot(root);
            checkAndSetAttributes(root, attributes);
            expectElement(CHILDNODES, 0, 1,
                    new ChildNodesCallback(root));
            expectElement(PERMISSIONS, 0, 1,
                    new PermissionsCallback(root));
        }
    }

    private class ChildNodesCallback implements ElementCallback {
        private final SiteNode parent;

        private ChildNodesCallback(SiteNode parent) {
            this.parent = parent;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            String[] expectedTags = {DOCUMENTNODE, FOLDER, CUSTOMNODE,
                    CUSTOMFOLDER, USECASENODE, PORTLETNODE};
            ElementCallback[] callbackArray = {
                    new DocumentNodeCallback(parent), new FolderCallback(parent),
                    new CustomNodeCallback(parent), new CustomFolderCallback(parent),
                    new UseCaseNodeCallback(parent), new PortletCallback(parent)};
            expectElement(expectedTags, 0, null,
                    callbackArray);
        }
    }

    private class FolderCallback implements ElementCallback {
        SiteNode parentNode;

        private FolderCallback(SiteNode parent) {
            this.parentNode = parent;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            FolderNode node = new FolderNode(parentNode);
            checkAndSetAttributes(node, attributes);
            parentNode.getChildNodes().add(node);
            expectElement(CHILDNODES, 0, 1,
                    new ChildNodesCallback(node));
            expectElement(PERMISSIONS, 0, 1,
                    new PermissionsCallback(node));
        }
    }

    private class DocumentNodeCallback implements ElementCallback {
        SiteNode parentNode;

        private DocumentNodeCallback(SiteNode parent) {
            this.parentNode = parent;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            DocumentNode node = new DocumentNode(parentNode);
            checkAndSetAttributes(node, attributes);
            parentNode.getChildNodes().add(node);
            expectElement(PERMISSIONS, 0, 1,
                    new PermissionsCallback(node));
        }
    }

    private class CustomNodeCallback implements ElementCallback {
        SiteNode parentNode;

        private CustomNodeCallback(SiteNode parent) {
            this.parentNode = parent;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            CustomNode node = new CustomNode(parentNode);
            checkAndSetAttributes(node, attributes);
            parentNode.getChildNodes().add(node);
            expectElement(PERMISSIONS, 0, 1,
                    new PermissionsCallback(node));
        }
    }

    private class CustomFolderCallback implements ElementCallback {
        SiteNode parentNode;

        private CustomFolderCallback(SiteNode parent) {
            this.parentNode = parent;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            CustomFolderNode node = new CustomFolderNode(parentNode);
            checkAndSetAttributes(node, attributes);
            parentNode.getChildNodes().add(node);
            expectElement(PERMISSIONS, 0, 1,
                    new PermissionsCallback(node));
        }
    }

    private class PortletCallback implements ElementCallback {
        SiteNode parentNode;

        private PortletCallback(SiteNode parent) {
            this.parentNode = parent;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            PortletNode node = new PortletNode(parentNode);
            checkAndSetAttributes(node, attributes);
            parentNode.getChildNodes().add(node);
            expectElement(PERMISSIONS, 0, 1,
                    new PermissionsCallback(node));
        }
    }

    private class UseCaseNodeCallback implements ElementCallback {
        SiteNode parentNode;

        private UseCaseNodeCallback(SiteNode parent) {
            this.parentNode = parent;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            UseCaseNode node = new UseCaseNode(parentNode);
            checkAndSetAttributes(node, attributes);
            parentNode.getChildNodes().add(node);
            expectElement(USECASE, 1, null, new UseCaseCallback(node));
            expectElement(PERMISSIONS, 0, 1,
                    new PermissionsCallback(node));   
        }
    }






    //**************************************************************************
    // Use cases
    //**************************************************************************

    private class UseCaseCallback implements ElementCallback {
        UseCaseNode node;
        private UseCaseCallback(UseCaseNode node){
            this.node = node;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            UseCase parentUseCase;
            if (useCaseStack.isEmpty()) {
                parentUseCase = null;
            } else {
                parentUseCase = useCaseStack.peek();
            }

            UseCase currentUseCase = new UseCase(parentUseCase);
            if (parentUseCase == null) {
                node.setUseCase(currentUseCase);
            } else {
                parentUseCase.getSubUseCases().add(currentUseCase);
            }
            useCaseStack.push(currentUseCase);

            checkAndSetAttributes(currentUseCase, attributes);

            expectElement(PROPERTIES, 0, 1, new PropertiesCallback());

            expectElement(BUTTONS, 0, 1, new ButtonsCallback());

            currentModelAnnotations = currentUseCase.getModelAnnotations();
            expectElement(ANNOTATIONS, 0, 1, new AnnotationsCallback());

            expectElement(SELECTIONPROVIDERS, 0, 1,
                    new SelectionProvidersCallback());

            expectElement(SUBUSECASE, 0, 1, new SubUseCasesCallback());

            UseCase poppedUsedCase = useCaseStack.pop();
            assert(poppedUsedCase == currentUseCase);
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
            UseCase currentUseCase = useCaseStack.peek();

            UseCaseProperty useCaseProperty =
                    new UseCaseProperty(currentUseCase);
            checkAndSetAttributes(useCaseProperty, attributes);
            currentUseCase.getProperties().add(useCaseProperty);

            currentModelAnnotations = useCaseProperty.getAnnotations();
            expectElement(ANNOTATIONS, 0, 1, new AnnotationsCallback());
        }
    }

    private class ButtonsCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(BUTTON, 0, null, new ButtonCallback());
        }
    }

    private class ButtonCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            UseCase currentUseCase = useCaseStack.peek();
            Button button = new Button(currentUseCase);
            checkAndSetAttributes(button, attributes);
            currentUseCase.getButtons().add(button);
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
            currentModelAnnotation = new ModelAnnotation();
            checkAndSetAttributes(currentModelAnnotation, attributes);
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

    private class SubUseCasesCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(USECASE, 0, null, new UseCaseCallback(null));
        }
    }

    //**************************************************************************
    // Permissions
    //**************************************************************************
    private class PermissionsCallback implements ElementCallback {
        SiteNode node;

        private PermissionsCallback(SiteNode node) {
            this.node = node;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(ALLOW, 0, 1, new AllowCallback(node));
            expectElement(DENY, 0, 1, new DenyCallback(node));
        }
    }
    private class AllowCallback implements ElementCallback {
        SiteNode node;

        private AllowCallback(SiteNode node) {
            this.node = node;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(GROUP, 1, null, new GroupCallback(node.getAllowGroups()));
        }
    }

    private class DenyCallback implements ElementCallback {
        SiteNode node;

        private DenyCallback(SiteNode node) {
            this.node = node;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(GROUP, 1, null, new GroupCallback(node.getDenyGroups()));
        }
    }

    private class GroupCallback implements ElementCallback {
        List<String> groups;

        private GroupCallback(List<String> groups) {
            this.groups = groups;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            groups.add(attributes.get(NAME));

        }
    }

    private class SelectionProvidersCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(SELECTIONPROVIDER, 0, null, new SelectionProviderCallback());
        }
    }

    private class SelectionProviderCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            currentModelSelectionProvider = new ModelSelectionProvider();
            checkAndSetAttributes(currentModelSelectionProvider, attributes);
            UseCase currentUseCase = useCaseStack.peek();
            currentUseCase.getModelSelectionProviders()
                    .add(currentModelSelectionProvider);
            expectElement(SELECTIONPROPERTIES, 0, null,
                    new SelectionPropertiesCallback());
        }
    }

    private class SelectionPropertiesCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(SELECTIONPROPERTY, 0, null, new SelectionPropertyCallback());
        }
    }

    private class SelectionPropertyCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            SelectionProperty selectionProperty =
                    new SelectionProperty(currentModelSelectionProvider);
            checkAndSetAttributes(selectionProperty, attributes);
            currentModelSelectionProvider.getSelectionProperties()
                    .add(selectionProperty);
        }
    }
}

