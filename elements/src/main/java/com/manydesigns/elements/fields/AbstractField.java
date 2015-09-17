/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.KeyValueAccessor;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.*;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public abstract class AbstractField<T> implements Field<T> {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final String INPUT_CONTAINER_CSS_CLASS = "input-container";
    public static final String FORM_LABEL_CSS_CLASS = "control-label";
    public static final String EDITABLE_FIELD_CSS_CLASS = "form-control";
    public static final String STATIC_VALUE_CSS_CLASS = "form-control-static";

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
    protected @NotNull String fieldCssClass;

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

        label = FieldUtils.getLabel(accessor);

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

        if (accessor.isAnnotationPresent(CssClass.class)) {
            String[] cssClasses = accessor.getAnnotation(CssClass.class).value();
            fieldCssClass = StringUtils.join(cssClasses, " ");
        } else {
            fieldCssClass = "";
        }
    }

    //**************************************************************************
    // Implementation of Element
    //**************************************************************************

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        if (mode.isView(insertable, updatable)) {
            openVisibleField(xb);
            valueToXhtml(xb);
            closeVisibleField(xb);
        } else if (mode.isEdit()) {
            openVisibleField(xb);
            valueToXhtml(xb);
            helpToXhtml(xb);
            errorsToXhtml(xb);
            closeVisibleField(xb);
        } else if (mode.isPreview()) {
            openVisibleField(xb);
            valueToXhtml(xb);
            closeVisibleField(xb);
        } else if (mode.isHidden()) {
            valueToXhtml(xb);
        } else {
            throw new IllegalStateException("Unknown mode: " + mode);
        }
    }

    /**
     * Writes the HTML before a visible input field. The HTML includes the label and any necessary layout
     * tags.
     * @param xb the buffer to write to.
     */
    protected void openVisibleField(XhtmlBuffer xb) {
        xb.openElement("div");
        String cssClass = "form-group";
        if(mode.isView(insertable, updatable)) {
            cssClass += " readonly";
        } else {
            cssClass += " readwrite";
        }
        if(errors.size() > 0) {
            cssClass += " has-error";
        }
        if(hasRequiredFields()) {
            cssClass += " required";
        }
        xb.addAttribute("class", cssClass);
        labelToXhtml(xb);
        xb.openElement("div");
        xb.addAttribute("class", INPUT_CONTAINER_CSS_CLASS + " " + fieldCssClass);
    }

    /**
     * Writes the HTML after a visible input field. The HTML closes any necessary layout tags opened by
     * openVisibleField().
     * @param xb the buffer to write to.
     */
    protected void closeVisibleField(XhtmlBuffer xb) {
        xb.closeElement("div");
        xb.closeElement("div");
    }

    public void labelToXhtml(XhtmlBuffer xb) {
        if(StringUtils.isBlank(label)) {
            return;
        }


        xb.openElement("label");
        if(!mode.isView(insertable, updatable)) {
            xb.addAttribute("for", id); //HTML5 validation
        }
        xb.addAttribute("class", FORM_LABEL_CSS_CLASS);

        if (mode.isBulk() && mode.isEdit() && !mode.isView(insertable, updatable)) {
            String cid = id+"_check";
            xb.openElement("div");
            xb.addAttribute("class", "pull-left checkbox");

            xb.writeInputCheckbox(cid, bulkCheckboxName, "checked", bulkChecked, false, "");
            xb.openElement("label");
            xb.addAttribute("for", cid);
            xb.addAttribute("class", "pull-left");
            xb.closeElement("label");
            xb.closeElement("div");
        }


        String actualLabel;
        boolean capitalize = elementsConfiguration.getBoolean(
                ElementsProperties.FIELDS_LABEL_CAPITALIZE);
        if (capitalize) {
            actualLabel = StringUtils.capitalize(label);
        } else {
            actualLabel = label;
        }
        xb.write(actualLabel);
        xb.closeElement("label");

    }

    public void helpToXhtml(XhtmlBuffer xb) {
        if (help != null) {
            xb.openElement("span");
            xb.addAttribute("class", "help-block");
            xb.write(help);
            xb.closeElement("span");
        }
    }

    public void errorsToXhtml(XhtmlBuffer xb) {
        if (errors.size() > 0) {
            xb.openElement("span");
            xb.addAttribute("class", "help-block");
            for (String error : errors) {
                xb.write(error);
                xb.writeBr();
            }
            xb.closeElement("span");
        }
    }

    public String getText(String key, Object... args) {
        return ElementsThreadLocals.getTextProvider().getText(key, args);
    }

    public void readFromRequest(HttpServletRequest req) {
        bulkChecked = (req.getParameter(bulkCheckboxName) != null);
    }

    public void readFromObject(Object obj) {}

    @Override
    public void readFrom(KeyValueAccessor keyValueAccessor) {
        if (mode.isView(insertable, updatable)) {
            return;
        }
        Object value = keyValueAccessor.get(accessor.getName());
        if(value instanceof String) {
            setStringValue((String) value);
        } else {
            setValue((T) value);
        }
    }

    @Override
    public void writeTo(KeyValueAccessor keyValueAccessor) {
        keyValueAccessor.set(accessor.getName(), getValue());
    }

    public String getDisplayValue() {
        return getStringValue();
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

    public boolean hasRequiredFields() {
        return required && !mode.isView(insertable, updatable);
    }

    public @NotNull String getFieldCssClass() {
        return fieldCssClass;
    }

    public void setFieldCssClass(@NotNull String fieldCssClass) {
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
