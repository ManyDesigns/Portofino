/*
* Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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
