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

import com.manydesigns.elements.annotations.Select;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/

public class RadioField extends AbstractField {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected OptionProvider optionProvider;
    protected String stringValue;

    //**************************************************************************
    // Constructors
    //**************************************************************************
    public RadioField(PropertyAccessor accessor) {
        this(accessor, null);
    }

    public RadioField(PropertyAccessor accessor, String prefix) {
        super(accessor, prefix);
        Select annotation =
                accessor.getAnnotation(Select.class);
        if (annotation != null) {
            int i = 0;
            ArrayList<SelectFieldOption> options =
                    new ArrayList<SelectFieldOption>();
            for (String value : annotation.values()) {
                String label = annotation.labels()[i];
                String link;
                try {
                    link = annotation.urls()[i];
                } catch (Exception e) { // link list may be empy
                    link = null;
                }

                DefaultSelectFieldOption option =
                        new DefaultSelectFieldOption(value, label, link);
                options.add(option);

                i++;
            }
            optionProvider = new DefaultOptionProvider(options);
        }
    }

    //**************************************************************************
    // Implementazione di Component
    //**************************************************************************

    public void readFromRequest(HttpServletRequest req) {
        super.readFromRequest(req);

        if (mode.isView(immutable)) {
            return;
        }

        if (optionProvider == null) {
            stringValue = null;
            return;
        }

        Collection<SelectFieldOption> options = optionProvider.getOptions();
        String reqValue = req.getParameter(inputName);
        if (reqValue == null) {
            if (required && options.size() == 1 ) {
                stringValue = options.iterator().next().getValue();
            }
            return;
        }



        stringValue = reqValue;

        boolean found = false;
        for (SelectFieldOption option : options) {
            String optionValue = option.getValue();
            if (optionValue.equals(stringValue)) {
                found = true;
                break;
            }
        }
        if (!found) {
            stringValue = null;
        }
    }

    public boolean validate() {
        if (mode.isView(immutable) || (mode.isBulk() && !bulkChecked)) {
            return true;
        }

        if (required && stringValue == null) {
            errors.add(getText("elements.error.field.required"));
            return false;
        }
        return true;
    }

    public void readFromObject(Object obj) {
        super.readFromObject(obj);
        try {
            if (obj == null) {
                stringValue = null;
            } else {
                stringValue = (String)accessor.get(obj);
            }
        } catch (IllegalAccessException e) {
            throw new Error(e);
        } catch (InvocationTargetException e) {
            throw new Error(e);
        }
    }

    public void writeToObject(Object obj) {
        writeToObject(obj, stringValue);
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

    private void valueToXhtmlEdit(XhtmlBuffer xb) {
        xb.openElement("div");
        xb.addAttribute("id", id);
        int index = 0;

        if (optionProvider != null) {
            for (SelectFieldOption option : optionProvider.getOptions()) {
                // il separatore deve essere "_" e non "." perché il "."
                // può confondere Struts (OGNL)
                String valueId = id + "_" + index;
                String optionValue = option.getValue();
                String optionLabel = option.getLabel();
                boolean checked = optionValue.equals(stringValue);
                xb.openElement("label");
                xb.addAttribute("for", valueId);
                xb.addAttribute("class", "radio");
                xb.writeInputRadio(valueId, inputName, optionValue, checked);
                xb.write(optionLabel);
                xb.closeElement("label");
                index++;
            }
        }
        xb.closeElement("div");
    }

    public void valueToXhtmlPreview(XhtmlBuffer xb) {
        valueToXhtmlView(xb);
        valueToXhtmlHidden(xb);
    }

    private void valueToXhtmlHidden(XhtmlBuffer xb) {
        String localValue = null;
        if (optionProvider != null) {
            SelectFieldOption option = optionProvider.getOption(stringValue);
            if (option != null) {
                localValue = option.getValue();
            }
        }
        xb.writeInputHidden(inputName, localValue);
    }

    public void valueToXhtmlView(XhtmlBuffer xb) {
        xb.openElement("div");
        xb.addAttribute("class", "value");
        xb.addAttribute("id", id);
        SelectFieldOption option = optionProvider.getOption(stringValue);
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

    //**************************************************************************
    // Getter/setter
    //**************************************************************************

    public OptionProvider getOptionProvider() {
        return optionProvider;
    }

    public void setOptionProvider(OptionProvider optionProvider) {
        this.optionProvider = optionProvider;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

}

