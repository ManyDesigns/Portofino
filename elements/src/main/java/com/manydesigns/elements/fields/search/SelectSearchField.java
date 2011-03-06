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
package com.manydesigns.elements.fields.search;

import com.manydesigns.elements.annotations.Select;
import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionModel;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class SelectSearchField extends AbstractSearchField {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    

    protected SelectionModel selectionModel;
    protected int selectionModelIndex;
    protected String comboLabel;
    protected SelectField.DisplayMode displayMode;
    protected String autocompleteId;
    protected String autocompleteInputName;
    protected SelectSearchField previousSelectField;
    protected SelectSearchField nextSelectField;

    public final static String AUTOCOMPLETE_SUFFIX = "_autocomplete";



    public SelectSearchField(PropertyAccessor accessor) {
        this(accessor, null);
    }

    private void initializeModel(PropertyAccessor accessor) {
        Select annotation = accessor.getAnnotation(Select.class);
        if (annotation != null) {
            Object[] values = annotation.values();
            String[] labels = annotation.labels();
            assert(values.length == labels.length);
            SelectionProvider selectionProvider =
                    DefaultSelectionProvider.create(
                            accessor.getName(), values, labels);
            selectionModel = selectionProvider.createSelectionModel();
            displayMode = annotation.displayMode();
        } else {
            displayMode = SelectField.DisplayMode.DROPDOWN;
        }
        selectionModelIndex = 0;
        comboLabel = getText("elements.field.select.select", label);
        autocompleteId = id + AUTOCOMPLETE_SUFFIX;
        autocompleteInputName = inputName + AUTOCOMPLETE_SUFFIX;
    }

    public SelectSearchField(PropertyAccessor accessor, String prefix) {
        super(accessor, prefix);
        initializeModel(accessor);
    }



    public void toSearchString(StringBuilder sb) {
        Object value = selectionModel.getValue(selectionModelIndex);
        if (value != null) {
            String valueString = OgnlUtils.convertValueToString(value);
            appendToSearchString(sb, inputName,
                valueString);
        }
    }

    public void configureCriteria(Criteria criteria) {
        Object value = selectionModel.getValue(selectionModelIndex);
        if (value != null) {
            criteria.eq(accessor, value);
        }
    }

    //**************************************************************************
    // Element implementation
    //**************************************************************************

    public void readFromRequest(HttpServletRequest req) {
        String stringValue = StringUtils.trimToNull(req.getParameter(inputName));
        Object value = OgnlUtils.convertValue(stringValue, accessor.getType());
        selectionModel.setValue(selectionModelIndex, value);
    }

    public boolean validate() {
        return true;
    }

    public void toXhtml(XhtmlBuffer xb) {
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

    }
    private void valueToXhtmlEditDropDown(XhtmlBuffer xb) {
        xb.openElement("fieldset");
        xb.writeLegend(StringUtils.capitalize(label), ATTR_NAME_HTML_CLASS);

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



    public String getStringValue() {
        Object value = selectionModel.getValue(selectionModelIndex);
        Map<Object, String> options =
                selectionModel.getOptions(selectionModelIndex);
        return options.get(value);
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

    public SelectField.DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(SelectField.DisplayMode displayMode) {
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
}
