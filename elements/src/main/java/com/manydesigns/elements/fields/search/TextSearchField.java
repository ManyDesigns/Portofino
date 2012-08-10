/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.annotations.MaxLength;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class TextSearchField extends AbstractSearchField {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public final static String MODE_SUFFIX = "_mode";
    public final static String MATCH_MODE_LABEL = "Match mode";

    protected String value;
    protected TextMatchMode matchMode = TextMatchMode.CONTAINS;
    protected String matchModeId;
    protected String matchModeParam;
    protected boolean showMatchMode = true;
    protected Integer maxLength = null;

    //**************************************************************************
    // Costruttori
    //**************************************************************************

    public TextSearchField(PropertyAccessor accessor) {
        this(accessor, null);
    }

    public TextSearchField(PropertyAccessor accessor, String prefix) {
        super(accessor, prefix);

        if (accessor.isAnnotationPresent(MaxLength.class)) {
            maxLength = accessor.getAnnotation(MaxLength.class).value();
        }

        matchModeId = id + MODE_SUFFIX;
        matchModeParam = this.inputName + MODE_SUFFIX;
    }


    //**************************************************************************
    // Element implementation
    //**************************************************************************

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        xb.writeLabel(StringUtils.capitalize(label),
                id, ATTR_NAME_HTML_CLASS);
        if (showMatchMode) {
            xb.writeLabel(MATCH_MODE_LABEL, matchModeId, "match_mode");
            xb.openElement("select");
            xb.addAttribute("id", matchModeId);
            xb.addAttribute("name", matchModeParam);
            xb.addAttribute("class", "matchMode");
            for (TextMatchMode m : TextMatchMode.values()) {
                boolean checked = matchMode == m;
                String option = m.getStringValue();
                xb.writeOption(option, checked, getText(m.getI18nKey()));
            }
            xb.closeElement("select");
        }
        xb.writeInputText(id, inputName, value, "text", 18, maxLength);
    }


    public void readFromRequest(HttpServletRequest req) {
        value = StringUtils.trimToNull(req.getParameter(inputName));
        if (showMatchMode) {
            String matchModeStr = req.getParameter(matchModeParam);
            matchMode = TextMatchMode.CONTAINS; // default
            for (TextMatchMode m : TextMatchMode.values()) {
                if (m.getStringValue().equals(matchModeStr)) {
                    matchMode = m;
                }
            }
        }
    }

    public boolean validate() {
        return true;
    }

    public void toSearchString(StringBuilder sb) {
        if (value != null) {
            appendToSearchString(sb, inputName, value);
        }
        if(matchMode != null && matchMode != TextMatchMode.CONTAINS && showMatchMode) {
            appendToSearchString(sb, matchModeParam, matchMode.getStringValue());
        }
    }

    public void configureCriteria(Criteria criteria) {
        if (value != null) {
            criteria.ilike(accessor, value, matchMode);
        }
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public TextMatchMode getMatchMode() {
        return matchMode;
    }

    public void setMatchMode(TextMatchMode matchMode) {
        this.matchMode = matchMode;
    }

    public boolean isShowMatchMode() {
        return showMatchMode;
    }

    public void setShowMatchMode(boolean showMatchMode) {
        this.showMatchMode = showMatchMode;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMatchModeId() {
        return matchModeId;
    }

    public void setMatchModeId(String matchModeId) {
        this.matchModeId = matchModeId;
    }

    public String getMatchModeParam() {
        return matchModeParam;
    }

    public void setMatchModeParam(String matchModeParam) {
        this.matchModeParam = matchModeParam;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }
}
