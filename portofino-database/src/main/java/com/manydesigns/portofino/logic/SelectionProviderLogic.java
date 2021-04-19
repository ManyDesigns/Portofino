/*
 * Copyright (C) 2005-2021 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.logic;

import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.OptionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.TextFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Collection;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class SelectionProviderLogic {
    public static final String copyright =
            "Copyright (C) 2005-2021 ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(SelectionProviderLogic.class);

    @Deprecated
    public static DefaultSelectionProvider createSelectionProvider
            (String name, int fieldCount, Class[] fieldTypes, Collection<Object[]> objects) {
        return createSelectionProvider(name, fieldTypes, objects);
    }

    public static DefaultSelectionProvider createSelectionProvider
            (String name, Class[] fieldTypes, Collection<Object[]> objects) {
        DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider(name, fieldTypes.length);
        for (Object[] valueAndLabel : objects) {
            OptionProvider.Option option = getOption(fieldTypes, valueAndLabel);
            selectionProvider.appendOption(option);
        }
        return selectionProvider;
    }

    @NotNull
    public static OptionProvider.Option getOption(Class[] fieldTypes, Object[] valueAndLabel) {
        int fieldCount = fieldTypes.length;
        Object[] values = new Object[fieldCount];
        String[] labels = new String[fieldCount];

        for (int j = 0; j < fieldCount; j++) {
            Class valueType = fieldTypes[j];
            values[j] = OgnlUtils.convertValue(valueAndLabel[j * 2], valueType);
            labels[j] = OgnlUtils.convertValueToString(valueAndLabel[j*2+1]);
        }

        boolean active = true;
        if(valueAndLabel.length > 2 * fieldCount) {
            Object booleanValue = OgnlUtils.convertValue(valueAndLabel[fieldCount * 2], Boolean.class);
            active = booleanValue instanceof Boolean && (Boolean) booleanValue;
        }

        return new OptionProvider.Option(values, labels, active);
    }

    public static DefaultSelectionProvider createSelectionProvider(
            String name,
            Collection objects,
            PropertyAccessor[] propertyAccessors,
            @Nullable TextFormat[] textFormats
    ) {
        DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider(name, propertyAccessors.length);
        for (Object current : objects) {
            OptionProvider.Option option = getOption(name, propertyAccessors, textFormats, current);
            selectionProvider.appendOption(option);
        }
        return selectionProvider;
    }

    @NotNull
    public static OptionProvider.Option getOption(
            String name, PropertyAccessor[] propertyAccessors, @Nullable TextFormat[] textFormats, Object object) {
        boolean active = true;
        if(object instanceof Object[]) {
            Object[] valueAndActive = (Object[]) object;
            if(valueAndActive.length > 1) {
                active = valueAndActive[1] instanceof Boolean && (Boolean) valueAndActive[1];
            }
            if(valueAndActive.length > 0) {
                object = valueAndActive[0];
            } else {
                throw new IllegalArgumentException("Invalid selection provider query result - sp: " + name);
            }
        }
        Object[] values = new Object[propertyAccessors.length];
        String[] labels = new String[propertyAccessors.length];
        int j = 0;
        for (PropertyAccessor property : propertyAccessors) {
            Object value = property.get(object);
            values[j] = value;
            if (textFormats == null || textFormats[j] == null) {
                String label = OgnlUtils.convertValueToString(value);
                labels[j] = label;
            } else {
                TextFormat textFormat = textFormats[j];
                labels[j] = textFormat.format(object);
            }
            j++;
        }
        return new OptionProvider.Option(values, labels, active);
    }

    public static DefaultSelectionProvider createSelectionProvider
            (String name, Collection objects, Class objectClass,
             @Nullable TextFormat[] textFormats, String[] propertyNames) {
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(objectClass);
        PropertyAccessor[] propertyAccessors =
                new PropertyAccessor[propertyNames.length];
        for (int i = 0; i < propertyNames.length; i++) {
            String currentName = propertyNames[i];
            try {
                PropertyAccessor propertyAccessor =
                        classAccessor.getProperty(currentName);
                propertyAccessors[i] = propertyAccessor;
            } catch (Throwable e) {
                String msg = MessageFormat.format(
                        "Could not access property: {0}", currentName);
                logger.warn(msg, e);
                throw new IllegalArgumentException(msg, e);
            }
        }
        return createSelectionProvider(name, objects, propertyAccessors, textFormats);
    }
}
