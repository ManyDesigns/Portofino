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

package com.manydesigns.portofino.reflection;

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.logic.CrudLogic;
import com.manydesigns.portofino.model.pages.crud.Crud;
import com.manydesigns.portofino.model.pages.crud.CrudProperty;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            "Copyright (c) 2005-2011, ManyDesigns srl";


    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Crud crud;
    protected final ClassAccessor tableAccessor;
    protected final CrudPropertyAccessor[] propertyAccessors;
    protected final CrudPropertyAccessor[] keyPropertyAccessors;

    public final static Logger logger =
            LoggerFactory.getLogger(CrudAccessor.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public CrudAccessor(@NotNull Crud crud, @NotNull ClassAccessor tableAccessor) {
        super(crud.getModelAnnotations());
        this.crud = crud;
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
                    CrudLogic.findCrudPropertyByName(
                            crud, columnAccessor.getName());
            CrudPropertyAccessor propertyAccessor =
                        new CrudPropertyAccessor(crudProperty, columnAccessor);
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
    }


    //**************************************************************************
    // ClassAccessor implementation
    //**************************************************************************

    public String getName() {
        return crud.getName();
    }

    public CrudPropertyAccessor getProperty(String propertyName) throws NoSuchFieldException {
        for (CrudPropertyAccessor current : propertyAccessors) {
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

    public Crud getCrud() {
        return crud;
    }

}
