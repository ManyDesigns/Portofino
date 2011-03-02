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

import com.manydesigns.elements.options.SelectionModel;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.annotations.Select;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

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
    protected String value;

    public SelectSearchField(PropertyAccessor accessor) {
        super(accessor);
        initializeModel(accessor);
    }

    private void initializeModel(PropertyAccessor accessor) {
        Select annotation = accessor.getAnnotation(Select.class);
        Object[] values = annotation.values();
        String[] labels = annotation.labels();
        assert(values.length == labels.length);
        SelectionProvider selectionProvider =
                DefaultSelectionProvider.create(
                        accessor.getName(), values, labels);
        selectionModel = selectionProvider.createSelectionModel();
        selectionModelIndex = 0;
        comboLabel = getText("elements.field.select.select", label);
    }

    public SelectSearchField(PropertyAccessor accessor, String prefix) {
        super(accessor, prefix);
        initializeModel(accessor);
    }



    public void toSearchString(StringBuilder sb) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void configureCriteria(Criteria criteria) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    //**************************************************************************
    // Element implementation
    //**************************************************************************

    public void readFromRequest(HttpServletRequest req) {
        value = req.getParameter(inputName);
    }

    public boolean validate() {
        return true;
    }

    public void toXhtml(XhtmlBuffer xb) {
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

    //**************************************************************************
    // Getter/setter
    //**************************************************************************

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
}
