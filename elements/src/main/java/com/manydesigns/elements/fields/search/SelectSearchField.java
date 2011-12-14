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
package com.manydesigns.elements.fields.search;

import com.manydesigns.elements.annotations.Select;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionModel;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class SelectSearchField extends AbstractSearchField {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public enum DisplayMode {
        DROPDOWN,
        RADIO,
        AUTOCOMPLETE,
        MULTIPLESELECT,
        CHECKBOX
    }

    protected SelectionModel selectionModel;
    protected int selectionModelIndex;
    protected String comboLabel;
    protected DisplayMode displayMode;
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
            DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider(accessor.getName(), 1);
            for(int i = 0; i < values.length; i++) {
                selectionProvider.appendRow(values[i], labels[i], true);
            }
            selectionModel = selectionProvider.createSelectionModel();
            displayMode = annotation.searchDisplayMode();
        } else {
            displayMode = DisplayMode.DROPDOWN;
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
        Object[] values = (Object[]) selectionModel.getValue(selectionModelIndex);
        if (null==values){
            return;
        } else {
            for (Object value : values){
                String valueString = OgnlUtils.convertValueToString(value);
                appendToSearchString(sb, inputName,
                    valueString);
            }
        }
    }

    public void configureCriteria(Criteria criteria) {
       Object[] values = (Object[]) selectionModel.getValue(selectionModelIndex);
       if (values == null) {
           logger.debug("Null values array. Not adding 'in' criteria.");
       } else if (values.length == 0) {
           logger.debug("Enpty values array. Not adding 'in' criteria.");
       } else {
           logger.debug("Adding 'in' criteria for values: {}",
                   ArrayUtils.toString(values));
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
        } else {
            Object[] castedValues = new Object[values.length];
            for (int i=0;i<values.length;i++){
                if(!StringUtils.isEmpty((String) values[i])) {
                    castedValues[i] =
                            OgnlUtils.convertValue((String) values[i], accessor.getType());
                }
            }
            selectionModel.setValue(selectionModelIndex, castedValues);
        }
    }

    public boolean validate() {
        return true;
    }

    public void toXhtml(@NotNull XhtmlBuffer xb) {
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
    }

    private void valueToXhtmlDropDown(XhtmlBuffer xb) {
        xb.openElement("fieldset");
        xb.writeLegend(StringUtils.capitalize(label), ATTR_NAME_HTML_CLASS);

        Object[] values = (Object[]) selectionModel.getValue(selectionModelIndex);
        Map<Object, String> options =
                selectionModel.getOptions(selectionModelIndex);
        xb.openElement("select");
        xb.addAttribute("id", id);
        xb.addAttribute("name", inputName);

        boolean checked = (values == null);
        if (!options.isEmpty()) {
            xb.writeOption("", checked, comboLabel);
        }

        for (Map.Entry<Object,String> option :
                options.entrySet()) {
            Object optionValue = option.getKey();
            String optionStringValue =
                    OgnlUtils.convertValueToString(optionValue);
            String optionLabel = option.getValue();
            checked = ArrayUtils.contains(values, optionValue);
            xb.writeOption(optionStringValue, checked, optionLabel);
        }
        xb.closeElement("select");
        xb.closeElement("fieldset");
    }

    public void valueToXhtmlRadio(XhtmlBuffer xb) {
        Object[] values = (Object[]) selectionModel.getValue(selectionModelIndex);
        Map<Object, String> options =
                selectionModel.getOptions(selectionModelIndex);

        xb.openElement("fieldset");
        xb.addAttribute("id", id);
        xb.addAttribute("class", "radio");
        xb.writeLegend(StringUtils.capitalize(label), ATTR_NAME_HTML_CLASS);
        
        int counter = 0;

        if (!required) {
            String radioId = id + "_" + counter;
            boolean checked = (values == null);
            writeRadioWithLabel(xb, radioId,
                    getText("elements.field.select.none"), "", checked);
            counter++;
        }

        for (Map.Entry<Object,String> option :
                options.entrySet()) {
            Object optionValue = option.getKey();
            String optionStringValue =
                    OgnlUtils.convertValueToString(optionValue);
            String optionLabel = option.getValue();
            String radioId = id + "_" + counter;
            boolean checked =  ArrayUtils.contains(values, optionValue);
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

    public void valueToXhtmlAutocomplete(XhtmlBuffer xb) {
        Object[] values = (Object[]) selectionModel.getValue(selectionModelIndex);
        if(values!=null){
            xb.writeInputHidden(id, inputName,
                    OgnlUtils.convertValueToString(values[0]));
        } else {
            xb.writeInputHidden(id, inputName, null);
        }
        xb.openElement("fieldset");
        xb.writeLegend(StringUtils.capitalize(label), ATTR_NAME_HTML_CLASS);

        xb.openElement("input");
        xb.addAttribute("id", autocompleteId);
        xb.addAttribute("type", "text");
        xb.addAttribute("name", autocompleteInputName);
        if(values != null){
            xb.addAttribute("value", OgnlUtils.convertValueToString(values[0]));
        }
        xb.addAttribute("class", null);
        xb.addAttribute("size", null);
        xb.closeElement("input");

        String js = composeAutocompleteJs();
        xb.writeJavaScript(js);
        xb.closeElement("fieldset");
    }

    private void valueToXhtmlCheckbox(XhtmlBuffer xb) {
        xb.openElement("fieldset");
        xb.writeLegend(StringUtils.capitalize(label), ATTR_NAME_HTML_CLASS);

        Object[] values = (Object[]) selectionModel.getValue(selectionModelIndex);
        Map<Object, String> options =
                selectionModel.getOptions(selectionModelIndex);
        int counter=0;
        for (Map.Entry<Object,String> option :
                options.entrySet()) {
            Object optionValue = option.getKey();
            String optionStringValue =
                    OgnlUtils.convertValueToString(optionValue);
            String optionLabel = option.getValue();
            boolean checked =  ArrayUtils.contains(values, optionValue);
            xb.writeInputCheckbox(id + "_" + counter,inputName, optionStringValue, checked);
            xb.writeNbsp();
            xb.writeLabel(optionLabel, id + "_" + counter, null);
            xb.writeBr();
            counter++;
        }
        xb.closeElement("fieldset");
    }

    private void valueToXhtmlMultipleSelection(XhtmlBuffer xb) {
        xb.openElement("fieldset");
        xb.writeLegend(StringUtils.capitalize(label), ATTR_NAME_HTML_CLASS);

        Object[] values = (Object[]) selectionModel.getValue(selectionModelIndex);
        Map<Object, String> options =
                selectionModel.getOptions(selectionModelIndex);
        xb.openElement("select");
        xb.addAttribute("id", id);
        xb.addAttribute("name", inputName);
        xb.addAttribute("multiple", "multiple");
        xb.addAttribute("size", "5");

        boolean checked;

        for (Map.Entry<Object,String> option :
                options.entrySet()) {
            Object optionValue = option.getKey();
            String optionStringValue =
                    OgnlUtils.convertValueToString(optionValue);
            String optionLabel = option.getValue();
            checked =  ArrayUtils.contains(values, optionValue);
            xb.writeOption(optionStringValue, checked, optionLabel);
        }
        xb.closeElement("select");
        xb.closeElement("fieldset");
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

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
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
        return (Object[]) selectionModel.getValue(selectionModelIndex);
    }

    public void setValue(Object[] values) {
        selectionModel.setValue(selectionModelIndex, values);
    }

    public Map<Object, String> getOptions() {
        return selectionModel.getOptions(selectionModelIndex);
    }

    public String getLabelSearch() {
        return selectionModel.getLabelSearch(selectionModelIndex);
    }
}
