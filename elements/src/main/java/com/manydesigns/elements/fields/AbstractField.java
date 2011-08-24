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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.*;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.Util;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public abstract class AbstractField implements Field {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public final static String BULK_SUFFIX = "_bulk";
    public static final String FORM_LABEL_CLASS = "mde-field-label";

    protected final Configuration elementsConfiguration;

    protected final PropertyAccessor accessor;
    protected final Mode mode;

    protected String id;
    protected String bulkCheckboxName;
    protected boolean bulkChecked;
    protected String inputName;
    protected String label;
    protected String href;
    protected String title;

    protected boolean required = false;
    protected boolean enabled = true;
    protected boolean insertable = true;
    protected boolean updatable = true;

    protected boolean forceNewRow = false;
    protected int colSpan = 1;
    protected String help;
    protected String fieldCssClass;


    protected List<String> errors = new ArrayList<String>();

    public static final Logger logger =
            LoggerFactory.getLogger(AbstractField.class);

    //**************************************************************************
    // Costruttori
    //**************************************************************************
    public AbstractField(@NotNull PropertyAccessor accessor,
                         @NotNull Mode mode) {
        this(accessor, mode, null);
    }

    public AbstractField(@NotNull PropertyAccessor accessor,
                         @NotNull Mode mode,
                         @Nullable String prefix) {
        elementsConfiguration = ElementsProperties.getConfiguration();
        this.accessor = accessor;
        this.mode = mode;

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
        Object[] bulkInputNameArgs = {inputName,  "_bulk"};
        bulkCheckboxName = StringUtils.join(bulkInputNameArgs);

        if (accessor.isAnnotationPresent(LabelI18N.class)) {
            String text = accessor.getAnnotation(LabelI18N.class).value();
            logger.debug("LabelI18N annotation present with value: {}", text);

            String args = null;
            String textCompare = MessageFormat.format(text, args);
            String i18NText = getText(text);
            label = i18NText;
            if (textCompare.equals(i18NText) && accessor.isAnnotationPresent(Label.class)) {
                label = accessor.getAnnotation(Label.class).value();
            }
        } else if (accessor.isAnnotationPresent(Label.class)) {
            label = accessor.getAnnotation(Label.class).value();
            logger.debug("Label annotation present with value: {}", label);
        } else {
            label = Util.camelCaseToWords(accessor.getName());
            logger.debug("Setting label from property name: {}", label);
        }

        if (accessor.isAnnotationPresent(Help.class)) {
            help = accessor.getAnnotation(Help.class).value();
            logger.debug("Help annotation present with value: {}", help);
        }

        Enabled enabledAnnotation =
                accessor.getAnnotation(Enabled.class);
        if (enabledAnnotation != null) {
            enabled = enabledAnnotation.value();
            logger.debug("Enabled annotation present with value: {}",
                    enabled);
        }

        Insertable insertableAnnotation =
                accessor.getAnnotation(Insertable.class);
        if (insertableAnnotation != null) {
            insertable = insertableAnnotation.value();
            logger.debug("Insertable annotation present with value: {}",
                    insertable);
        }

        Updatable updatableAnnotation =
                accessor.getAnnotation(Updatable.class);
        if (updatableAnnotation != null) {
            updatable = updatableAnnotation.value();
            logger.debug("Updatable annotation present with value: {}",
                    updatable);
        }

        Required requiredAnnotation = accessor.getAnnotation(Required.class);
        if (requiredAnnotation != null) {
            required = requiredAnnotation.value();
            logger.debug("Required annotation present with value: {}",
                    required);
        }

        if (accessor.isAnnotationPresent(ForceNewRow.class)) {
            forceNewRow = true;
            logger.debug("ForceNewRow annotation present");
        }

        if (accessor.isAnnotationPresent(ColSpan.class)) {
            colSpan = accessor.getAnnotation(ColSpan.class).value();
            logger.debug("ColSpan annotation present with value: " + colSpan);
        }
    }

    //**************************************************************************
    // Implementation of Element
    //**************************************************************************

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        if (mode.isView(insertable, updatable)) {
            if (mode.isBulk()) {
                xb.openElement("td");
                xb.writeNbsp();
                xb.closeElement("td");
            }

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
            if (mode.isBulk()) {
                xb.openElement("td");
                xb.writeInputCheckbox(null, bulkCheckboxName,
                        "checked", bulkChecked);
                xb.closeElement("td");
            }

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
        xb.addAttribute("class", FORM_LABEL_CLASS);
        if (isRequiredField()) {
            xb.openElement("span");
            xb.addAttribute("class", "required");
            xb.write("*");
            xb.closeElement("span");
            xb.writeNbsp();
        }
        String actualLabel;
        boolean capitalize = elementsConfiguration.getBoolean(
                ElementsProperties.FIELDS_LABEL_CAPITALIZE);
        if (capitalize) {
            actualLabel = StringUtils.capitalize(label + ":");
        } else {
            actualLabel = label + ":";
        }
        xb.write(actualLabel);
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

    public void readFromRequest(HttpServletRequest req) {
        bulkChecked = (req.getParameter(bulkCheckboxName) != null);
    }

    public void readFromObject(Object obj) {
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public PropertyAccessor getPropertyAccessor() {
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

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public Mode getMode() {
        return mode;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isRequiredField() {
        return required && !mode.isView(insertable, updatable);
    }

    public String getFieldCssClass() {
        return fieldCssClass;
    }

    public void setFieldCssClass(String fieldCssClass) {
        this.fieldCssClass = fieldCssClass;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isInsertable() {
        return insertable;
    }

    public void setInsertable(boolean insertable) {
        this.insertable = insertable;
    }

    public boolean isUpdatable() {
        return updatable;
    }

    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    //**************************************************************************
    // Other methods
    //**************************************************************************

    public void writeToObject(@NotNull Object obj, @Nullable Object value) {
        if (mode.isView(insertable, updatable) || (mode.isBulk() && !bulkChecked)) {
            return;
        }
        Object convertedValue =
                OgnlUtils.convertValue(value, accessor.getType());
        accessor.set(obj, convertedValue);
    }
}
