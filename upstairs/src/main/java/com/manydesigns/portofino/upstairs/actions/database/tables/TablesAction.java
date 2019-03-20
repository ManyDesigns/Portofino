package com.manydesigns.portofino.upstairs.actions.database.tables;

import com.manydesigns.elements.MapKeyValueAccessor;
import com.manydesigns.elements.annotations.*;
import com.manydesigns.elements.annotations.impl.SelectImpl;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.RequestMessages;
import com.manydesigns.elements.options.DisplayMode;
import com.manydesigns.elements.options.SearchDisplayMode;
import com.manydesigns.elements.reflection.MutableClassAccessor;
import com.manydesigns.elements.reflection.MutablePropertyAccessor;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.upstairs.actions.database.tables.support.ColumnAndAnnotations;
import org.apache.commons.lang.ArrayUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.jetbrains.annotations.NotNull;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Alessio Stalla - alessiostalla@gmail.com
 */
@RequiresAuthentication
@RequiresAdministrator
public class TablesAction extends AbstractPageAction {

    private static final Logger logger = LoggerFactory.getLogger(TablesAction.class);
    public static final String DATE_FORMAT = DateFormat.class.getName();
    public static final String DECIMAL_FORMAT = DecimalFormat.class.getName();
    public static final String FIELD_SIZE = FieldSize.class.getName();
    public static final String FILE_BLOB = FileBlob.class.getName();
    public static final String HIGHLIGHT_LINKS = HighlightLinks.class.getName();
    public static final String MIN_INT_VALUE = MinIntValue.class.getName();
    public static final String MIN_DECIMAL_VALUE = MinDecimalValue.class.getName();
    public static final String MAX_INT_VALUE = MaxIntValue.class.getName();
    public static final String MAX_DECIMAL_VALUE = MaxDecimalValue.class.getName();
    public static final String MULTILINE = Multiline.class.getName();
    public static final String REGEXP = RegExp.class.getName();
    public static final String RICH_TEXT = RichText.class.getName();
    public static final String EMAIL = Email.class.getName();
    public static final String PASSWORD = Password.class.getName();
    public static final String CAP = CAP.class.getName();
    public static final String PARTITA_IVA = PartitaIva.class.getName();
    public static final String CODICE_FISCALE = CodiceFiscale.class.getName();
    public static final String PHONE = Phone.class.getName();
    public static final String ENCRYPTED = Encrypted.class.getName();
    public static final Map<String, String> STRING_FORMAT = new HashMap<>();
    static {
        STRING_FORMAT.put(EMAIL, "Email");
        STRING_FORMAT.put(PASSWORD, "Password");
        STRING_FORMAT.put(CAP, "CAP/ZIP");
        STRING_FORMAT.put(PARTITA_IVA, "Partita IVA");
        STRING_FORMAT.put(CODICE_FISCALE, "Codice Fiscale");
        STRING_FORMAT.put(PHONE, "Phone");
        STRING_FORMAT.put(ENCRYPTED, "Encrypted");
    }

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
            Map info = new HashMap();
            info.put("type", type);

            //Default
            Class defaultJavaType = Type.getDefaultJavaType(column.getJdbcType(), column.getColumnType(), precision, column.getScale());
            if(defaultJavaType == null) {
                defaultJavaType = Object.class;
            }
            info.put("default", describeType(defaultJavaType));
            typeInfo.add(info);

            //Available
            List availableTypes = new ArrayList();
            info.put("types", availableTypes);
            //Existing
            if(column.getJavaType() != null) {
                try {
                    Class existingType = Class.forName(column.getJavaType());
                    if (!ArrayUtils.contains(javaTypes, existingType)) {
                        availableTypes.add(describeType(existingType));
                    }
                } catch (Exception e) {
                    logger.error("Invalid Java type", e);
                }
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
        existing.setShortName(table.getShortName());
        for(Column column : table.getColumns()) {
            Column c2 = DatabaseLogic.findColumnByName(existing, column.getColumnName());
            BeanUtils.copyProperties(column, c2);
            c2.setTable(existing);
        }
        persistence.initModel();
        persistence.saveXmlModel();
    }

