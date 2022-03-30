/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.database.model;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.Pair;
import com.manydesigns.portofino.model.annotations.KeyMappings;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang.ObjectUtils;
import org.eclipse.emf.ecore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.text.MessageFormat;
import java.util.List;

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
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Fields (physical JDBC)
    //**************************************************************************

    protected String onUpdate;
    protected String onDelete;
    protected String toSchema;

    //**************************************************************************
    // Fields (logical)
    //**************************************************************************

    /**
     * Name of the property containing many values (e.g. cities)
     */
    protected String manyPropertyName;
    /**
     * Name of the property referring to a single value (e.g. country)
     */
    protected String onePropertyName;
    protected String toTableName;
    protected String toEntityName;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected String actualManyPropertyName;
    protected String actualOnePropertyName;
    protected Table toTable;
    protected EReference relationship;

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
        setFromTable(fromTable);
    }

    public void setFromTable(Table fromTable) {
        this.fromTable = fromTable;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    @Override
    public String getQualifiedName() {
        return MessageFormat.format("{0}${1}", fromTable.getQualifiedName(), getName());
    }

    @Override
    public void reset() {
        super.reset();
        setToTable(null);
        actualManyPropertyName = null;
        actualOnePropertyName = null;
    }

    @Override
    public void init(Object context, Configuration configuration) {
        super.init(context, configuration);

        if (references.isEmpty()) {
            throw new Error(MessageFormat.format(
                    "Foreign key {0} has no references",
                    getQualifiedName()));
        }

    }

    @Override
    public void link(Object context, Configuration configuration) {
        super.link(context, configuration);
        List<Database> databases = (List) context;
        if(toEntityName != null) {
            setToTable(DatabaseLogic.findTableByEntityName(databases, getToDatabase(), toEntityName));
        } else {
            setToTable(DatabaseLogic.findTableByName(databases, getToDatabase(), toSchema, toTableName));
        }
        if(toTable != null) {
            // wire up Table.oneToManyRelationships
            toTable.getOneToManyRelationships().add(this);
            setHql("from " + toTable.getActualEntityName());

            actualManyPropertyName = (manyPropertyName == null)
                ? DatabaseLogic.getUniquePropertyName(toTable, DatabaseLogic.normalizeName(getName()))
                : manyPropertyName;

            if(relationship == null) {
                relationship = EcoreFactory.eINSTANCE.createEReference();
                EReference opposite = EcoreFactory.eINSTANCE.createEReference();
                opposite.setName(getName());
                opposite.setContainment(false);
                opposite.setEType(fromTable.getModelElement());
                opposite.setLowerBound(0);
                opposite.setUpperBound(EReference.UNBOUNDED_MULTIPLICITY);
                opposite.setDerived(true);
                toTable.getModelElement().getEStructuralFeatures().add(opposite);
                relationship.setEOpposite(opposite);
            }
            relationship.setContainment(false);
            relationship.setEType(toTable.getModelElement());
            relationship.setLowerBound(0);
            relationship.setUpperBound(1);
            relationship.setName(getName());
            EAnnotation mappings = relationship.getEAnnotation(KeyMappings.class.getName());
            if(mappings == null) {
                mappings = EcoreFactory.eINSTANCE.createEAnnotation();
                mappings.setSource(KeyMappings.class.getName());
            }
            mappings.getDetails().clear();
            final EAnnotation finalMappings = mappings;
            getReferences().forEach(r -> {
                r.link(databases, configuration);
                relationship.getEKeys().add(r.getActualToColumn().getModelElement());
                finalMappings.getDetails().put(
                        r.getActualFromColumn().getActualPropertyName(),
                        r.getActualToColumn().getActualPropertyName());
            });
            relationship.getEAnnotations().add(mappings);
            fromTable.getModelElement().getEStructuralFeatures().add(relationship);
        } else {
            logger.warn("Cannot find destination table '{}'",
                    Table.composeQualifiedName(getToDatabase(), toSchema, toTableName));

            actualManyPropertyName = (manyPropertyName == null)
                ? DatabaseLogic.normalizeName(getName())
                : manyPropertyName;
        }

        actualOnePropertyName = (onePropertyName == null)
                ? DatabaseLogic.getUniquePropertyName(fromTable, DatabaseLogic.normalizeName(getName()))
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

    @XmlAttribute
    public String getManyPropertyName() {
        return manyPropertyName;
    }

    public void setManyPropertyName(String manyPropertyName) {
        this.manyPropertyName = manyPropertyName;
    }

    @XmlAttribute
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
        return super.getHql();
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

    /**
     * Target table name, for legacy XML-based models.
     * @return the name of the table referenced by this fk.
     */
    @XmlAttribute(name = "toTable")
    public String getToTableName() {
        return toTableName;
    }

    public void setToTableName(String toTableName) {
        this.toTableName = toTableName;
    }

    public String getToEntityName() {
        return toEntityName;
    }

    public void setToEntityName(String toEntityName) {
        this.toEntityName = toEntityName;
    }

    @Override
    public String toString() {
        return MessageFormat.format("foreign key {0}", getQualifiedName());
    }

}
