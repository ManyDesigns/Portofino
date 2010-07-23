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

package com.manydesigns.portofino.elements;

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class SearchFormBuilder {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    private final ClassAccessor classAccessor;
    protected List<PropertyAccessor> propertyAccessors;
    protected String prefix;

    protected Logger logger = Logger.getLogger(SearchFormBuilder.class.getName());

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public SearchFormBuilder(Class clazz) {
        this(new JavaClassAccessor(clazz));
    }

    public SearchFormBuilder(ClassAccessor classAccessor) {
        logger.entering("SearchFormBuilder", "SearchFormBuilder", classAccessor);

        this.classAccessor = classAccessor;

        logger.exiting("SearchFormBuilder", "SearchFormBuilder");
    }

    //**************************************************************************
    // Builder configuration
    //**************************************************************************

    public SearchFormBuilder configFields(String... fieldNames) {
        logger.entering("SearchFormBuilder", "configFields");

        propertyAccessors = new ArrayList<PropertyAccessor>();
        for (String current : fieldNames) {
            try {
                PropertyAccessor accessor =
                        classAccessor.getProperty(current);
                propertyAccessors.add(accessor);
            } catch (NoSuchFieldException e) {
                logger.log(Level.WARNING, "Field not found: " + current, e);
            }
        }

        logger.exiting("SearchFormBuilder", "configFields");
        return this;
    }

    public SearchFormBuilder configPrefix(String prefix) {
        logger.entering("SearchFormBuilder", "configPrefix", prefix);

        this.prefix = prefix;

        logger.exiting("SearchFormBuilder", "configPrefix");
        return this;
    }

    public SearchFormBuilder configReflectiveFields() {
        logger.entering("SearchFormBuilder", "configReflectiveFields");

        propertyAccessors = new ArrayList<PropertyAccessor>();

        for (PropertyAccessor current : classAccessor.getProperties()) {
            if (Modifier.isStatic(current.getModifiers())) {
                logger.finer("Skipping static field: " + current.getName());
                continue;
            }

            propertyAccessors.add(current);
        }

        logger.exiting("SearchFormBuilder", "configReflectiveFields");
        return this;
    }

    //**************************************************************************
    // Building
    //**************************************************************************

    public SearchForm build() {
        logger.entering("SearchFormBuilder", "build");

        SearchForm form = new SearchForm();
        //FieldHelper fieldHelper = ElementsThreadLocals.getFieldHelper();

        if (propertyAccessors == null) {
            configReflectiveFields();
        }

        /*
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            SearchField field = null;

            if (field == null) {
                continue;
            }
            form.add(field);
        }
        */

        logger.exiting("SearchFormBuilder", "build");
        return form;
    }
}
