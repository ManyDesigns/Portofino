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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.LabelI18N;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class FieldUtils {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public static final Logger logger =
            LoggerFactory.getLogger(FieldUtils.class);

    public static String getLabel(PropertyAccessor accessor) {
        String label;
        if (accessor.isAnnotationPresent(LabelI18N.class)) {
            String text = accessor.getAnnotation(LabelI18N.class).value();
            logger.debug("LabelI18N annotation present with value: {}", text);

            String args = null;
            String textCompare = MessageFormat.format(text, args);
            String i18NText = ElementsThreadLocals.getTextProvider().getText(text);
            label = i18NText;
            if (textCompare.equals(i18NText) && accessor.isAnnotationPresent(Label.class)) {
                label = accessor.getAnnotation(Label.class).value();
            }
        } else if (accessor.isAnnotationPresent(Label.class)) {
            label = accessor.getAnnotation(Label.class).value();
            logger.debug("Label annotation present with value: {}", label);
        } else {
            label = Util.guessToWords(accessor.getName());
            logger.debug("Setting label from property name: {}", label);
        }
        return label;
    }

}
