/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.model.database;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.util.Pair;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(value = XmlAccessType.NONE)
@XmlType(propOrder = {"toTableName", "toSchema", "onUpdate", "onDelete"})
public class ForeignKey extends DatabaseSelectionProvider
        implements HasReferences {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields (physical JDBC)
    //**************************************************************************

    protected String onUpdate;
    protected String onDelete;
    protected String toSchema;

    //**************************************************************************
    // Fields (logical)
    //**************************************************************************

    protected String manyPropertyName;
    protected String onePropertyName;
    protected String toTableName;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected String actualManyPropertyName;
    protected String actualOnePropertyName;
    protected Table toTable;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(ForeignKey.class);

    //**************************************************************************
    // Constructors and init
    //**************************************************************************

    public ForeignKey() {
    }

    public ForeignKey(Table fromTable) {
        this();
        this.fromTable = fromTable;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    @Override
    public String getQualifiedName() {
        return MessageFormat.format("{0}${1}",
                fromTable.getQualifiedName(), name);
    }

    @Override
    public void reset() {
        super.reset();
        toTable = null;
        actualManyPropertyName = null;
        actualOnePropertyName = null;
    }

    @Override
    public void init(Model model) {
        super.init(model);

        assert fromTable != null;
        assert name != null;
        assert toSchema != null;
        assert toTableName != null;

        if (references.isEmpty()) {
            throw new Error(MessageFormat.format(
                    "Foreign key {0} has no references",
                    getQualifiedName()));
        }

    }

    @Override
    public void link(Model model) {
        super.link(model);
        toTable = DatabaseLogic.findTableByName(model, toDatabase, toSchema, toTableName);
        if(toTable != null) {
            // wire up Table.oneToManyRelationships
            toTable.getOneToManyRelationships().add(this);
            hql = "from " + toTable.getActualEntityName();

            actualManyPropertyName = (manyPropertyName == null)
                ? DatabaseLogic.getUniquePropertyName(toTable, DatabaseLogic.normalizeName(name))
                : manyPropertyName;

        } else {
            logger.warn("Cannot find destination table '{}'",
                    Table.composeQualifiedName(toDatabase, toSchema, toTableName));

            actualManyPropertyName = (manyPropertyName == null)
                ? DatabaseLogic.normalizeName(name)
                : manyPropertyName;
        }

        actualOnePropertyName = (onePropertyName == null)
                ? DatabaseLogic.getUniquePropertyName(fromTable, DatabaseLogic.normalizeName(name))
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

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public String getFromDatabaseName() {
        return fromTable.getDatabaseName();
    }

    public String getFromSchemaName() {
        return fromTable.getSchemaName();
    }

    public String getFromTableName() {
        return fromTable.getTableName();
    }

    @XmlAttribute(required = true)
    public String getOnUpdate() {
        return onUpdate;
    }

    public void setOnUpdate(String onUpdate) {
        this.onUpdate = onUpdate;
    }

    @XmlAttribute(required = true)
    public String getOnDelete() {
        return onDelete;
    }

    public void setOnDelete(String onDelete) {
        this.onDelete = onDelete;
    }

    @XmlAttribute(required = false)
    public String getManyPropertyName() {
        return manyPropertyName;
    }

    public void setManyPropertyName(String manyPropertyName) {
        this.manyPropertyName = manyPropertyName;
    }

    @XmlAttribute(required = false)
    public String getOnePropertyName() {
        return onePropertyName;
    }

    public void setOnePropertyName(String onePropertyName) {
        this.onePropertyName = onePropertyName;
    }

    public String getActualManyPropertyName() {
        return actualManyPropertyName;
    }

    public String getActualOnePropertyName() {
        return actualOnePropertyName;
    }

    @Override
    @XmlTransient
    public String getHql() {
        return hql;
    }

    @XmlAttribute(required = true)
    public String getToSchema() {
        return toSchema;
    }

    public void setToSchema(String toSchema) {
        this.toSchema = toSchema;
    }

    public Table getToTable() {
        return toTable;
    }

    public void setToTable(Table toTable) {
        this.toTable = toTable;
    }

    @XmlAttribute(name = "toTable")
    public String getToTableName() {
        return toTableName;
    }

    public void setToTableName(String toTableName) {
        this.toTableName = toTableName;
    }

    //**************************************************************************
    // toString() override
    //**************************************************************************

    @Override
    public String toString() {
        return MessageFormat.format("foreign key {0}", getQualifiedName());
    }

}
