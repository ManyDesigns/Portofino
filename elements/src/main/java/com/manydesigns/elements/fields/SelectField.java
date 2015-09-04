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

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.Select;
import com.manydesigns.elements.json.JsonBuffer;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.DisplayMode;
import com.manydesigns.elements.options.SelectionModel;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Map;

/*
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 */
public class SelectField extends AbstractField<Object> {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public final static String AUTOCOMPLETE_SUFFIX = "_autocomplete";

    protected SelectionModel selectionModel;
    protected int selectionModelIndex;
    protected SelectField previousSelectField;
    protected SelectField nextSelectField;

    protected String comboLabel;
    protected DisplayMode displayMode;

    protected String autocompleteId;
    protected String autocompleteInputName;

    protected String createNewValueHref;
    protected String createNewValueText;

    //**************************************************************************
    // Costruttori
    //**************************************************************************
    public SelectField(PropertyAccessor accessor, Mode mode, String prefix) {
        this(accessor, null, mode, prefix);
    }

    public SelectField(PropertyAccessor accessor, SelectionProvider selectionProvider,
                       Mode mode, String prefix) {
        super(accessor, mode, prefix);

        Select annotation = accessor.getAnnotation(Select.class);

        boolean nullOption = (annotation == null) || annotation.nullOption();

        if(selectionProvider == null) {
            Object[] values;
            String[] labels;

            if (annotation == null) {
                values = new Object[0];
                labels = new String[0];
            } else {
                values = annotation.values();
                labels = annotation.labels();
                displayMode = annotation.displayMode();
            }

            assert(values.length == labels.length);
            if(values.length > 0) {
                selectionProvider = createValuesSelectionProvider(accessor, values, labels);
            } else if (accessor.getType().isEnum()) {
                selectionProvider = createEnumSelectionProvider(accessor);
            }
        } else {
            displayMode = selectionProvider.getDisplayMode();
            if(displayMode == null && annotation != null) {
                displayMode = annotation.displayMode();
            }
        }

        if(displayMode == null) {
            displayMode = DisplayMode.DROPDOWN;
        }

        if(selectionProvider != null) {
            selectionModel = selectionProvider.createSelectionModel();
            selectionModelIndex = 0;
            createNewValueHref = selectionProvider.getCreateNewValueHref();
            createNewValueText = selectionProvider.getCreateNewValueText();
        }

        if(nullOption) {
            comboLabel = getText("elements.field.select.select", label);
        }
        autocompleteId = id + AUTOCOMPLETE_SUFFIX;
        autocompleteInputName = inputName + AUTOCOMPLETE_SUFFIX;
    }

