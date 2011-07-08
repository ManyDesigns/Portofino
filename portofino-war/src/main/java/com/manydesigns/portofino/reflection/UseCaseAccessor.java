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

package com.manydesigns.portofino.reflection;

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.model.site.usecases.UseCase;
import com.manydesigns.portofino.model.site.usecases.UseCaseProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class UseCaseAccessor
        extends AbstractAnnotatedAccessor
        implements ClassAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";


    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final UseCase useCase;
    protected final ClassAccessor tableAccessor;
    protected final UseCasePropertyAccessor[] propertyAccessors;
    protected final UseCasePropertyAccessor[] keyPropertyAccessors;

    public final static Logger logger =
            LoggerFactory.getLogger(UseCaseAccessor.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public UseCaseAccessor(UseCase useCase, TableAccessor tableAccessor) {
        super(useCase.getModelAnnotations());
        this.useCase = useCase;
        this.tableAccessor = tableAccessor;
        List<UseCaseProperty> properties = useCase.getProperties();
        PropertyAccessor[] keyColumnAccessors = tableAccessor.getKeyProperties();

        propertyAccessors = new UseCasePropertyAccessor[properties.size()];
        keyPropertyAccessors =
                new UseCasePropertyAccessor[keyColumnAccessors.length];

        int i = 0;
        for (UseCaseProperty property : properties) {
            String propertyName = property.getName();
            try {
                PropertyAccessor columnAccessor =
                        tableAccessor.getProperty(propertyName);
                UseCasePropertyAccessor propertyAccessor =
                        new UseCasePropertyAccessor(property, columnAccessor);
                propertyAccessors[i] = propertyAccessor;
            } catch (NoSuchFieldException e) {
                logger.error("Could not access table property: " +
                        propertyName, e);
            }
            i++;
        }

        i = 0;
        for (PropertyAccessor keyColumnAccessor : keyColumnAccessors) {
            String propertyName = keyColumnAccessor.getName();
            try {
                UseCasePropertyAccessor keyPropertyAccessor =
                        getProperty(keyColumnAccessor.getName());
                keyPropertyAccessors[i] = keyPropertyAccessor;
            } catch (NoSuchFieldException e) {
                logger.error("Could not find key property: " + propertyName, e);
            }
            i++;
        }
    }


    //**************************************************************************
    // ClassAccessor implementation
    //**************************************************************************

    public String getName() {
        return useCase.getName();
    }

    public UseCasePropertyAccessor getProperty(String propertyName) throws NoSuchFieldException {
        for (UseCasePropertyAccessor current : propertyAccessors) {
            if (current.getName().equals(propertyName)) {
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


    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public UseCase getUseCase() {
        return useCase;
    }

}
