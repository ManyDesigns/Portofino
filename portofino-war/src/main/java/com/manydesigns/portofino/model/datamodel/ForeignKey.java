/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.annotations.Annotation;
import com.manydesigns.portofino.util.Pair;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(value = XmlAccessType.NONE)
public class ForeignKey extends DatabaseSelectionProvider implements ModelObject, HasReferences {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

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

    protected final List<Annotation> annotations;

    protected String onUpdate;
    protected String onDelete;

    //**************************************************************************
    // Fields (logical)
    //**************************************************************************

    protected String manyPropertyName;
    protected String onePropertyName;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected String actualManyPropertyName;
    protected String actualOnePropertyName;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(ForeignKey.class);

    //**************************************************************************
    // Constructors and init
    //**************************************************************************

    public ForeignKey() {
        annotations = new ArrayList<Annotation>();
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
        actualManyPropertyName = null;
        actualOnePropertyName = null;

        for (Reference reference : references) {
            reference.reset();
        }
    }

    @Override
    public void init(Model model) {
        super.init(model);

        assert fromTable != null;
        assert name != null;
        assert toSchema != null;

        if (references.isEmpty()) {
            throw new Error(MessageFormat.format(
                    "Foreign key {0} has no referneces",
                    getQualifiedName()));
        }

        if (toTable != null) {
            // wire up Table.oneToManyRelationships
            toTable.getOneToManyRelationships().add(this);
            //Build HQL query
            hql = "from " + toTable.getActualEntityName();
        }

        for (Reference reference : references) {
            reference.init(model);
        }

        actualManyPropertyName = (manyPropertyName == null)
                ? name
                : manyPropertyName;

        actualOnePropertyName = (onePropertyName == null)
                ? name
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
        logger.debug("Model annotation not found: {}", annotationType);
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

    @XmlElementWrapper(name="annotations")
    @XmlElement(name="annotation",type=Annotation.class)
    public List<Annotation> getAnnotations() {
        return annotations;
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