    public static SelectionProvider createEnumSelectionProvider(PropertyAccessor accessor) {
        try {
            Method valuesMethod = accessor.getType().getMethod("values");
            Enum[] values = (Enum[]) valuesMethod.invoke(null);
            String[] labels = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                labels[i] = values[i].name();
            }
            return createValuesSelectionProvider(accessor, values, labels);
        } catch (Exception e) {
            logger.error("Cannot create Selection provider from enumeration", e);
            throw new Error(e);
        }
    }

    public static SelectionProvider createValuesSelectionProvider
            (PropertyAccessor accessor, Object[] values, String[] labels) {
        DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider(accessor.getName(), 1);
        for(int i = 0; i < values.length; i++) {
            selectionProvider.appendRow(values[i], labels[i], true);
        }
        return selectionProvider;
    }

    //**************************************************************************
    // Element implementation
    //**************************************************************************

    public void readFromRequest(HttpServletRequest req) {
        super.readFromRequest(req);

        if (mode.isView(insertable, updatable)) {
            return;
        }

        String stringValue = req.getParameter(inputName);
        Object value;
        if(DisplayMode.AUTOCOMPLETE == displayMode) {
            if (StringUtils.isEmpty(stringValue)) {
                value = null;
                //Attempt to find the value among the options
                String userValue = req.getParameter(autocompleteInputName);
                Map<Object, SelectionModel.Option> options = selectionModel.getOptions(selectionModelIndex);
                boolean found = false;
                for(SelectionModel.Option option : options.values()) {
                    if(ObjectUtils.equals(userValue, option.label)) {
                        found = true;
                        value = option.value;
                    }
                }
                if(!found) {
                    return;
                }
            } else {
                value = OgnlUtils.convertValue(stringValue, accessor.getType());
            }
        } else {
            if (stringValue == null) {
                return;
            }

            if (stringValue.length() == 0) {
                value = null;
            } else {
                value = OgnlUtils.convertValue(stringValue, accessor.getType());
            }
        }
        selectionModel.setValue(selectionModelIndex, value);
    }

    public boolean validate() {
        if (mode.isView(insertable, updatable)
                || (mode.isBulk() && !bulkChecked)) {
            return true;
        }

        Object value = selectionModel.getValue(selectionModelIndex);
        if (required && value == null) {
            errors.add(getText("elements.error.field.required"));
            return false;
        }
        return true;
    }

    public void readFromObject(Object obj) {
        super.readFromObject(obj);
        if (obj == null) {
            setValue(null);
        } else {
            setValue(accessor.get(obj));
        }
    }

    public void writeToObject(Object obj) {
        Object value = selectionModel.getValue(selectionModelIndex);
        writeToObject(obj, value);
    }

    public void valueToXhtml(XhtmlBuffer xb) {
        if (mode.isView(insertable, updatable)) {
            valueToXhtmlView(xb);
        } else if (mode.isEdit()) {
            valueToXhtmlEdit(xb);
        } else if (mode.isPreview()) {
            valueToXhtmlPreview(xb);
        } else if (mode.isHidden()) {
            valueToXhtmlHidden(xb);
        } else {
            throw new IllegalStateException("Unknown mode: " + mode.name());
        }
    }

    public void valueToXhtmlEdit(XhtmlBuffer xb) {
        switch (displayMode) {
            case DROPDOWN:
                valueToXhtmlEditDropDown(xb);
                addCreateNewLink(xb);
                if(mode.isBulk()) {
                    xb.writeJavaScript(
                            "$(function() { " +
                                "configureBulkEditField('" + id + "', '" + bulkCheckboxName + "'); " +
                            "});");
                }
                break;
            case RADIO:
                valueToXhtmlEditRadio(xb);
                break;
            case AUTOCOMPLETE:
                valueToXhtmlEditAutocomplete(xb);
                addCreateNewLink(xb);
                break;
            default:
                throw new IllegalStateException(
                        "Unknown display mode: " + displayMode.name());
        }
        /* TODO: precedente versione
        if (selectionModel.getSelectionProvider().isAutocomplete()) {
        } else {
        }
        */
    }

    protected void addCreateNewLink(XhtmlBuffer xb) {
        if (createNewValueHref != null) {
            String href = createNewValueHref;
            if(href.contains("?")) {
                href += "&";
            } else {
                href += "?";
            }
            href += "popupCloseCallback=popupCloseCallback_" + id;
            xb.write(" ");
            xb.openElement("a");
            xb.addAttribute("href", href);
            xb.addAttribute("class", "mde-select-field-create-new-link");
            xb.write(createNewValueText);
            xb.closeElement("a");
            String js = composeCreateNewJs();
            xb.writeJavaScript(js);
        }
    }

    public String composeCreateNewJs() {
        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format(
                "function popupCloseCallback_{0}(val) '{'" +
                        "if(val) updateSelectOptions(''{1}'', {2}, ''jsonSelectFieldOptions''",
                StringEscapeUtils.escapeJavaScript(id),
                StringEscapeUtils.escapeJavaScript(selectionModel.getName()),
                selectionModelIndex));
        appendIds(sb);
        sb.append(");}");
        return sb.toString();
    }

    public void valueToXhtmlEditDropDown(XhtmlBuffer xb) {
        Object value = selectionModel.getValue(selectionModelIndex);
        Map<Object, SelectionModel.Option> options =
                selectionModel.getOptions(selectionModelIndex);

        xb.openElement("select");
        xb.addAttribute("id", id);
        xb.addAttribute("name", inputName);
        xb.addAttribute("class", EDITABLE_FIELD_CSS_CLASS);

        boolean checked = (value == null);
        if (comboLabel != null && !options.isEmpty()) {
            SelectionModel.Option option = options.remove(null);
            if(options.isEmpty()) {
                //Uh-oh, wrong decision
                options.put(null, option);
            } else {
                xb.writeOption("", checked, comboLabel);
            }
        }

        for (Map.Entry<Object,SelectionModel.Option> option :
                options.entrySet()) {
            if(!option.getValue().active) {
                continue;
            }
            Object optionValue = option.getKey();
            String optionStringValue =
                    (String) OgnlUtils.convertValue(optionValue, String.class);
            optionStringValue = StringUtils.defaultString(optionStringValue);
            String optionLabel = option.getValue().label;
            checked =  (optionValue == value) ||
                       (optionValue != null && optionValue.equals(value));
            xb.writeOption(optionStringValue, checked, optionLabel);
        }
        xb.closeElement("select");

        if (nextSelectField != null) {
            String js = composeDropDownJs();
            xb.writeJavaScript(js);
        }
    }

    public String composeDropDownJs() {
        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format(
                "$(''#{0}'').change(" +
                        "function() '{'" +
                        "updateSelectOptions(''{1}'', {2}, ''jsonSelectFieldOptions''",
                StringEscapeUtils.escapeJavaScript(id),
                StringEscapeUtils.escapeJavaScript(selectionModel.getName()),
                selectionModelIndex + 1));
        appendIds(sb);
        sb.append(");});");
        return sb.toString();
    }

    public String composeAutocompleteJs() {
        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format(
                "setupAutocomplete(''#{0}'', ''{1}'', {2}, ''jsonAutocompleteOptions''",
                StringEscapeUtils.escapeJavaScript(autocompleteId),
                StringEscapeUtils.escapeJavaScript(selectionModel.getName()),
                selectionModelIndex));
        appendIds(sb);
        sb.append(");");
        return sb.toString();
    }

    public void appendIds(StringBuilder sb) {
        SelectField rootField = this;
        while (rootField.previousSelectField != null) {
            rootField = rootField.previousSelectField;
        }
        SelectField currentField = rootField;
        while (currentField != null) {
            sb.append(MessageFormat.format(", ''#{0}''",
                    StringEscapeUtils.escapeJavaScript(currentField.getId())));
            currentField = currentField.nextSelectField;
        }
    }

    public void valueToXhtmlEditRadio(XhtmlBuffer xb) {
        Object value = selectionModel.getValue(selectionModelIndex);
        Map<Object, SelectionModel.Option> options =
                selectionModel.getOptions(selectionModelIndex);

        int counter = 0;
        
        if (!required) {
            String radioId = id + "_" + counter;
            boolean checked = (value == null);
            writeRadioWithLabel(xb, radioId,
                    getText("elements.field.select.none"), "", checked);
            counter++;
        }

        for (Map.Entry<Object,SelectionModel.Option> option :
                options.entrySet()) {
            if(!option.getValue().active) {
                continue;
            }
            Object optionValue = option.getKey();
            String optionStringValue =
                    (String) OgnlUtils.convertValue(optionValue, String.class);
            String optionLabel = option.getValue().label;
            String radioId = id + "_" + counter;
            boolean checked =  optionValue.equals(value);
            writeRadioWithLabel(xb, radioId, optionLabel,
                    optionStringValue, checked);
            counter++;
        }
        // TODO: gestire radio in cascata
    }

    protected void writeRadioWithLabel(XhtmlBuffer xb,
                                       String radioId,
                                       String label,
                                       String stringValue,
                                       boolean checked) {
        xb.openElement("div");
        xb.addAttribute("class", "radio");
        xb.writeInputRadio(radioId, inputName, stringValue, checked);
        xb.openElement("label");
        xb.addAttribute("for", radioId);
        xb.write(label);
        xb.closeElement("label");
        xb.closeElement("div");
    }

    public void valueToXhtmlEditAutocomplete(XhtmlBuffer xb) {
        Object value = selectionModel.getValue(selectionModelIndex);
        String stringValue = OgnlUtils.convertValueToString(value);
        xb.writeInputHidden(id, inputName, stringValue);

        xb.openElement("input");
        xb.addAttribute("id", autocompleteId);
        xb.addAttribute("type", "text");
        xb.addAttribute("name", autocompleteInputName);
        xb.addAttribute("autocomplete", "off");
        xb.addAttribute("value", getStringValue());
        xb.addAttribute("class", EDITABLE_FIELD_CSS_CLASS);
        xb.closeElement("input");

        String js = composeAutocompleteJs();

        xb.writeJavaScript(js);
    }

    public void valueToXhtmlPreview(XhtmlBuffer xb) {
        valueToXhtmlView(xb);
        valueToXhtmlHidden(xb);
    }

    public void valueToXhtmlHidden(XhtmlBuffer xb) {
        Object value = selectionModel.getValue(selectionModelIndex);
        String stringValue = OgnlUtils.convertValueToString(value);
        xb.writeInputHidden(inputName, stringValue);
    }

    public void valueToXhtmlView(XhtmlBuffer xb) {
        xb.openElement("p");
        xb.addAttribute("class", STATIC_VALUE_CSS_CLASS);
        xb.addAttribute("id", id);
        if (href != null) {
            xb.openElement("a");
            xb.addAttribute("href", href);
        }
        xb.write(getStringValue());
        if (href != null) {
            xb.closeElement("a");
        }
        xb.closeElement("p");
    }

    public String getStringValue() {
        Object value = selectionModel.getValue(selectionModelIndex);
        String result = selectionModel.getOption(selectionModelIndex, value, true);
        logger.trace("getStringValue() - name: {} - value: {} - result: {}", new Object[] {getPropertyAccessor().getName(), value, result});
        return result;
    }

    @Override
    public void setStringValue(String stringValue) {
        Object value = OgnlUtils.convertValue(stringValue, accessor.getType());
        selectionModel.setValue(selectionModelIndex, value);
    }

    public String jsonSelectFieldOptions(boolean includeSelectPrompt) {
        Map<Object, SelectionModel.Option> options =
                selectionModel.getOptions(selectionModelIndex);
        // prepariamo Json
        JsonBuffer jb = new JsonBuffer();

        // apertura array Json
        jb.openArray();

        if (includeSelectPrompt && !options.isEmpty()) {
            jb.openObject();
            jb.writeKeyValue("v", ""); // value
            jb.writeKeyValue("l", comboLabel); // label
            jb.writeKeyValue("s", true); // selected
            jb.closeObject();
        }

        for (Map.Entry<Object,SelectionModel.Option> option : options.entrySet()) {
            if(!option.getValue().active) {
                continue;
            }
            jb.openObject();
            Object optionValue = option.getKey();
            String optionStringValue = OgnlUtils.convertValueToString(optionValue);
            String optionLabel = option.getValue().label;

            jb.writeKeyValue("v", optionStringValue);
            jb.writeKeyValue("l", optionLabel);
            jb.writeKeyValue("s", false);
            jb.closeObject();
        }

        // chiusura array Json
        jb.closeArray();

        return jb.toString();
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public Object getValue() {
        return selectionModel.getValue(selectionModelIndex);
    }

    public void setValue(Object value) {
        value = OgnlUtils.convertValue(value, accessor.getType());
        selectionModel.setValue(selectionModelIndex, value);
    }

    public Map<Object, SelectionModel.Option> getOptions() {
        return selectionModel.getOptions(selectionModelIndex);
    }

    public String getLabelSearch() {
        return selectionModel.getLabelSearch(selectionModelIndex);
    }

    public String getComboLabel() {
        return comboLabel;
    }

    public void setComboLabel(String comboLabel) {
        this.comboLabel = comboLabel;
    }

    public String getAutocompleteId() {
        return autocompleteId;
    }

    public SelectField getNextSelectField() {
        return nextSelectField;
    }

    public void setNextSelectField(SelectField nextSelectField) {
        this.nextSelectField = nextSelectField;
    }

    public SelectField getPreviousSelectField() {
        return previousSelectField;
    }

    public void setPreviousSelectField(SelectField previousSelectField) {
        this.previousSelectField = previousSelectField;
    }

    public void setAutocompleteId(String autocompleteId) {
        this.autocompleteId = autocompleteId;
    }

    public String getAutocompleteInputName() {
        return autocompleteInputName;
    }

    public void setAutocompleteInputName(String autocompleteInputName) {
        this.autocompleteInputName = autocompleteInputName;
    }

    public void setLabelSearch(String labelSearch) {
        selectionModel.setLabelSearch(selectionModelIndex, labelSearch);
    }

    public SelectionModel getSelectionModel() {
        return selectionModel;
    }

    public void setSelectionModel(SelectionModel selectionModel) {
        this.selectionModel = selectionModel;
    }

    public int getSelectionModelIndex() {
        return selectionModelIndex;
    }

    public void setSelectionModelIndex(int selectionModelIndex) {
        this.selectionModelIndex = selectionModelIndex;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    public String getCreateNewValueHref() {
        return createNewValueHref;
    }

    public void setCreateNewValueHref(String createNewValueHref) {
        this.createNewValueHref = createNewValueHref;
    }

    public String getCreateNewValueText() {
        return createNewValueText;
    }

    public void setCreateNewValueText(String createNewValueText) {
        this.createNewValueText = createNewValueText;
    }
}
