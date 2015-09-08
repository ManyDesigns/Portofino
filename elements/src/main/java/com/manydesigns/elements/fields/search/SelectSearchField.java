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

package com.manydesigns.elements.fields.search;

import com.manydesigns.elements.annotations.Select;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SearchDisplayMode;
import com.manydesigns.elements.options.SelectionModel;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Map;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class SelectSearchField extends AbstractSearchField {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    protected SelectionModel selectionModel;
    protected int selectionModelIndex;
    protected boolean notSet;
    protected String comboLabel;
    protected String notSetLabel;
    protected SearchDisplayMode displayMode;
    protected String autocompleteId;
    protected String autocompleteInputName;
    protected SelectSearchField previousSelectField;
    protected SelectSearchField nextSelectField;

    public final static String AUTOCOMPLETE_SUFFIX = "_autocomplete";
    public final static String VALUE_NOT_SET = "__notset__";

    public SelectSearchField(PropertyAccessor accessor, String prefix) {
        this(accessor, null, prefix);
    }

    private void initializeModel(PropertyAccessor accessor, SelectionProvider selectionProvider) {
        Select annotation = accessor.getAnnotation(Select.class);
        if(selectionProvider == null) {
            if (annotation != null) {
                Object[] values = annotation.values();
                String[] labels = annotation.labels();
                assert(values.length == labels.length);
                DefaultSelectionProvider sp = new DefaultSelectionProvider(accessor.getName(), 1);
                for(int i = 0; i < values.length; i++) {
                    sp.appendRow(values[i], labels[i], true);
                }
                selectionModel = sp.createSelectionModel();
                displayMode = annotation.searchDisplayMode();
            }
        } else {
            displayMode = selectionProvider.getSearchDisplayMode();
            if(displayMode == null && annotation != null) {
                displayMode = annotation.searchDisplayMode();
            }
        }
        if(displayMode == null) {
            displayMode = SearchDisplayMode.DROPDOWN;
        }
        selectionModelIndex = 0;
        comboLabel = getText("elements.field.select.select", label );
        notSetLabel = getText("elements.search.select.notset", label );
        autocompleteId = id + AUTOCOMPLETE_SUFFIX;
        autocompleteInputName = inputName + AUTOCOMPLETE_SUFFIX;
    }

    public SelectSearchField(PropertyAccessor accessor, SelectionProvider selectionProvider, String prefix) {
        super(accessor, prefix);
        initializeModel(accessor, selectionProvider);
    }

    public void toSearchString(StringBuilder sb, String encoding) {
        if(!required && notSet) {
            appendToSearchString(sb, inputName, VALUE_NOT_SET, encoding);
            return;
        }
        Object[] values = getValues();
        if (null==values){
            return;
        } else {
            for (Object value : values){
                String valueString = OgnlUtils.convertValueToString(value);
                appendToSearchString(sb, inputName, valueString, encoding);
            }
        }
    }

    public void configureCriteria(Criteria criteria) {
        if(!required && notSet) {
            criteria.isNull(accessor);
            return;
        }
        Object[] values = getValues();
        if (values == null) {
            logger.debug("Null values array. Not adding 'in' criteria.");
        } else if (values.length == 0) {
            logger.debug("Enpty values array. Not adding 'in' criteria.");
        } else {
            if(logger.isDebugEnabled()) {
                logger.debug("Adding 'in' criteria for values: {}", ArrayUtils.toString(values));
            }
            criteria.in(accessor, values);
        }
    }

    //**************************************************************************
    // Element implementation
    //**************************************************************************

    public void readFromRequest(HttpServletRequest req) {
        Object[] values = req.getParameterValues(inputName);
        if (values == null){
            return;
        } else if(values.length == 1 && VALUE_NOT_SET.equals(values[0])) {
            notSet = true;
        } else if(SearchDisplayMode.AUTOCOMPLETE == displayMode) {
            String stringValue = values[0].toString();
            boolean search;
            Object value;
            String userValue = req.getParameter(autocompleteInputName);
            if(StringUtils.isEmpty(stringValue)) {
                search = true;
                value = null;
            } else {
                value = OgnlUtils.convertValue(stringValue, accessor.getType());
                String label = selectionModel.getOption(selectionModelIndex, value, true);
                search = userValue != null && !StringUtils.equals(userValue, label);
            }
            if (search) {
                value = null;
                //Attempt to find the value among the options
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
            }
            selectionModel.setValue(selectionModelIndex, value);
        } else {
            Object[] castedValues = new Object[values.length];
            for (int i=0;i<values.length;i++){
                if(!StringUtils.isEmpty((String) values[i])) {
                    castedValues[i] =
                            OgnlUtils.convertValueQuietly(values[i], accessor.getType());
                }
            }
            selectionModel.setValue(selectionModelIndex, castedValues);
        }
    }

    public boolean validate() {
        return true;
    }

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        xb.openElement("div");
        xb.addAttribute("class", "form-group");
        switch (displayMode) {
            case DROPDOWN:
                valueToXhtmlDropDown(xb);
                break;
            case RADIO:
                valueToXhtmlRadio(xb);
                break;
            case AUTOCOMPLETE:
                valueToXhtmlAutocomplete(xb);
                break;
            case CHECKBOX:
                valueToXhtmlCheckbox(xb);
                break;
            case MULTIPLESELECT:
                valueToXhtmlMultipleSelection(xb);
                break;
            default:
                throw new IllegalStateException(
                        "Unknown display mode: " + displayMode.name());
        }
        xb.closeElement("div");
    }

    private void valueToXhtmlDropDown(XhtmlBuffer xb) {
        xb.writeLabel(StringUtils.capitalize(label), id, ATTR_NAME_HTML_CLASS);

        Object[] values = getValues();
        Map<Object, SelectionModel.Option> options =
                selectionModel.getOptions(selectionModelIndex);
        xb.openElement("select");
        xb.addAttribute("id", id);
        xb.addAttribute("name", inputName);
        xb.addAttribute("class", FORM_CONTROL_CSS_CLASS);

        boolean selected = (values == null && !notSet);
        if (!options.isEmpty()) {
            xb.writeOption("", selected, comboLabel);
            if(!required) {
                xb.writeOption(VALUE_NOT_SET, notSet, notSetLabel);
            }
        }

        for (Map.Entry<Object,SelectionModel.Option> option :
                options.entrySet()) {
            //#1318 include inactive options, because they must be searchable.
            Object optionValue = option.getKey();
            String optionStringValue =
                    OgnlUtils.convertValueToString(optionValue);
            String optionLabel = option.getValue().label;
            selected = ArrayUtils.contains(values, optionValue);
            xb.writeOption(optionStringValue, selected, optionLabel);
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
                        "updateSelectOptions(''{1}'', {2}, ''jsonSelectFieldSearchOptions''",
                StringEscapeUtils.escapeJavaScript(id),
                StringEscapeUtils.escapeJavaScript(selectionModel.getName()),
                selectionModelIndex + 1));
        appendIds(sb);
        sb.append(");});");
        return sb.toString();
    }

    public void valueToXhtmlRadio(XhtmlBuffer xb) {
        Object[] values = getValues();
        Map<Object, SelectionModel.Option> options =
                selectionModel.getOptions(selectionModelIndex);


        xb.writeLabel(StringUtils.capitalize(label), id, ATTR_NAME_HTML_CLASS);

        xb.openElement("div");
        xb.addAttribute("class", FORM_CONTROL_CSS_CLASS + " radio");

        int counter = 0;

        if (!required) {
            String radioId = id + "_" + counter;
            boolean checked = (values == null && !notSet);
            writeRadioWithLabel(xb, radioId,
                    getText("elements.search.select.none"), "", checked);
            counter++;
            radioId = id + "_" + counter;
            writeRadioWithLabel(xb, radioId,
                    getText("elements.search.select.notset.radio"), VALUE_NOT_SET, notSet);
            counter++;
        }

        for (Map.Entry<Object,SelectionModel.Option> option :
                options.entrySet()) {
            if(!option.getValue().active) {
                continue;
            }
            Object optionValue = option.getKey();
            String optionStringValue =
                    OgnlUtils.convertValueToString(optionValue);
            String optionLabel = option.getValue().label;
            String radioId = id + "_" + counter;
            boolean checked =  ArrayUtils.contains(values, optionValue);
            writeRadioWithLabel(xb, radioId, optionLabel, optionStringValue, checked);
            counter++;
        }
        xb.closeElement("div");

        // TODO: gestire radio in cascata
    }

    protected void writeRadioWithLabel(XhtmlBuffer xb,
                                       String radioId,
                                       String label,
                                       String stringValue,
                                       boolean checked) {

        //xb.writeNbsp();
        xb.writeInputRadio(radioId, inputName, stringValue, checked);
        //xb.writeNbsp();

        xb.openElement("label");
        xb.addAttribute("class", "radio");
        xb.addAttribute("for", radioId);
       // xb.writeNbsp();
        xb.write(label);
        //xb.writeNbsp();
        xb.closeElement("label");
    }

    public void valueToXhtmlAutocomplete(XhtmlBuffer xb) {
        xb.writeLabel(StringUtils.capitalize(label), id, ATTR_NAME_HTML_CLASS);

        Object value = selectionModel.getValue(selectionModelIndex);
        String stringValue = OgnlUtils.convertValueToString(value);
        xb.writeInputHidden(id, inputName, stringValue);
        String label = selectionModel.getOption(selectionModelIndex, value, true);

        xb.openElement("input");
        xb.addAttribute("id", autocompleteId);
        xb.addAttribute("type", "text");
        xb.addAttribute("name", autocompleteInputName);
        xb.addAttribute("value", label);
        xb.addAttribute("class", FORM_CONTROL_CSS_CLASS);
        xb.addAttribute("size", null);
        xb.closeElement("input");

        String js = composeAutocompleteJs();
        xb.writeJavaScript(js);
    }

    private void valueToXhtmlCheckbox(XhtmlBuffer xb) {
        xb.writeLabel(StringUtils.capitalize(label), id, ATTR_NAME_HTML_CLASS);

        Object[] values = getValues();
        Map<Object, SelectionModel.Option> options =
                selectionModel.getOptions(selectionModelIndex);
        int counter=0;
        for (Map.Entry<Object,SelectionModel.Option> option :
                options.entrySet()) {
            if(!option.getValue().active) {
                continue;
            }
            xb.openElement("div");
            xb.addAttribute("class", FORM_CONTROL_CSS_CLASS + " checkbox");
            Object optionValue = option.getKey();
            String optionStringValue =
                    OgnlUtils.convertValueToString(optionValue);
            String optionLabel = option.getValue().label;
            boolean checked =  ArrayUtils.contains(values, optionValue);
            String checkboxId = id + "_" + counter;

            xb.writeInputCheckbox(checkboxId, inputName, optionStringValue, checked);
            xb.openElement("label");
            xb.addAttribute("for", checkboxId);
            xb.write(optionLabel);
            xb.closeElement("label");

            xb.closeElement("div");
            counter++;
        }
    }

    private void valueToXhtmlMultipleSelection(XhtmlBuffer xb) {
        xb.writeLabel(StringUtils.capitalize(label), id, ATTR_NAME_HTML_CLASS);
        xb.openElement("div");
        xb.addAttribute("class", FORM_CONTROL_CSS_CLASS + " multiple-select");

        Object[] values = getValues();
        Map<Object, SelectionModel.Option> options =
                selectionModel.getOptions(selectionModelIndex);
        xb.openElement("select");
        xb.addAttribute("id", id);
        xb.addAttribute("name", inputName);
        xb.addAttribute("multiple", "multiple");
        xb.addAttribute("size", "5");

        boolean checked;

        for (Map.Entry<Object,SelectionModel.Option> option :
                options.entrySet()) {
            if(!option.getValue().active) {
                continue;
            }
            Object optionValue = option.getKey();
            String optionStringValue =
                    OgnlUtils.convertValueToString(optionValue);
            String optionLabel = option.getValue().label;
            checked =  ArrayUtils.contains(values, optionValue);
            xb.writeOption(optionStringValue, checked, optionLabel);
        }
        xb.closeElement("select");
        xb.closeElement("div");
    }

    public String composeAutocompleteJs() {
        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format(
                "setupAutocomplete(''#{0}'', ''{1}'', {2}, ''jsonAutocompleteSearchOptions''",
                StringEscapeUtils.escapeJavaScript(autocompleteId),
                StringEscapeUtils.escapeJavaScript(selectionModel.getName()),
                selectionModelIndex));
        appendIds(sb);
        sb.append(");");
        return sb.toString();
    }

    public void appendIds(StringBuilder sb) {
        SelectSearchField rootField = this;
        while (rootField.previousSelectField != null) {
            rootField = rootField.previousSelectField;
        }
        SelectSearchField currentField = rootField;
        while (currentField != null) {
            sb.append(MessageFormat.format(", ''#{0}''",
                    StringEscapeUtils.escapeJavaScript(currentField.getId())));
            currentField = currentField.nextSelectField;
        }
    }

    //**************************************************************************
    // Getter/setter
    //**************************************************************************
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

    public String getComboLabel() {
        return comboLabel;
    }

    public void setComboLabel(String comboLabel) {
        this.comboLabel = comboLabel;
    }

    public String getNotSetLabel() {
        return notSetLabel;
    }

    public void setNotSetLabel(String notSetLabel) {
        this.notSetLabel = notSetLabel;
    }

    public SearchDisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(SearchDisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    public String getAutocompleteId() {
        return autocompleteId;
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

    public SelectSearchField getPreviousSelectField() {
        return previousSelectField;
    }

    public void setPreviousSelectField(SelectSearchField previousSelectField) {
        this.previousSelectField = previousSelectField;
    }

    public SelectSearchField getNextSelectField() {
        return nextSelectField;
    }

    public void setNextSelectField(SelectSearchField nextSelectField) {
        this.nextSelectField = nextSelectField;
    }

    public Object[] getValues() {
        Object object = selectionModel.getValue(selectionModelIndex);
        if(object instanceof Object[]) {
            return (Object[]) object;
        } else if(object != null) {
            return new Object[] { object };
        } else {
            return null;
        }
    }

    public void setValue(Object[] values) {
        selectionModel.setValue(selectionModelIndex, values);
    }

    public Map<Object, SelectionModel.Option> getOptions() {
        return selectionModel.getOptions(selectionModelIndex);
    }

    public String getLabelSearch() {
        return selectionModel.getLabelSearch(selectionModelIndex);
    }
}
