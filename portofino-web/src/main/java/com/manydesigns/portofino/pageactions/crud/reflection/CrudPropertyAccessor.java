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

package com.manydesigns.portofino.pageactions.crud.reflection;

import com.manydesigns.elements.annotations.*;
import com.manydesigns.elements.annotations.impl.*;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudProperty;
import com.manydesigns.portofino.reflection.AbstractAnnotatedAccessor;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class CrudPropertyAccessor
        extends AbstractAnnotatedAccessor
        implements PropertyAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final CrudProperty crudProperty;
    protected final PropertyAccessor nestedAccessor;

    public static final Logger logger =
            LoggerFactory.getLogger(CrudPropertyAccessor.class);

    
    //**************************************************************************
    // Constructors
    //**************************************************************************

    public CrudPropertyAccessor(@Nullable CrudProperty crudProperty,
                                PropertyAccessor nestedAccessor,
                                boolean inKey) {
        super(crudProperty != null ? crudProperty.getAnnotations() : null);
        this.crudProperty = crudProperty;
        this.nestedAccessor = nestedAccessor;

        Enabled enabledAnn = nestedAccessor.getAnnotation(Enabled.class);
        boolean accessorEnabled = (enabledAnn == null || enabledAnn.value());

        Insertable insertableAnn = nestedAccessor.getAnnotation(Insertable.class);
        boolean accessorInsertable = (insertableAnn == null || insertableAnn.value());

        Updatable updatableAnn = nestedAccessor.getAnnotation(Updatable.class);
        boolean accessorUpdatable = (updatableAnn == null || updatableAnn.value());

        InSummary inSummaryAnn = nestedAccessor.getAnnotation(InSummary.class);
        boolean accessorInSummary = (inSummaryAnn == null || inSummaryAnn.value());

        Searchable searchableAnn = nestedAccessor.getAnnotation(Searchable.class);
        boolean accessorSearchable = (searchableAnn == null || searchableAnn.value());

        boolean crudEnabled;
        boolean crudInsertable;
        boolean crudUpdatable;
        boolean crudInSummary;
        boolean crudSearchable;

        if (crudProperty != null) {
            String label = crudProperty.getLabel();
            if (StringUtils.isNotEmpty(label)) {
                annotations.put(Label.class,
                        new LabelImpl(label));
            }
            crudEnabled    = crudProperty.isEnabled();
            crudSearchable = crudProperty.isSearchable();
            crudInSummary  = crudProperty.isInSummary();
            crudInsertable = crudProperty.isInsertable();
            crudUpdatable  = crudProperty.isUpdatable();
        } else {
            crudEnabled    = inKey;
            crudSearchable = false;
            crudInSummary  = inKey;
            crudInsertable = false;
            crudUpdatable  = false;
        }

        annotations.put(Searchable.class, new SearchableImpl(crudSearchable && accessorSearchable));
        annotations.put(InSummary.class, new InSummaryImpl(crudInSummary && accessorInSummary));
        annotations.put(Enabled.class, new EnabledImpl(crudEnabled && accessorEnabled));
        annotations.put(Insertable.class, new InsertableImpl(crudInsertable && accessorInsertable));
        annotations.put(Updatable.class, new UpdatableImpl(crudUpdatable && accessorUpdatable));
    }


    //**************************************************************************
    // PropertyAccessor implementation
    //**************************************************************************

    public String getName() {
        return nestedAccessor.getName();
    }

    public Class getType() {
        return nestedAccessor.getType();
    }

    public int getModifiers() {
        return nestedAccessor.getModifiers();
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T annotation = super.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        return nestedAccessor.getAnnotation(annotationClass);
    }

    public Object get(Object obj) {
        return nestedAccessor.get(obj);
    }

    public void set(Object obj, Object value) {
        nestedAccessor.set(obj, value);
    }

    public CrudProperty getCrudProperty() {
        return crudProperty;
    }
}
