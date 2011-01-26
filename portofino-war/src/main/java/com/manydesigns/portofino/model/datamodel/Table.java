/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */

package com.manydesigns.portofino.model.datamodel;

import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.annotations.Annotation;
import com.manydesigns.portofino.xml.XmlAttribute;
import com.manydesigns.portofino.xml.XmlCollection;
import com.manydesigns.portofino.xml.XmlElement;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Table implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Schema schema;
    protected final List<Column> columns;
    protected final List<ForeignKey> foreignKeys;
    protected final List<Annotation> annotations;

    protected String tableName;

    protected String entityName;
    protected String actualEntityName;

    protected Boolean manyToMany;
    protected String javaClass;

    protected PrimaryKey primaryKey;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected final List<ForeignKey> oneToManyRelationships;
    protected Class actualJavaClass;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(Table.class);

    //**************************************************************************
    // Constructors and init
    //**************************************************************************
    public Table(Schema schema) {
        this.schema = schema;
        columns = new ArrayList<Column>();
        foreignKeys = new ArrayList<ForeignKey>();
        oneToManyRelationships = new ArrayList<ForeignKey>();
        annotations = new ArrayList<Annotation>();
    }

    public Table(Schema schema, String tableName) {
        this(schema);
        this.tableName = tableName;
    }

    public Table(Schema schema, String tableName,
                 Boolean manyToMany, String javaClass) {
        this(schema, tableName);
        this.manyToMany = manyToMany;
        this.javaClass = javaClass;
    }

    //**************************************************************************
    // DatamodelObject implementation
    //**************************************************************************

    public String getQualifiedName() {
        return MessageFormat.format("{0}.{1}",
                schema.getQualifiedName(), tableName);
    }


    public void reset() {
        actualJavaClass = null;
        oneToManyRelationships.clear();

        for (Column column : columns) {
            column.reset();
        }

        if (primaryKey != null) {
            primaryKey.reset();
        }

        for (ForeignKey foreignKey : foreignKeys) {
            foreignKey.reset();
        }

        for (Annotation annotation : annotations) {
            annotation.reset();
        }
    }

    public void init(Model model) {
        // wire up javaClass
        actualJavaClass = ReflectionUtil.loadClass(javaClass);

        if (entityName == null) {
            actualEntityName = defineEntityName(this.getQualifiedName());
        } else {
            actualEntityName = defineEntityName(entityName);
        }

        for (Column column : columns) {
            column.init(model);
        }

        if (primaryKey == null) {
            logger.warn("Table {} has no primary key", toString());
        } else {
            primaryKey.init(model);
        }

        for (ForeignKey foreignKey : foreignKeys) {
            foreignKey.init(model);
        }


        for (Annotation annotation : annotations) {
            annotation.init(model);
        }
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public Schema getSchema() {
        return schema;
    }

    @Required
    public String getDatabaseName() {
        return schema.getDatabaseName();
    }

    @Required
    public String getSchemaName() {
        return schema.getSchemaName();
    }

    @Required
    @XmlAttribute(required = true, order = 1, identifier = true)
    public String getTableName() {
        return tableName;
    }


    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @XmlAttribute(required = false, order = 2)
    public Boolean getManyToMany() {
        return manyToMany;
    }

    public void setManyToMany(Boolean manyToMany) {
        this.manyToMany = manyToMany;
    }

    @XmlAttribute(required = false, order = 3)
    public String getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(String javaClass) {
        this.javaClass = javaClass;
    }

    @XmlCollection(itemClasses = Column.class, itemNames = "column", order = 1)
    public List<Column> getColumns() {
        return columns;
    }

    @XmlElement(order = 2)
    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Class getActualJavaClass() {
        return actualJavaClass;
    }

    @XmlCollection(itemClasses = ForeignKey.class, itemNames = "foreignKey", order = 3)
    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    @XmlAttribute(required = false, order = 4)
    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getActualEntityName() {
        return actualEntityName;
    }

    public void setActualEntityName(String actualEntityName) {
        this.actualEntityName = actualEntityName;
    }

    public List<ForeignKey> getOneToManyRelationships() {
        return oneToManyRelationships;
    }

    @XmlCollection(itemClasses = Annotation.class, itemNames = "annotation", order = 4)
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    //**************************************************************************
    // Search objects of a certain kind
    //**************************************************************************

    public Column findColumnByName(String columnName) {
        for (Column column : columns) {
            if (column.getColumnName().equals(columnName)) {
                return column;
            }
        }
        logger.debug("Column not found: {}", columnName);
        return null;
    }

    public ForeignKey findForeignKeyByName(String fkName) {
        for (ForeignKey current : foreignKeys) {
            if (current.getForeignKeyName().equals(fkName)) {
                return current;
            }
        }
        logger.debug("Foreign key not found: {}", fkName);
        return null;
    }

    public ForeignKey findOneToManyRelationshipByName(String relationshipName) {
        for (ForeignKey current : getOneToManyRelationships()) {
            if (current.getForeignKeyName().equals(relationshipName)) {
                return current;
            }
        }
        logger.debug("One to many relationship not found: {}", relationshipName);
        return null;
    }

    public Annotation findModelAnnotationByType(String annotationType) {
        for (Annotation annotation : annotations) {
            if (annotation.getType().equals(annotationType)) {
                return annotation;
            }
        }
        logger.debug("Model annotation not found: {}", annotationType);
        return null;
    }


    //**************************************************************************
    // toString() override
    //**************************************************************************

    @Override
    public String toString() {
        return MessageFormat.format("table {0}", getQualifiedName());
    }

    //**************************************************************************
    // Utility methods
    //**************************************************************************

    public static String composeQualifiedName(String databaseName,
                                              String schemaName,
                                              String tableName) {
        return MessageFormat.format(
                "{0}.{1}.{2}", databaseName, schemaName, tableName);
    }


    //**************************************************************************
    // Rende un nome secondo le regole che specificano gli identificatori in HQL
    // protected
    //ID_START_LETTER
    //    :    '_'
    //    |    '$'
    //|    'a'..'z'
    //|    '\u0080'..'\ufffe'       // HHH-558 : Allow unicode chars in identifiers
    //;
    //
    //protected
    //ID_LETTER
    //:    ID_START_LETTER
    //|    '0'..'9'
    //;
    //**************************************************************************
    public static String defineEntityName (String name) {
        name = StringUtils.replaceChars(name, ".", "_");
        String firstLetter = name.substring(0,1);
        String others = name.substring(1);

        StringBuffer result = new StringBuffer();
        result.append(checkFirstLetter(firstLetter));

        for(int i=0; i< others.length();i++){
            String letter = String.valueOf(others.charAt(i));
            result.append(checkOtherLetters(letter));
        }
        return result.toString();
    }

    private static String checkFirstLetter(String letter) {
        letter = StringUtils.replaceChars(letter, "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                                                  "abcdefghijklmnopqrstuvwxyz");

        if (letter.equals("_") || letter.equals("$")
                || StringUtils.isAlpha(letter)){
            return letter;
        } else if (StringUtils.isNumeric(letter)) {
            return "_"+letter;
        } else {
            return "_";
        }
    }

    private  static String checkOtherLetters(String letter) {
        letter = StringUtils.replaceChars(letter, "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                                                  "abcdefghijklmnopqrstuvwxyz");
        if (letter.equals("_") || letter.equals("$")
                || StringUtils.isAlphanumeric(letter)){
            return letter;
        } else {
            return "_";
        }
    }
}
