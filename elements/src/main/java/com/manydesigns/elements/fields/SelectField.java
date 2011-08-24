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

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.Select;
import com.manydesigns.elements.json.JsonBuffer;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionModel;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Map;

/*
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 */
public class SelectField extends AbstractField {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public final static String AUTOCOMPLETE_SUFFIX = "_autocomplete";
    public enum DisplayMode {
        DROPDOWN,
        RADIO,
        AUTOCOMPLETE
    }

    protected SelectionModel selectionModel;
    protected int selectionModelIndex;
    protected SelectField previousSelectField;
    protected SelectField nextSelectField;

    protected String comboLabel;
    protected DisplayMode displayMode;

    protected String autocompleteId;
    protected String autocompleteInputName;

    //**************************************************************************
    // Costruttori
    //**************************************************************************
    public SelectField(PropertyAccessor accessor, Mode mode, String prefix) {
        super(accessor, mode, prefix);

        Select annotation = accessor.getAnnotation(Select.class);
        if (annotation == null) {
            displayMode = DisplayMode.DROPDOWN;
            if (accessor.getType().isEnum()) {
                SelectionProvider selectionProvider =
                    DefaultSelectionProvider.create(
                            accessor.getName(), accessor.getType());
                selectionModel = selectionProvider.createSelectionModel();
                selectionModelIndex = 0;
            }
        } else {
            Object[] values = annotation.values();
            String[] labels = annotation.labels();
            assert(values.length == labels.length);
            SelectionProvider selectionProvider =
                    DefaultSelectionProvider.create(
                            accessor.getName(), values, labels);
            selectionModel = selectionProvider.createSelectionModel();
            selectionModelIndex = 0;
            displayMode = annotation.displayMode();
        }

        comboLabel = getText("elements.field.select.select", label);
        autocompleteId = id + AUTOCOMPLETE_SUFFIX;
        autocompleteInputName = inputName + AUTOCOMPLETE_SUFFIX;
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
        if (stringValue == null) {
            return;
        }
        
        Object value;
        if (stringValue.length() == 0) {
            value = null;
        } else {
            value = OgnlUtils.convertValue(stringValue, accessor.getType());
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
                break;
            case RADIO:
                valueToXhtmlEditRadio(xb);
                break;
            case AUTOCOMPLETE:
                valueToXhtmlEditAutocomplete(xb);
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

    public void valueToXhtmlEditDropDown(XhtmlBuffer xb) {
        Object value = selectionModel.getValue(selectionModelIndex);
        Map<Object, String> options =
                selectionModel.getOptions(selectionModelIndex);

        xb.openElement("select");
        xb.addAttribute("id", id);
        xb.addAttribute("name", inputName);

        boolean checked = (value == null);
        if (!options.isEmpty()) {
            xb.writeOption("", checked, comboLabel);
        }

        for (Map.Entry<Object,String> option :
                options.entrySet()) {
            Object optionValue = option.getKey();
            String optionStringValue =
                    (String) OgnlUtils.convertValue(optionValue, String.class);
            String optionLabel = option.getValue();
            checked =  optionValue.equals(value);
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
                        "updateSelectOptions(''{1}'', {2}",
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
                "setupAutocomplete(''#{0}'', ''{1}'', {2}",
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
        Map<Object, String> options =
                selectionModel.getOptions(selectionModelIndex);

        xb.openElement("fieldset");
        xb.addAttribute("id", id);
        xb.addAttribute("class", "radio");

        int counter = 0;
        
        if (!required) {
            String radioId = id + "_" + counter;
            boolean checked = (value == null);
            writeRadioWithLabel(xb, radioId,
                    getText("elements.field.select.none"), "", checked);
            counter++;
        }

        for (Map.Entry<Object,String> option :
                options.entrySet()) {
            Object optionValue = option.getKey();
            String optionStringValue =
                    (String) OgnlUtils.convertValue(optionValue, String.class);
            String optionLabel = option.getValue();
            String radioId = id + "_" + counter;
            boolean checked =  optionValue.equals(value);
            writeRadioWithLabel(xb, radioId, optionLabel,
                    optionStringValue, checked);
            counter++;
        }
        xb.closeElement("fieldset");

        // TODO: gestire radio in cascata
    }

    protected void writeRadioWithLabel(XhtmlBuffer xb,
                                       String radioId,
                                       String label,
                                       String stringValue,
                                       boolean checked) {
        xb.writeInputRadio(radioId, inputName, stringValue, checked);
        xb.writeNbsp();
        xb.writeLabel(label, radioId, null);
        xb.writeBr();
    }

    public void valueToXhtmlEditAutocomplete(XhtmlBuffer xb) {
        Object value = selectionModel.getValue(selectionModelIndex);
        String stringValue = OgnlUtils.convertValueToString(value);
        xb.writeInputHidden(id, inputName, stringValue);

        xb.openElement("input");
        xb.addAttribute("id", autocompleteId);
        xb.addAttribute("type", "text");
        xb.addAttribute("name", autocompleteInputName);
        xb.addAttribute("value", getStringValue());
        xb.addAttribute("class", null);
        xb.addAttribute("size", null);
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
        xb.writeInputHidden(id, inputName, stringValue);
    }

    public void valueToXhtmlView(XhtmlBuffer xb) {
        xb.openElement("div");
        xb.addAttribute("class", "value");
        xb.addAttribute("id", id);
        if (href != null) {
            xb.openElement("a");
            xb.addAttribute("href", href);
        }
        xb.write(getStringValue());
        if (href != null) {
            xb.closeElement("a");
        }
        xb.closeElement("div");
    }

    public String getStringValue() {
        Object value = selectionModel.getValue(selectionModelIndex);
        Map<Object, String> options =
                selectionModel.getOptions(selectionModelIndex);
        return options.get(value);
    }

    public String jsonSelectFieldOptions(boolean includeSelectPrompt) {
        Map<Object, String> options =
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

        for (Map.Entry<Object,String> option : options.entrySet()) {
            jb.openObject();
            Object optionValue = option.getKey();
            String optionStringValue = OgnlUtils.convertValueToString(optionValue);
            String optionLabel = option.getValue();

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
        selectionModel.setValue(selectionModelIndex, value);
    }

    public Map<Object, String> getOptions() {
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
}
