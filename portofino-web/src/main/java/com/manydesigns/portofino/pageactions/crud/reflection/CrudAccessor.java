/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudConfiguration;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudProperty;
import com.manydesigns.portofino.reflection.AbstractAnnotatedAccessor;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class CrudAccessor
        extends AbstractAnnotatedAccessor
        implements ClassAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";


    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final CrudConfiguration crudConfiguration;
    protected final ClassAccessor tableAccessor;
    protected final CrudPropertyAccessor[] propertyAccessors;
    protected final CrudPropertyAccessor[] keyPropertyAccessors;

    public final static Logger logger =
            LoggerFactory.getLogger(CrudAccessor.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public CrudAccessor(@NotNull final CrudConfiguration crudConfiguration, @NotNull ClassAccessor tableAccessor) {
        super(null);
        this.crudConfiguration = crudConfiguration;
        this.tableAccessor = tableAccessor;
        PropertyAccessor[] columnAccessors = tableAccessor.getProperties();
        PropertyAccessor[] keyColumnAccessors = tableAccessor.getKeyProperties();

        propertyAccessors =
                new CrudPropertyAccessor[columnAccessors.length];
        keyPropertyAccessors =
                new CrudPropertyAccessor[keyColumnAccessors.length];

        int i = 0;
        for (PropertyAccessor columnAccessor : columnAccessors) {
            CrudProperty crudProperty =
                    findCrudPropertyByName(
                            crudConfiguration, columnAccessor.getName());
            boolean inKey = ArrayUtils.contains(keyColumnAccessors, columnAccessor);
            CrudPropertyAccessor propertyAccessor =
                        new CrudPropertyAccessor(crudProperty, columnAccessor, inKey);
            propertyAccessors[i] = propertyAccessor;
            i++;
        }

        i = 0;
        for (PropertyAccessor keyColumnAccessor : keyColumnAccessors) {
            String propertyName = keyColumnAccessor.getName();
            try {
                CrudPropertyAccessor keyPropertyAccessor =
                        getProperty(keyColumnAccessor.getName());
                keyPropertyAccessors[i] = keyPropertyAccessor;
            } catch (NoSuchFieldException e) {
                logger.error("Could not find key property: " + propertyName, e);
            }
            i++;
        }


/*        logger.debug("Sorting crud properties to preserve their previous order as much as possible");
        Arrays.sort(propertyAccessors, new Comparator<CrudPropertyAccessor>() {
            private int oldIndex(CrudPropertyAccessor c) {
                int i = 0;
                for (CrudProperty old : crudConfiguration.getProperties()) {
                    if (old.equals(c.getCrudProperty())) {
                        return i;
                    }
                    i++;
                }
                return -1;
            }

            public int compare(CrudPropertyAccessor c1, CrudPropertyAccessor c2) {
                Integer index1 = oldIndex(c1);
                Integer index2 = oldIndex(c2);
                if (index1 != -1) {
                    if (index2 != -1) {
                        return index1.compareTo(index2);
                    } else {
                        return -1;
                    }
                } else {
                    return index2 == -1 ? 0 : 1;
                }
            }
        });*/
    }

    public static CrudProperty findCrudPropertyByName(CrudConfiguration crudConfiguration, String propertyName) {
        for (CrudProperty current : crudConfiguration.getProperties()) {
            if (current.getName().equalsIgnoreCase(propertyName)) {
                return current;
            }
        }
        return null;
    }


    //**************************************************************************
    // ClassAccessor implementation
    //**************************************************************************

    public String getName() {
        return crudConfiguration.getName();
    }

    public CrudPropertyAccessor getProperty(String propertyName) throws NoSuchFieldException {
        for (CrudPropertyAccessor current : propertyAccessors) {
            //XXX Alessio verificare
            if (current.getName().equalsIgnoreCase(propertyName)) {
                return current;
            }
        }

        throw new NoSuchFieldException(propertyName + " (of use case " + getName() + ")");
    }

    public PropertyAccessor[] getProperties() {
        return propertyAccessors.clone();
    }

    public PropertyAccessor[] getKeyProperties() {
        return keyPropertyAccessors.clone();
    }

    public Object newInstance() {
        return tableAccessor.newInstance();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T annotation = super.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        return tableAccessor.getAnnotation(annotationClass);
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public CrudConfiguration getCrudConfiguration() {
        return crudConfiguration;
    }

}
