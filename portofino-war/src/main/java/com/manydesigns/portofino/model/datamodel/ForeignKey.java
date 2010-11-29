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

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.annotations.Annotation;
import com.manydesigns.portofino.util.Pair;
import com.manydesigns.portofino.xml.XmlAttribute;
import com.manydesigns.portofino.xml.XmlCollection;
import org.apache.commons.lang.ObjectUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ForeignKey implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String RULE_NO_ACTION = "NO ACTION";
    public static final String RULE_CASCADE = "CASCADE";
    public static final String RULE_SET_NULL = "SET NULL";
    public static final String RULE_SET_DEFAULT = "SET DEFAULT";
    

    //**************************************************************************
    // Fields (physical JDBC)
    //**************************************************************************

    protected final Table fromTable;
    protected final List<Reference> references;
    protected final List<Annotation> annotations;

    protected String foreignKeyName;

    protected String toDatabase;
    protected String toSchema;
    protected String toTable;

    protected String onUpdate;
    protected String onDelete;

    protected boolean virtual;



    //**************************************************************************
    // Fields (logical)
    //**************************************************************************

    protected String manyPropertyName;
    protected String onePropertyName;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected Table actualToTable;
    protected String actualManyPropertyName;
    protected String actualOnePropertyName;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LogUtil.getLogger(ForeignKey.class);

    //**************************************************************************
    // Constructors and init
    //**************************************************************************

    public ForeignKey(Table fromTable) {
        this.fromTable = fromTable;
        references = new ArrayList<Reference>();
        annotations = new ArrayList<Annotation>();
    }

    public ForeignKey(Table fromTable, String foreignKeyName,
                      String toDatabase, String toSchema,
                      String toTable, String onUpdate, String onDelete) {
        this(fromTable);
        this.foreignKeyName = foreignKeyName;
        this.toDatabase = toDatabase;
        this.toSchema = toSchema;
        this.toTable = toTable;
        this.onUpdate = onUpdate;
        this.onDelete = onDelete;        
    }

    public ForeignKey(Table fromTable, String foreignKeyName,
                      String toDatabase, String toSchema,
                      String toTable, String onUpdate, String onDelete,
                      String manyPropertyName, String onePropertyName, boolean virtual) {
        this(fromTable, foreignKeyName, toDatabase,
                toSchema, toTable, onUpdate, onDelete);
        this.manyPropertyName = manyPropertyName;
        this.onePropertyName = onePropertyName;
        this.virtual = virtual;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public String getQualifiedName() {
        return MessageFormat.format("{0}${1}",
                fromTable.getQualifiedName(), foreignKeyName);
    }

    public void reset() {
        actualToTable = null;
        actualManyPropertyName = null;
        actualOnePropertyName = null;

        for (Reference reference : references) {
            reference.reset();
        }
    }

    public void init(Model model) {
        // wire up ForeignKey.toTable
        String qualifiedToTableName =
                Table.composeQualifiedName(toDatabase, toSchema, toTable);
        actualToTable = model.findTableByQualifiedName(qualifiedToTableName);
        if (actualToTable == null) {
            LogUtil.warningMF(logger,
                    "Cannor wire ''{0}'' to table ''{1}''",
                    this, qualifiedToTableName);
        } else {
            // wire up Table.oneToManyRelationships
            actualToTable.getOneToManyRelationships().add(this);
        }

        if (references.isEmpty()) {
            throw new Error(MessageFormat.format(
                    "Foreign key {0} has no referneces",
                    getQualifiedName()));
        }

        for (Reference reference : references) {
            reference.init(model);
        }

        actualManyPropertyName = (manyPropertyName == null)
                ? foreignKeyName
                : manyPropertyName;

        actualOnePropertyName = (onePropertyName == null)
                ? foreignKeyName
                : onePropertyName;
    }


    //**************************************************************************
    // Find methods
    //**************************************************************************

    public Reference findReferenceByColumnNamePair(Pair<String> columnNamePair) {
        for (Reference reference : references) {
            if (ObjectUtils.equals(reference.getFromColumn(), columnNamePair.left)
                    && ObjectUtils.equals(reference.getToColumn(), columnNamePair.right)) {
                return reference;
            }
        }
        return null;
    }

    public Annotation findModelAnnotationByType(String annotationType) {
        for (Annotation annotation : annotations) {
            if (annotation.getType().equals(annotationType)) {
                return annotation;
            }
        }
        LogUtil.fineMF(logger,
                "Model annotation not found: {0}", annotationType);
        return null;
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public Table getFromTable() {
        return fromTable;
    }

    public String getFromDatabaseName() {
        return fromTable.getDatabaseName();
    }

    public String getFromSchemaName() {
        return fromTable.getSchemaName();
    }

    public String getFromTableName() {
        return fromTable.getTableName();
    }

    @XmlAttribute(required = true, order = 1)
    public String getForeignKeyName() {
        return foreignKeyName;
    }

    public void setForeignKeyName(String foreignKeyName) {
        this.foreignKeyName = foreignKeyName;
    }

    @XmlAttribute(required = true, order = 2)
    public String getToDatabase() {
        return toDatabase;
    }

    public void setToDatabase(String toDatabase) {
        this.toDatabase = toDatabase;
    }

    @XmlAttribute(required = true, order = 3)
    public String getToSchema() {
        return toSchema;
    }

    public void setToSchema(String toSchema) {
        this.toSchema = toSchema;
    }

    @XmlAttribute(required = true, order = 4)
    public String getToTable() {
        return toTable;
    }

    public void setToTable(String toTable) {
        this.toTable = toTable;
    }

    @XmlAttribute(required = true, order = 5)
    public String getOnUpdate() {
        return onUpdate;
    }

    public void setOnUpdate(String onUpdate) {
        this.onUpdate = onUpdate;
    }

    @XmlAttribute(required = true, order = 6)
    public String getOnDelete() {
        return onDelete;
    }

    public void setOnDelete(String onDelete) {
        this.onDelete = onDelete;
    }

    @XmlCollection(itemMin = 1, itemClasses = Reference.class, itemNames = "reference")
    public List<Reference> getReferences() {
        return references;
    }

    @XmlAttribute(required = false, order = 7)
    public String getManyPropertyName() {
        return manyPropertyName;
    }

    public void setManyPropertyName(String manyPropertyName) {
        this.manyPropertyName = manyPropertyName;
    }

    @XmlAttribute(required = false, order = 8)
    public String getOnePropertyName() {
        return onePropertyName;
    }

    @XmlAttribute(required = true, order = 9)
    public boolean isVirtual() {
        return virtual;
    }

    public void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }

    public void setOnePropertyName(String onePropertyName) {
        this.onePropertyName = onePropertyName;
    }

    @XmlCollection(itemClasses = Annotation.class, itemNames = "annotation")
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public Table getActualToTable() {
        return actualToTable;
    }

    public void setActualToTable(Table actualToTable) {
        this.actualToTable = actualToTable;
    }

    public String getActualManyPropertyName() {
        return actualManyPropertyName;
    }

    public void setActualManyPropertyName(String actualManyPropertyName) {
        this.actualManyPropertyName = actualManyPropertyName;
    }

    public String getActualOnePropertyName() {
        return actualOnePropertyName;
    }

    public void setActualOnePropertyName(String actualOnePropertyName) {
        this.actualOnePropertyName = actualOnePropertyName;
    }

    //**************************************************************************
    // toString() override
    //**************************************************************************

    @Override
    public String toString() {
        return MessageFormat.format("foreign key {0}", getQualifiedName());
    }

}
