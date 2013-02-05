/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.forms;

import com.manydesigns.elements.annotations.Enabled;
import com.manydesigns.elements.fields.helpers.FieldsManager;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class AbstractFormBuilder {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public final static String[] PROPERTY_NAME_BLACKLIST = {"class"};

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final FieldsManager manager;
    protected final ClassAccessor classAccessor;
    protected final Map<String[], SelectionProvider> selectionProviders;

    protected String prefix;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(AbstractFormBuilder.class);
    
    //**************************************************************************
    // Fields
    //**************************************************************************

    public AbstractFormBuilder(ClassAccessor classAccessor) {
        logger.debug("Entering AbstractBuilder constructor");

        manager = FieldsManager.getManager();
        this.classAccessor = classAccessor;
        selectionProviders = new HashMap<String[], SelectionProvider>();

        logger.debug("Exiting AbstractBuilder constructor");
    }

    //**************************************************************************
    // Utility methods
    //**************************************************************************

    protected boolean skippableProperty(PropertyAccessor propertyAccessor) {
        // static field?
        if (Modifier.isStatic(propertyAccessor.getModifiers())) {
            return true;
        }
        // blacklisted?
        if (ArrayUtils.contains(PROPERTY_NAME_BLACKLIST,
                propertyAccessor.getName())) {
            return true;
        }
        return false;
    }


    protected void removeUnusedSelectionProviders(
            Collection<PropertyAccessor> propertyAccessors) {
        List<String> propertyNames = new ArrayList<String>();
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            propertyNames.add(propertyAccessor.getName());
        }
        List<String[]> removeList = new ArrayList<String[]>();
        for (String[] current : selectionProviders.keySet()) {
            List<String> currentNames = Arrays.asList(current);
            if (!propertyNames.containsAll(currentNames)) {
                removeList.add(current);
            }
        }
        for (String[] current : removeList) {
            selectionProviders.remove(current);
        }
    }

    protected boolean isPropertyEnabled(PropertyAccessor propertyAccessor) {
        // check if field is enabled
        Enabled enabled = propertyAccessor.getAnnotation(Enabled.class);
        if(enabled != null && !enabled.value()) {
        logger.debug("Skipping non-enabled field: {}",
                propertyAccessor.getName());
            return false;
    }
        return true;
    }
}
