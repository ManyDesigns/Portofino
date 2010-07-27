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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.Util;
import com.manydesigns.elements.annotations.*;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public abstract class AbstractField implements Field {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";
    protected final PropertyAccessor accessor;

    protected String id;
    protected String inputName;
    protected String label;
    protected String href;
    protected String alt;

    protected boolean required = false;
    protected boolean immutable = false;
    protected boolean forceNewRow = false;
    protected int colSpan = 1;
    protected String help;

    protected Mode mode = Mode.EDIT;

    protected List<String> errors = new ArrayList<String>();

    protected final Logger logger = LogUtil.getLogger(AbstractField.class);

    //--------------------------------------------------------------------------
    // Costruttori
    //--------------------------------------------------------------------------
    public AbstractField(PropertyAccessor accessor) {
        this(accessor, null);
    }

    public AbstractField(PropertyAccessor accessor, String prefix) {
        LogUtil.entering(logger, "AbstractField", accessor, prefix);

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
            logger.finer("LabelI18N annotation present with value: " + text);

            String args = null;
            String textCompare = MessageFormat.format(text, args);
            String i18NText = getText(text);
            label = i18NText;
            if (textCompare.equals(i18NText) && accessor.isAnnotationPresent(Label.class)) {
                label = accessor.getAnnotation(Label.class).value();
            }
        } else if (accessor.isAnnotationPresent(Label.class)) {
            label = accessor.getAnnotation(Label.class).value();
            logger.finer("Label annotation present with value: " + label);
        } else {
            label = Util.camelCaseToWords(accessor.getName());
            logger.finer("Setting label from property name: " + label);
        }

        if (accessor.isAnnotationPresent(Help.class)) {
            help = accessor.getAnnotation(Help.class).value();
            logger.finer("Help annotation present with value: " + help);
        }

        if (accessor.isAnnotationPresent(Required.class)) {
            required = true;
            logger.finer("Required annotation present");
        }

        if (accessor.isAnnotationPresent(Immutable.class)) {
            immutable = true;
            logger.finer("Immutable annotation present");
        }

        if (accessor.isAnnotationPresent(ForceNewRow.class)) {
            forceNewRow = true;
            logger.finer("ForceNewRow annotation present");
        }

        if (accessor.isAnnotationPresent(ColSpan.class)) {
            colSpan = accessor.getAnnotation(ColSpan.class).value();
            logger.finer("ColSpan annotation present with value: " + colSpan);
        }

        LogUtil.exiting(logger, "AbstractField");
    }

    //--------------------------------------------------------------------------
    // Implementation of Element
    //--------------------------------------------------------------------------

    public void toXhtml(XhtmlBuffer xb) {
        if (mode.isView(immutable)) {
            xb.openElement("th");
            labelToXhtml(xb);
            xb.closeElement("th");
            xb.openElement("td");
            if (colSpan != 1) {
                xb.addAttribute("colspan", Integer.toString(colSpan * 2 - 1));
            }
            valueToXhtml(xb);
            xb.closeElement("td");
        } else if (mode.isEdit()) {
            xb.openElement("th");
            labelToXhtml(xb);
            xb.closeElement("th");
            xb.openElement("td");
            if (colSpan != 1) {
                xb.addAttribute("colspan", Integer.toString(colSpan * 2 - 1));
            }
            valueToXhtml(xb);
            helpToXhtml(xb);
            errorsToXhtml(xb);
            xb.closeElement("td");
        } else if (mode.isPreview()) {
            xb.openElement("th");
            labelToXhtml(xb);
            xb.closeElement("th");
            xb.openElement("td");
            if (colSpan != 1) {
                xb.addAttribute("colspan", Integer.toString(colSpan * 2 - 1));
            }
            valueToXhtml(xb);
            xb.closeElement("td");
        } else if (mode.isHidden()) {
            valueToXhtml(xb);
        } else {
            throw new IllegalStateException("Unknown mode: " + mode);
        }
    }

    public void labelToXhtml(XhtmlBuffer xb) {
        xb.openElement("label");
        xb.addAttribute("for", id);
        xb.addAttribute("class", "field");
        if (isRequiredField()) {
            xb.openElement("span");
            xb.addAttribute("class", "required");
            xb.write("*");
            xb.closeElement("span");
            xb.writeNbsp();
        }
        xb.write(StringUtils.capitalize(label + ":"));
        xb.closeElement("label");
    }

    public void helpToXhtml(XhtmlBuffer xb) {
        if (help != null) {
            xb.openElement("div");
            xb.addAttribute("class", "inputdescription");
            xb.write(help);
            xb.closeElement("div");
        }
    }

    public void errorsToXhtml(XhtmlBuffer xb) {
        if (errors.size() > 0) {
            xb.openElement("ul");
            xb.addAttribute("class", "errors");
            for (String error : errors) {
                xb.openElement("li");
                xb.write(error);
                xb.closeElement("li");
            }
            xb.closeElement("ul");
        }
    }

    public String getText(String key, Object... args) {
        return ElementsThreadLocals.getTextProvider().getText(key, args);
    }

    public void readFromObject(Object obj) {
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

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

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isImmutable() {
        return immutable;
    }

    public void setImmutable(boolean immutable) {
        this.immutable = immutable;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public boolean isForceNewRow() {
        return forceNewRow;
    }

    public void setForceNewRow(boolean forceNewRow) {
        this.forceNewRow = forceNewRow;
    }

    public int getColSpan() {
        return colSpan;
    }

    public void setColSpan(int colSpan) {
        this.colSpan = colSpan;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public boolean isRequiredField() {
        return required && !mode.isView(immutable);
    }

    //--------------------------------------------------------------------------
    // Other methods
    //--------------------------------------------------------------------------

    public void writeToObject(Object obj, Object value) {
        if (mode.isView(immutable)) {
            return;
        }

        try {
            accessor.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }


}