    @Path("{db}/{schema}/{table}/{column}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveColumn(
            @PathParam("db") String db, @PathParam("schema") String schema, @PathParam("table") String tableName,
            @PathParam("column") String columnName, ColumnAndAnnotations column) throws Exception {
        Column existing = DatabaseLogic.findColumnByName(persistence.getModel(), db, schema, tableName, columnName);
        if(existing == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        Table table = existing.getTable();
        Class type = getColumnType(existing, existing.getJavaType());
        Form annotationsForm = new FormBuilder(getApplicableAnnotations(type)).build();
        MapKeyValueAccessor annotationsAccessor = new MapKeyValueAccessor(column.getAnnotations());
        annotationsForm.readFrom(annotationsAccessor);
        if(annotationsForm.validate()) {
            BeanUtils.copyProperties(column.getColumn(), existing);
            existing.setTable(table);
            existing.getAnnotations().clear();
            Set<Map.Entry<String, ?>> set = column.getAnnotations().entrySet();
            set.forEach(e -> {
                if(e.getValue() != null) {
                    Annotation a;
                    String value = e.getValue().toString();
                    switch (e.getKey()) {
                        case "dateFormat":
                            try {
                                new SimpleDateFormat(value);
                            } catch (IllegalArgumentException ex) {
                                logger.error("Invalid date format: " + value, ex);
                                RequestMessages.addErrorMessage("Invalid date format: " + value); //TODO I18n
                                break;
                            }
                            a = new Annotation(DATE_FORMAT);
                            a.getValues().add(value);
                            existing.getAnnotations().add(a);
                            break;
                        case "decimalFormat":
                            try {
                                new java.text.DecimalFormat(value);
                            } catch (IllegalArgumentException ex) {
                                logger.error("Invalid decimal format: " + value, ex);
                                RequestMessages.addErrorMessage("Invalid decimal format: " + value); //TODO I18n
                                break;
                            }
                            a = new Annotation(DECIMAL_FORMAT);
                            a.getValues().add(value);
                            existing.getAnnotations().add(a);
                            break;
                        case "fieldSize":
                            a = new Annotation(FIELD_SIZE);
                            a.getValues().add(value);
                            existing.getAnnotations().add(a);
                            break;
                        case "fileBlob":
                            if(Boolean.TRUE.equals(e.getValue())) {
                                a = new Annotation(FILE_BLOB);
                                existing.getAnnotations().add(a);
                            }
                            break;
                        case "highlightLinks":
                            a = new Annotation(HIGHLIGHT_LINKS);
                            a.getValues().add(value);
                            existing.getAnnotations().add(a);
                            break;
                        case "minValue":
                            if(type == Integer.class || type == Long.class || type == BigInteger.class) {
                                a = new Annotation(MIN_INT_VALUE);
                            } else {
                                a = new Annotation(MIN_DECIMAL_VALUE);
                            }
                            a.getValues().add(value);
                            existing.getAnnotations().add(a);
                            break;
                        case "maxValue":
                            if(type == Integer.class || type == Long.class || type == BigInteger.class) {
                                a = new Annotation(MAX_INT_VALUE);
                            } else {
                                a = new Annotation(MAX_DECIMAL_VALUE);
                            }
                            a.getValues().add(value);
                            existing.getAnnotations().add(a);
                            break;
                        case "regexp":
                            a = new Annotation(REGEXP);
                            a.getValues().add(value);
                            a.getValues().add("elements.error.field.regexp.format"); //Default error message
                            existing.getAnnotations().add(a);
                            break;
                        case "stringFormat":
                            a = new Annotation(((Map)e.getValue()).get("v").toString());
                            existing.getAnnotations().add(a);
                            break;
                        case "typeOfContent":
                            a = new Annotation(((Map)e.getValue()).get("v").toString());
                            a.getValues().add("true");
                            existing.getAnnotations().add(a);
                            break;
                        default:
                            String msg = "Unsupported annotation: " + e.getKey();
                            logger.error(msg);
                            RequestMessages.addErrorMessage(msg); //TODO i18n
                    }
                }
            });
            persistence.initModel();
            persistence.saveXmlModel();
        } else {
            throw new WebApplicationException(Response.serverError().entity(annotationsForm).build());
        }
    }

    @Path("{db}/{schema}/{table}/{column}/annotations/{typeName}")
    @GET
    public String getAnnotations(
            @PathParam("db") String db, @PathParam("schema") String schema, @PathParam("table") String tableName,
            @PathParam("column") String columnName, @PathParam("typeName") String typeName) throws ClassNotFoundException {
        Column column = DatabaseLogic.findColumnByName(persistence.getModel(), db, schema, tableName, columnName);
        if(column == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        Class type = getColumnType(column, typeName);
        MutableClassAccessor classAccessor = getApplicableAnnotations(type);
        JSONStringer jsonStringer = new JSONStringer();
        jsonStringer.object();
        jsonStringer.key("classAccessor");
        ReflectionUtil.classAccessorToJson(classAccessor, jsonStringer);
        jsonStringer.key("annotations");
        jsonStringer.object();
        column.getAnnotations().forEach(a -> {
            String annType = a.getType();
            if(DATE_FORMAT.equals(annType)) {
                jsonStringer.key("dateFormat");
            } else if(DECIMAL_FORMAT.equals(annType)) {
                jsonStringer.key("decimalFormat");
            } else if(FIELD_SIZE.equals(annType)) {
                jsonStringer.key("fieldSize");
            } else if(FILE_BLOB.equals(annType)) {
                jsonStringer.key("fileBlob");
            } else if(HIGHLIGHT_LINKS.equals(annType)) {
                jsonStringer.key("highlightLinks");
            } else if(MIN_INT_VALUE.equals(annType)) {
                jsonStringer.key("minValue");
            } else if(MIN_DECIMAL_VALUE.equals(annType)) {
                jsonStringer.key("minValue");
            } else if(MAX_INT_VALUE.equals(annType)) {
                jsonStringer.key("maxValue");
            } else if(MAX_DECIMAL_VALUE.equals(annType)) {
                jsonStringer.key("maxValue");
            } else if(MULTILINE.equals(annType)) {
                jsonStringer.key("typeOfContent");
                jsonStringer.object();
                jsonStringer.key("v").value(annType);
                jsonStringer.key("l").value("Multiline");
                jsonStringer.key("s").value(true);
                jsonStringer.endObject();
                return;
            } else if(REGEXP.equals(annType)) {
                jsonStringer.key("regexp");
            } else if(RICH_TEXT.equals(annType)) {
                jsonStringer.key("typeOfContent");
                jsonStringer.object();
                jsonStringer.key("v").value(annType);
                jsonStringer.key("l").value("Rich text");
                jsonStringer.key("s").value(true);
                jsonStringer.endObject();
                return;
            } else if(STRING_FORMAT.keySet().contains(annType)) {
                jsonStringer.key("stringFormat");
                jsonStringer.object();
                jsonStringer.key("v").value(annType);
                jsonStringer.key("l").value(STRING_FORMAT.get(annType));
                jsonStringer.key("s").value(true);
                jsonStringer.endObject();
                return;
            } else {
                String msg = "Unsupported annotation: " + annType;
                logger.error(msg);
                RequestMessages.addErrorMessage(msg); //TODO i18n
                return;
            }
            if(!a.getValues().isEmpty()) {
                jsonStringer.value(a.getValues().get(0));
            }

        });
        jsonStringer.endObject();
        jsonStringer.endObject();
        return jsonStringer.toString();
    }

    @NotNull
    public MutableClassAccessor getApplicableAnnotations(Class type) {
        MutableClassAccessor classAccessor = new MutableClassAccessor();
        if(type == null) {
            return classAccessor;
        }
        if(Number.class.isAssignableFrom(type)) {
            classAccessor.addProperty(new MutablePropertyAccessor("fieldSize", Integer.class));
            classAccessor.addProperty(new MutablePropertyAccessor("minValue", BigDecimal.class));
            classAccessor.addProperty(new MutablePropertyAccessor("maxValue", BigDecimal.class));
            classAccessor.addProperty(new MutablePropertyAccessor("decimalFormat", String.class));
        } else if(String.class.equals(type)) {
            classAccessor.addProperty(new MutablePropertyAccessor("fieldSize", Integer.class));
            classAccessor.addProperty(new MutablePropertyAccessor("regexp", String.class));
            classAccessor.addProperty(new MutablePropertyAccessor("highlightLinks", Boolean.class));
            classAccessor.addProperty(new MutablePropertyAccessor("fileBlob", Boolean.class));
            SelectImpl select = new SelectImpl(
                    DisplayMode.DROPDOWN, SearchDisplayMode.DROPDOWN,
                    new String[] {
                            EMAIL, PASSWORD, CAP, PARTITA_IVA, CODICE_FISCALE, PHONE, ENCRYPTED
                    }, new String[] {
                            "Email", "Password", "CAP/ZIP", "Partita IVA", "Codice Fiscale", "Phone", "Encrypted"
                    }, true);
            classAccessor.addProperty(new MutablePropertyAccessor("stringFormat", String.class).configureAnnotation(select));
            select = new SelectImpl(
                    DisplayMode.DROPDOWN, SearchDisplayMode.DROPDOWN,
                    new String[] {MULTILINE, RICH_TEXT},
                    new String[] { "Multiline", "Rich text" },
                    true);
            classAccessor.addProperty(new MutablePropertyAccessor("typeOfContent", String.class).configureAnnotation(select));
        } else if(Date.class.isAssignableFrom(type)) {
            classAccessor.addProperty(new MutablePropertyAccessor("fieldSize", Integer.class));
            classAccessor.addProperty(new MutablePropertyAccessor("dateFormat", String.class));

        } else if(byte[].class.isAssignableFrom(type)) {
            classAccessor.addProperty(new MutablePropertyAccessor("databaseBlobContentTypeProperty", String.class));
            classAccessor.addProperty(new MutablePropertyAccessor("databaseBlobFileNameProperty", String.class));
            classAccessor.addProperty(new MutablePropertyAccessor("databaseBlobTimestampProperty", String.class));
        }
        return classAccessor;
    }

    public Class getColumnType(Column column, String typeName) throws ClassNotFoundException {
        Class type;
        if("default".equals(typeName) || typeName == null) {
            type = Type.getDefaultJavaType(column.getJdbcType(), column.getColumnType(), column.getLength(), column.getScale());
        } else {
            type = Class.forName(typeName);
        }
        return type;
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
