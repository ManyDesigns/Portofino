package com.manydesigns.portofino.model.io;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.annotations.ModelAnnotation;
import com.manydesigns.portofino.model.datamodel.*;
import com.manydesigns.portofino.model.selectionproviders.ModelSelectionProvider;
import com.manydesigns.portofino.model.selectionproviders.SelectionProperty;
import com.manydesigns.portofino.model.site.*;
import com.manydesigns.portofino.model.site.usecases.Button;
import com.manydesigns.portofino.model.site.usecases.UseCase;
import com.manydesigns.portofino.model.site.usecases.UseCaseProperty;
import com.manydesigns.portofino.xml.CharactersCallback;
import com.manydesigns.portofino.xml.DocumentCallback;
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
    public static final String SUBUSECASES = "subUseCases";
    public static final String PROPERTIES = "properties";
    public static final String PROPERTY = "property";
    public static final String ANNOTATIONS = "annotations";
    public static final String ANNOTATION = "annotation";
    public static final String VALUE = "value";
    public static final String ROOTNODE = "rootNode";
    public static final String DOCUMENTNODE = "documentNode";
    public static final String FOLDERNODE = "folderNode";
    public static final String CUSTOMNODE = "customNode";
    public static final String CUSTOMFOLDERNODE = "customFolderNode";
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
            expectElement(new ModelCallback());
        }
    }

    private class ModelCallback extends ElementCallback {
        private ModelCallback() {
            super(null, Model.class, MODEL, 1, 1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {


            expectElement(new DatabasesCallback(obj));
            expectElement(new RootNodeCallback());
        }
    }

    //**************************************************************************
    // datamodel/databases
    //**************************************************************************

    private class DatabasesCallback extends ElementCallback {
        private DatabasesCallback(Object parent) {
            super(parent, DATABASES, 0, 1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(new DatabaseCallback());
        }
    }

    private class DatabaseCallback extends ElementCallback {
        private DatabaseCallback() {
            super(Database.class, DATABASE, 1, -1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            currentDatabase = new Database();
            checkAndSetAttributes(currentDatabase, attributes);
            model.getDatabases().add(currentDatabase);
            expectElement(new SchemasCallback());

        }
    }

    private class SchemasCallback extends ElementCallback {
        private SchemasCallback() {
            super(SCHEMAS, 1, 1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(new SchemaCallback());
        }
    }

    private class SchemaCallback extends ElementCallback {
        private SchemaCallback() {
            super(SCHEMA, 0, -1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            currentSchema = new Schema(currentDatabase);
            checkAndSetAttributes(currentSchema, attributes);
            currentDatabase.getSchemas().add(currentSchema);
            expectElement(new TablesCallback());
        }
    }

    private class TablesCallback extends ElementCallback {
        private TablesCallback() {
            super(TABLES, 0, 1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(new TableCallback());
        }
    }

    private class TableCallback extends ElementCallback {
        private TableCallback() {
            super(TABLE, 0, -1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            currentTable = new Table(currentSchema);
            checkAndSetAttributes(currentTable, attributes);
            currentSchema.getTables().add(currentTable);

            expectElement(new ColumnsCallback());
            expectElement(new PrimaryKeyCallback());
            expectElement(new ForeignKeysCallback());

            currentModelAnnotations = currentTable.getModelAnnotations();
            expectElement(new AnnotationsCallback());
        }
    }

    private class ColumnsCallback extends ElementCallback {
        private ColumnsCallback() {
            super(COLUMNS, 1, 1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(new ColumnCallback());
        }
    }

    private class ColumnCallback extends ElementCallback {
        private ColumnCallback() {
            super(COLUMN, 1, -1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            currentColumn = new Column(currentTable);
            checkAndSetAttributes(currentColumn, attributes);
            currentTable.getColumns().add(currentColumn);

            currentModelAnnotations = currentColumn.getModelAnnotations();
            expectElement(new AnnotationsCallback());
        }
    }

    private class PrimaryKeyCallback extends ElementCallback {
        private PrimaryKeyCallback() {
            super(PRIMARYKEY, 0, 1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            currentPk = new PrimaryKey(currentTable);
            checkAndSetAttributes(currentPk, attributes);
            currentTable.setPrimaryKey(currentPk);

            expectElement(new PrimaryKeyColumnCallback());
        }
    }

    private class PrimaryKeyColumnCallback extends ElementCallback {
        private PrimaryKeyColumnCallback() {
            super(PRIMARYKEYCOLUMN, 1, -1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            PrimaryKeyColumn pkColumn = new PrimaryKeyColumn(currentPk);
            checkAndSetAttributes(pkColumn, attributes);
            currentPk.getPrimaryKeyColumns().add(pkColumn);
        }
    }

    private class ForeignKeysCallback extends ElementCallback {
        private ForeignKeysCallback() {
            super(FOREIGNKEYS, 0, 1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(new ForeignKeyCallback());
        }
    }

    private class ForeignKeyCallback extends ElementCallback {
        private ForeignKeyCallback() {
            super(FOREIGNKEY, 1, -1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            currentFk = new ForeignKey(currentTable);
            checkAndSetAttributes(currentFk, attributes);
            currentTable.getForeignKeys().add(currentFk);

            expectElement(new ReferencesCallback());

            currentModelAnnotations = currentFk.getModelAnnotations();
            expectElement(new AnnotationsCallback());
        }
    }

    private class ReferencesCallback extends ElementCallback {
        private ReferencesCallback() {
            super(REFERENCES, 1, 1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(new ReferenceCallback());
        }
    }

    private class ReferenceCallback extends ElementCallback {
        private ReferenceCallback() {
            super(REFERENCE, 1, -1);
        }

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

    private class RootNodeCallback extends ElementCallback {
        private RootNodeCallback() {
            super(ROOTNODE, 0, 1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            RootNode root = new RootNode();
            model.setRoot(root);
            checkAndSetAttributes(root, attributes);
            expectElement(new ChildNodesCallback(root));
            expectElement(new PermissionsCallback(root));
        }
    }

    private class ChildNodesCallback extends ElementCallback {
        private final SiteNode parent;

        private ChildNodesCallback(SiteNode parent) {
            super(CHILDNODES, 0, 1);
            this.parent = parent;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            String[] expectedTags = {DOCUMENTNODE, FOLDERNODE, CUSTOMNODE,
                    CUSTOMFOLDERNODE, USECASENODE, PORTLETNODE};
            ElementCallback[] callbackArray = {
                    new DocumentNodeCallback(parent), new FolderCallback(parent),
                    new CustomNodeCallback(parent), new CustomFolderCallback(parent),
                    new UseCaseNodeCallback(parent), new PortletCallback(parent)};
            expectElement(expectedTags, 0, null,
                    callbackArray);
        }
    }

    private class FolderCallback extends ElementCallback {
        SiteNode parentNode;

        private FolderCallback(SiteNode parent) {
            super(FOLDERNODE, 0, -1);
            this.parentNode = parent;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            FolderNode node = new FolderNode(parentNode);
            checkAndSetAttributes(node, attributes);
            parentNode.getChildNodes().add(node);
            expectElement(new ChildNodesCallback(node));
            expectElement(new PermissionsCallback(node));
        }
    }

    private class DocumentNodeCallback extends ElementCallback {
        SiteNode parentNode;

        private DocumentNodeCallback(SiteNode parent) {
            super(DOCUMENTNODE, 0, -1);
            this.parentNode = parent;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            DocumentNode node = new DocumentNode(parentNode);
            checkAndSetAttributes(node, attributes);
            parentNode.getChildNodes().add(node);
            expectElement(new PermissionsCallback(node));
        }
    }

    private class CustomNodeCallback extends ElementCallback {
        SiteNode parentNode;

        private CustomNodeCallback(SiteNode parent) {
            super(CUSTOMNODE, 0, -1);
            this.parentNode = parent;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            CustomNode node = new CustomNode(parentNode);
            checkAndSetAttributes(node, attributes);
            parentNode.getChildNodes().add(node);
            expectElement(new PermissionsCallback(node));
        }
    }

    private class CustomFolderCallback extends ElementCallback {
        SiteNode parentNode;

        private CustomFolderCallback(SiteNode parent) {
            super(CUSTOMFOLDERNODE, 0, -1);
            this.parentNode = parent;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            CustomFolderNode node = new CustomFolderNode(parentNode);
            checkAndSetAttributes(node, attributes);
            parentNode.getChildNodes().add(node);
            expectElement(new PermissionsCallback(node));
        }
    }

    private class PortletCallback extends ElementCallback {
        SiteNode parentNode;

        private PortletCallback(SiteNode parent) {
            super(PORTLETNODE, 0, -1);
            this.parentNode = parent;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            PortletNode node = new PortletNode(parentNode);
            checkAndSetAttributes(node, attributes);
            parentNode.getChildNodes().add(node);
            expectElement(new PermissionsCallback(node));
        }
    }

    private class UseCaseNodeCallback extends ElementCallback {
        SiteNode parentNode;

        private UseCaseNodeCallback(SiteNode parent) {
            super(USECASENODE, 0, -1);
            this.parentNode = parent;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            UseCaseNode node = new UseCaseNode(parentNode);
            checkAndSetAttributes(node, attributes);
            parentNode.getChildNodes().add(node);
            expectElement(new UseCaseCallback(node));
            expectElement(new PermissionsCallback(node));
        }
    }






    //**************************************************************************
    // Use cases
    //**************************************************************************

    private class UseCaseCallback extends ElementCallback {
        UseCaseNode node;
        private UseCaseCallback(UseCaseNode node){
            super(USECASE, 1, -1);
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

            expectElement(new PropertiesCallback());

            expectElement(new SelectionProvidersCallback());

            currentModelAnnotations = currentUseCase.getModelAnnotations();
            expectElement(new AnnotationsCallback());

            expectElement(new ButtonsCallback());

            expectElement(new SubUseCasesCallback());

            UseCase poppedUsedCase = useCaseStack.pop();
            assert(poppedUsedCase == currentUseCase);
        }
    }

    private class PropertiesCallback extends ElementCallback {
        private PropertiesCallback() {
            super(PROPERTIES, 0, 1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(new PropertyCallback());
        }
    }

    private class PropertyCallback extends ElementCallback {
        private PropertyCallback() {
            super(PROPERTY, 0, -1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            UseCase currentUseCase = useCaseStack.peek();

            UseCaseProperty useCaseProperty =
                    new UseCaseProperty(currentUseCase);
            checkAndSetAttributes(useCaseProperty, attributes);
            currentUseCase.getProperties().add(useCaseProperty);

            currentModelAnnotations = useCaseProperty.getAnnotations();
            expectElement(new AnnotationsCallback());
        }
    }

    private class ButtonsCallback extends ElementCallback {
        private ButtonsCallback() {
            super(BUTTONS, 0, 1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(new ButtonCallback());
        }
    }

    private class ButtonCallback extends ElementCallback {
        private ButtonCallback() {
            super(BUTTON, 0, -1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            UseCase currentUseCase = useCaseStack.peek();
            Button button = new Button(currentUseCase);
            checkAndSetAttributes(button, attributes);
            currentUseCase.getButtons().add(button);
        }
    }

    private class AnnotationsCallback extends ElementCallback {
        private AnnotationsCallback() {
            super(ANNOTATIONS, 0, 1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {

            expectElement(new AnnotationCallback());
        }
    }

    private class AnnotationCallback extends ElementCallback {
        private AnnotationCallback() {
            super(ANNOTATION, 0, -1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            currentModelAnnotation = new ModelAnnotation();
            checkAndSetAttributes(currentModelAnnotation, attributes);
            currentModelAnnotations.add(currentModelAnnotation);
            expectElement(new AnnotationValueCallback());
        }
    }

    private class AnnotationValueCallback extends ElementCallback {
        private AnnotationValueCallback() {
            super(VALUE, 0, -1);
        }

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

    private class SubUseCasesCallback extends ElementCallback {
        private SubUseCasesCallback() {
            super(SUBUSECASES, 0, 1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(new UseCaseCallback(null));
        }
    }

    //**************************************************************************
    // Permissions
    //**************************************************************************
    private class PermissionsCallback extends ElementCallback {
        SiteNode node;

        private PermissionsCallback(SiteNode node) {
            super(PERMISSIONS, 0, 1);
            this.node = node;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(new AllowCallback(node));
            expectElement(new DenyCallback(node));
        }
    }
    private class AllowCallback extends ElementCallback {
        SiteNode node;

        private AllowCallback(SiteNode node) {
            super(ALLOW, 0, 1);
            this.node = node;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(new GroupCallback(node.getAllowGroups()));
        }
    }

    private class DenyCallback extends ElementCallback {
        SiteNode node;

        private DenyCallback(SiteNode node) {
            super(DENY, 0, 1);
            this.node = node;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(new GroupCallback(node.getDenyGroups()));
        }
    }

    private class GroupCallback extends ElementCallback {
        List<String> groups;

        private GroupCallback(List<String> groups) {
            super(GROUP, 1, -1);
            this.groups = groups;
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            groups.add(attributes.get(NAME));

        }
    }

    private class SelectionProvidersCallback extends ElementCallback {
        private SelectionProvidersCallback() {
            super(SELECTIONPROVIDERS, 0, 1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(new SelectionProviderCallback());
        }
    }

    private class SelectionProviderCallback extends ElementCallback {
        private SelectionProviderCallback() {
            super(SELECTIONPROVIDER, 0, -1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            currentModelSelectionProvider = new ModelSelectionProvider();
            checkAndSetAttributes(currentModelSelectionProvider, attributes);
            UseCase currentUseCase = useCaseStack.peek();
            currentUseCase.getModelSelectionProviders()
                    .add(currentModelSelectionProvider);
            expectElement(new SelectionPropertiesCallback());
        }
    }

    private class SelectionPropertiesCallback extends ElementCallback {
        private SelectionPropertiesCallback() {
            super(SELECTIONPROPERTIES, 0, -1);
        }

        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(new SelectionPropertyCallback());
        }
    }

    private class SelectionPropertyCallback extends ElementCallback {
        private SelectionPropertyCallback() {
            super(SELECTIONPROPERTY, 0, -1);
        }

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

