/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.logic;

import com.manydesigns.elements.annotations.ShortName;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.DisplayMode;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.text.TextFormat;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.application.QueryUtils;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.reflection.TableAccessor;
import org.hibernate.Session;
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
            "Copyright (c) 2005-2012, ManyDesigns srl";

    private static final Logger logger = LoggerFactory.getLogger(SelectionProviderLogic.class);

    public static DefaultSelectionProvider createSelectionProvider
            (String name, int fieldCount, Class[] fieldTypes, Collection<Object[]> objects) {
        DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider(name, fieldCount);
        for (Object[] valueAndLabel : objects) {
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

            selectionProvider.appendRow(values, labels, active);
        }
        return selectionProvider;
    }

    public static DefaultSelectionProvider createSelectionProvider(
            String name,
            Collection objects,
            PropertyAccessor[] propertyAccessors,
            TextFormat[] textFormats
    ) {
        int fieldsCount = propertyAccessors.length;
        DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider(name, propertyAccessors.length);
        for (Object current : objects) {
            boolean active = true;
            if(current instanceof Object[]) {
                Object[] valueAndActive = (Object[]) current;
                if(valueAndActive.length > 1) {
                    active = valueAndActive[1] instanceof Boolean && (Boolean) valueAndActive[1];
                }
                if(valueAndActive.length > 0) {
                    current = valueAndActive[0];
                } else {
                    throw new IllegalArgumentException("Invalid selection provider query result - sp: " + name);
                }
            }
            Object[] values = new Object[fieldsCount];
            String[] labels = new String[fieldsCount];
            int j = 0;
            for (PropertyAccessor property : propertyAccessors) {
                Object value = property.get(current);
                values[j] = value;
                if (textFormats == null || textFormats[j] == null) {
                    String label = OgnlUtils.convertValueToString(value);
                    labels[j] = label;
                } else {
                    TextFormat textFormat = textFormats[j];
                    labels[j] = textFormat.format(current);
                }
                j++;
            }
            selectionProvider.appendRow(values, labels, active);
        }
        return selectionProvider;
    }

    public static DefaultSelectionProvider createSelectionProvider
            (String name, Collection objects, Class objectClass,
             TextFormat[] textFormats, String[] propertyNames) {
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

    public static DefaultSelectionProvider createSelectionProviderFromHql
            (String name, Application application, String databaseName, String hql, DisplayMode dm) {
        Database database = DatabaseLogic.findDatabaseByName(application.getModel(), databaseName);
        Table table = QueryUtils.getTableFromQueryString(database, hql);
        String entityName = table.getActualEntityName();
        Session session = application.getSession(databaseName);
        Collection<Object> objects = QueryUtils.getObjects(session, hql, null, null);
        TableAccessor tableAccessor =
                application.getTableAccessor(databaseName, entityName);
        ShortName shortNameAnnotation =
                tableAccessor.getAnnotation(ShortName.class);
        TextFormat[] textFormats = null;
        //L'ordinamento e' usato solo in caso di chiave singola
        if (shortNameAnnotation != null && tableAccessor.getKeyProperties().length == 1) {
            textFormats = new TextFormat[] {
                OgnlTextFormat.create(shortNameAnnotation.value())
            };
        }

        DefaultSelectionProvider selectionProvider = createSelectionProvider
                (name, objects, tableAccessor.getKeyProperties(), textFormats);
        selectionProvider.setDisplayMode(dm);
        return selectionProvider;
    }
}
