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

package com.manydesigns.elements;

import com.manydesigns.elements.options.OptionProvider;
import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class OptionProviderRunner implements Element {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected final OptionProvider optionProvider;
    protected final SelectField selectFields[];

    public OptionProviderRunner(OptionProvider optionProvider,
                                SelectField[] selectFields) {
        this.optionProvider = optionProvider;
        this.selectFields = selectFields;
    }

    public void readFromRequest(HttpServletRequest req) {
    }

    public boolean validate() {
        return true;
    }

    public void readFromObject(Object obj) {
    }

    public void writeToObject(Object obj) {}

    public void toXhtml(XhtmlBuffer xb) {
        xb.openElement("script");
        if (optionProvider.isAutocomplete()) {
            int index = 0;
            for (SelectField current : selectFields) {
                String js = composeAutocompleteJs(current, index);
                xb.write(js);
                index++;
            }
        } else {

            for (int i = 0; i < selectFields.length - 1; i++) {
                SelectField current = selectFields[i];
                String js = composeDropDownJs(current, i);
                xb.write(js);
            }
        }
        xb.closeElement("script");

    }

    protected String composeDropDownJs(SelectField field, int index) {
        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format(
                "$(''#{0}'').change(" +
                        "function() '{'" +
                        "updateSelectOptions(''{1}'', {2}",
                StringEscapeUtils.escapeJavaScript(field.getId()),
                StringEscapeUtils.escapeJavaScript(optionProvider.getName()),
                index + 1));
        appendIds(sb);
        sb.append(");});");
        return sb.toString();
    }

    protected String composeAutocompleteJs(SelectField field, int index) {
        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format(
                "setupAutocomplete(''{0}'', ''{1}'', {2}",
                StringEscapeUtils.escapeJavaScript(field.getAutocompleteId()),
                StringEscapeUtils.escapeJavaScript(optionProvider.getName()),
                index));
        appendIds(sb);
        sb.append(");");
        return sb.toString();
    }

    private void appendIds(StringBuilder sb) {
        for (SelectField currentField : selectFields) {
            sb.append(MessageFormat.format(", ''#{0}''",
                    StringEscapeUtils.escapeJavaScript(currentField.getId())));
        }
    }

    //**************************************************************************
    // Getters
    //**************************************************************************


    public OptionProvider getOptionProvider() {
        return optionProvider;
    }

    public SelectField[] getSelectFields() {
        return selectFields;
    }
}
