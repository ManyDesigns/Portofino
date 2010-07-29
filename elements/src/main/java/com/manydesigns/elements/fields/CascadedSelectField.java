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

import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class CascadedSelectField extends AbstractField {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public static final String REL_NAME_HTML_CLASS = "rel_name";

    protected final SelectField[] selectFieldArray;
    protected final String[] fieldLabels;
    protected final String[] ajaxUrls;
    protected final PropertyAccessor[] accessors;
    protected CascadedOptionProvider cascadedOptionProvider;

    public CascadedSelectField(PropertyAccessor... accessors) {
        this(null, accessors);
    }
    
    public CascadedSelectField(String prefix, PropertyAccessor... accessors) {
        super(accessors[accessors.length-1], prefix);
        this.accessors = accessors;
        fieldLabels = new String[accessors.length];

        // inizializzato a length-1 perché l'ultima combo non ha ajax.
        ajaxUrls = new String[accessors.length-1];

        // il separatore deve essere "_" e non "." perché il "."
        // può confondere Struts (OGNL)
        String[] args = {prefix, id, "_"};
        String localPrefix = StringUtils.join(args);

        // cura i figli
        int length = accessors.length;
        selectFieldArray = new SelectField[length];
        for (int i = 0; i < length; i++) {
            selectFieldArray[i] = new SelectField(accessors[i], localPrefix);
        }
    }

    public void valueToXhtml(XhtmlBuffer xb) {
        if (mode.isView(immutable)) {
            valueToXhtmlView(xb);
        } else if (mode.isEdit()) {
            valueToXhtmlEdit(xb);
        } else if (mode.isPreview()) {
            valueToXhtmlPreview(xb);
        } else if (mode.isHidden()) {
            valueToXhtmlHidden(xb);
        } else {
            throw new IllegalStateException("Unknown mode: " + mode);
        }
    }

    public void valueToXhtmlHidden(XhtmlBuffer xb) {
        SelectFieldOption option = getLastSelectFieldOption();
        String localValue = null;
        if (option != null) {
            localValue = option.getValue();
        }
        xb.writeInputHidden(inputName, localValue);
    }

    public void valueToXhtmlView(XhtmlBuffer xb) {
        SelectFieldOption option = getLastSelectFieldOption();
        xb.openElement("div");
        xb.addAttribute("class", "value");
        xb.addAttribute("id", id);
        if (option != null) {
            String optionLabel = option.getLabel();
            String optionUrl = option.getUrl();

            if (optionUrl != null) {
                xb.writeAnchor(optionUrl, optionLabel);
            } else {
                xb.write(optionLabel);
            }
        }
        xb.closeElement("div");
    }

    public SelectFieldOption getLastSelectFieldOption() {
        OptionProvider optionProvider =
                new CascadedOptionToOptionProviderAdapter(
                        cascadedOptionProvider, selectFieldArray.length-1);
        String lastFieldStringValue = getLastSelectField().getStringValue();
        return optionProvider.getOption(lastFieldStringValue);
    }

    public void valueToXhtmlPreview(XhtmlBuffer xb) {
        valueToXhtmlView(xb);
        valueToXhtmlHidden(xb);
    }

    public void valueToXhtmlEdit(XhtmlBuffer xb) {
        xb.openElement("div");
        xb.addAttribute("id", id);
        Collection<SelectFieldOption> filteredOptions =
                cascadedOptionProvider.getOptions(0);
        int level = 0;
        for (SelectField current : selectFieldArray) {
            String currentFieldLabel = fieldLabels[level];

            if (currentFieldLabel != null) {
                xb.writeLabel(currentFieldLabel,
                        current.getId(), REL_NAME_HTML_CLASS);
            }

            DefaultOptionProvider optionProvider =
                    new DefaultOptionProvider(filteredOptions);
            current.setOptionProvider(optionProvider);
            //current.valueToXhtml(xb);
            xb.openElement("select");
            xb.addAttribute("id", current.getId());
            xb.addAttribute("name", current.getInputName());

            //Javascript
            if (level!=selectFieldArray.length-1) {
                SelectField next = selectFieldArray[level+1];

                xb.addAttribute("onChange",
                    "chainedSelect(this,'" +
                            StringEscapeUtils.escapeJavaScript(next.getInputName())
                            + "','" +
                            StringEscapeUtils.escapeJavaScript(ajaxUrls[level])
                            +"',true," +
                            "'Unable to recover data')");
            }

            String stringValue = current.getStringValue();
            boolean checked = (stringValue == null || stringValue.length() == 0);
            xb.writeOption("", checked, current.getComboLabel());

            if (current.getOptionProvider() != null) {
                for (SelectFieldOption option : current.getOptionProvider().getOptions()) {
                    String optionValue = option.getValue();
                    String optionLabel = option.getLabel();
                    checked = optionValue.equals(stringValue);
                    xb.writeOption(optionValue, checked, optionLabel);
                }
            }
            xb.closeElement("select");

            String currentValue = current.getStringValue();
            level++;
            filteredOptions =
                    cascadedOptionProvider.getOptions(level, currentValue);
        }
        xb.closeElement("div");
    }

    public SelectField getLastSelectField() {
        return selectFieldArray[selectFieldArray.length-1];
    }

    public void readFromRequest(HttpServletRequest req) {
        super.readFromRequest(req);

        if (mode.isView(immutable)) {
            return;
        }

        String stringValue = req.getParameter(inputName);

        if (cascadedOptionProvider == null) {
            return;
        }

        if (stringValue == null) {
            readFromRequestParentNotProvided(req);
        } else {
            readFromRequestParentProvided(stringValue);
        }

    }

    protected void readFromRequestParentNotProvided(HttpServletRequest req) {
        Collection<SelectFieldOption> filteredOptions =
                cascadedOptionProvider.getOptions(0);
        int level = 0;
        for (SelectField current : selectFieldArray) {
            DefaultOptionProvider optionProvider =
                    new DefaultOptionProvider(filteredOptions);

            current.setOptionProvider(optionProvider);
            current.readFromRequest(req);

            String currentValue = current.getStringValue();
            level++;
            filteredOptions =
                    cascadedOptionProvider.getOptions(level, currentValue);
        }
    }

    protected void readFromRequestParentProvided(String stringValue) {
        SelectFieldOption foundOption = null;
        for (SelectFieldOption option :
                cascadedOptionProvider.getOptions(selectFieldArray.length-1)) {
            String optionValue = option.getValue();
            if (optionValue.equals(stringValue)) {
                foundOption = option;
                break;
            }
        }

        for (int level = selectFieldArray.length-1; level > 0; level--) {
            SelectField current = selectFieldArray[level];
            if (foundOption == null) {
                current.setStringValue(null);
            } else {
                String optionValue = foundOption.getValue();
                current.setStringValue(optionValue);

                // prepariamo foundOption per l'iterazione successiva
                foundOption = cascadedOptionProvider.getParent(
                        level, optionValue);
            }
        }
        // chiudiamo il livello 0, senza chiamare getParent()
        SelectField current = selectFieldArray[0];
        if (foundOption == null) {
            current.setStringValue(null);
        } else {
            current.setStringValue(foundOption.getValue());
        }
    }

    public boolean validate() {
        if (mode.isView(immutable) || (mode.isBulk() && !bulkChecked)) {
            return true;
        }

        boolean result = getLastSelectField().validate();
        errors.addAll(getLastSelectField().getErrors());
        return result;
    }

    public void readFromObject(Object obj) {
        super.readFromObject(obj);
        SelectField current = getLastSelectField();
        current.readFromObject(obj);
        String currentValue = current.getStringValue();
        for (int level = selectFieldArray.length-1; level > 0; level--) {
            current = selectFieldArray[level];
            current.setStringValue(currentValue);

            // prepare for next round
            SelectFieldOption parent = cascadedOptionProvider
                    .getParent(level, currentValue);
            if (parent == null) {
                currentValue = null;
            } else {
                currentValue = parent.getValue();
            }
        }
        current = selectFieldArray[0];
        current.setStringValue(currentValue);
    }

    public void writeToObject(Object obj) {
        if (mode.isView(immutable)) {
            return;
        }

        for (SelectField current : selectFieldArray) {
            current.writeToObject(obj);
        }
    }

    public int getLength() {
        return selectFieldArray.length;
    }

    public String getComboLabel(int index) {
        return selectFieldArray[index].getComboLabel();
    }

    public void setComboLabel(int index, String comboLabel) {
        selectFieldArray[index].setComboLabel(comboLabel);
    }

    public String getFieldLabel(int index) {
        return fieldLabels[index];
    }

    public void setFieldLabel(int index, String fieldLabel) {
        fieldLabels[index] = fieldLabel;
    }

    public String getAjaxUrl(int index) {
        return ajaxUrls[index];
    }

    public void setAjaxUrl(int index, String ajaxUrl) {
        ajaxUrls[index] = ajaxUrl;
    }

    public CascadedOptionProvider getOptionProvider() {
        return cascadedOptionProvider;
    }

    public void setOptionProvider(CascadedOptionProvider cascadedOptionProvider) {
        this.cascadedOptionProvider = cascadedOptionProvider;
    }

    public String getStringValue(int index) {
        return selectFieldArray[index].getStringValue();
    }

    public void setStringValue(int index, String value) {
        selectFieldArray[index].setStringValue(value);
    }

    @Override
    public void setRequired(boolean required) {
        super.setRequired(required);
        for (SelectField current : selectFieldArray) {
            current.setRequired(required);
        }
    }
}



