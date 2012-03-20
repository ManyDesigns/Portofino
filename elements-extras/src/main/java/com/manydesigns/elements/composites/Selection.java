/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.elements.composites;

import com.manydesigns.elements.Element;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Selection extends AbstractReflectiveCompositeElement {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected SelectField _selectField;

    //**************************************************************************
    // Costruttori
    //**************************************************************************
    public Selection(PropertyAccessor accessor, Mode mode) {
        this(accessor, mode, null);
    }

    public Selection(PropertyAccessor accessor, Mode mode, String prefix) {
        super(prefix);
        _selectField = new SelectField(accessor, mode, prefix);
    }

    public Selection(ClassAccessor classAccessor, Mode mode,
                     String propertyName, String prefix) {
        super(prefix);
        try {
            PropertyAccessor accessor =
                    classAccessor.getProperty(propertyName);
            _selectField = new SelectField(accessor, mode, prefix);
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    //**************************************************************************
    // Implementazione di Component
    //**************************************************************************
    public void readFromRequest(HttpServletRequest req) {
        _selectField.readFromRequest(req);
        Integer selectedIndex = findIndexOfSelection();
        if (selectedIndex != null) {
            Element component = elements().get(selectedIndex);
            component.readFromRequest(req);
        }
    }

    public boolean validate() {
        if (!_selectField.validate()) {
            return false;
        }

        Integer selectedIndex = findIndexOfSelection();
        if (selectedIndex == null) {
            return true;
        }

        Element component = elements().get(selectedIndex);
        return component.validate();
    }

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        /*
        if (_mode.isEdit() || _mode.isPreview() || _mode.isView()) {
            xb.openElement("fieldset");
            xb.addAttribute("class", "group");
            xb.writeLegend(_selectField.getLabel(), null);
            _selectField.errorsToXhtml(xb);
            xb.openElement("table");
            xb.addAttribute("class", "selection");
            xb.addAttribute("id", _selectField.getId());
            String stringValue = _selectField.getStringValue();
            OptionProvider optionProvider =
                    _selectField.getOptionProvider();
            Iterator<SelectFieldOption> optionsIterator =
                    optionProvider.getOptions().iterator();
            int index = 0;

            for (Element component : elements()) {
                SelectFieldOption currentOption =
                        optionsIterator.next();
                String currentValue = currentOption.getValue();
                String currentLabel = currentOption.getLabel();
                String currentId = _selectField.getId() + "." + index;
                boolean checked = currentValue.equals(stringValue);
                xb.openElement("tr");
                xb.openElement("td");
                xb.addAttribute("class", "radio");
                xb.writeInputRadio(null, _selectField.getInputName(),
                        currentValue, checked, false,
                        "cleanSelect('" + _selectField.getId() + "')");
                xb.write(currentLabel);
                xb.closeElement("td");
                xb.openElement("td");
                xb.addAttribute("id", currentId);
                component.setMode(_mode);
                component.toXhtml(xb);
                xb.closeElement("td");
                xb.closeElement("tr");
                index = index + 1;
            }
            xb.closeElement("table");
            xb.closeElement("fieldset");
            xb.openElement("script");
            xb.addAttribute("type", "text/javascript");
            xb.write("cleanSelect('" + _selectField.getId() + "')");
            xb.closeElement("script");
        } else if (_mode.isHidden()) {
            for (Element component : elements()) {
                component.setMode(_mode);
                component.toXhtml(xb);
            }
            _selectField.setMode(_mode);
            _selectField.toXhtml(xb);
        } else {
            throw new IllegalStateException("Unknown mode:" + _mode);
        }
        */
    }

    public Integer findIndexOfSelection() {
        /*
        OptionProvider optionProvider = _selectField.getOptionProvider();
        int index = 0;
        String stringValue = _selectField.getStringValue();
        for (SelectFieldOption current : optionProvider.getOptions()) {
            if (current.getValue().equals(stringValue)) {
                return index;
            }
            index++;
        }
        */
        return null;
    }
}
