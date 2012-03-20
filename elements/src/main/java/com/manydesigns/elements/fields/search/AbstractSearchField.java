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

package com.manydesigns.elements.fields.search;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.annotations.*;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.Util;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public abstract class AbstractSearchField implements SearchField {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected final PropertyAccessor accessor;

    protected String id;
    protected String inputName;
    protected String label;
    protected boolean required;

    public static final Logger logger =
            LoggerFactory.getLogger(AbstractSearchField.class);
    public static final String ATTR_NAME_HTML_CLASS = "attr_name";

    //**************************************************************************
    // Costruttori
    //**************************************************************************
    public AbstractSearchField(PropertyAccessor accessor) {
        this(accessor, null);
    }

    public AbstractSearchField(PropertyAccessor accessor, String prefix) {
        this.accessor = accessor;

        String localId;
        if (accessor.isAnnotationPresent(Id.class)) {
            localId = accessor.getAnnotation(Id.class).value();
        } else {
            localId = accessor.getName();
        }
        Object[] idArgs = {prefix, localId};
        id = StringUtils.join(idArgs);

        String localInputName;
        if (accessor.isAnnotationPresent(InputName.class)) {
            localInputName = accessor.getAnnotation(InputName.class).value();
        } else {
            localInputName = accessor.getName();
        }
        Object[] inputNameArgs = {prefix, localInputName};
        inputName = StringUtils.join(inputNameArgs);

        if (accessor.isAnnotationPresent(LabelI18N.class)) {
            String text = accessor.getAnnotation(LabelI18N.class).value();
            logger.debug("LabelI18N annotation present with value: {}", text);

            label = getText(text);
        } else if (accessor.isAnnotationPresent(Label.class)) {
            label = accessor.getAnnotation(Label.class).value();
            logger.debug("Label annotation present with value: ", label);
        } else {
            label = Util.guessToWords(accessor.getName());
            logger.debug("Setting label from property name: ", label);
        }

        Required requiredAnnotation = accessor.getAnnotation(Required.class);
        if (requiredAnnotation != null) {
            required = requiredAnnotation.value();
            logger.debug("Required annotation present with value: {}", required);
        }
    }

    //**************************************************************************
    // Implementation of Element
    //**************************************************************************

    public String getText(String key, Object... args) {
        return ElementsThreadLocals.getTextProvider().getText(key, args);
    }

    public void readFromObject(Object obj) {
    }

    public void writeToObject(Object obj) {
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public PropertyAccessor getAccessor() {
        return accessor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    protected void appendToSearchString(StringBuilder sb, String name, String value) {
        if (sb.length() > 0) {
            sb.append(",");
        }
        sb.append(name);
        sb.append("=");
        sb.append(value);
    }
}
