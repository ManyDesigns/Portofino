/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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
            "Copyright (c) 2005-2013, ManyDesigns srl";

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
        xb.openElement("div");
        xb.addAttribute("class", "control-group");
        xb.writeLabel(StringUtils.capitalize(label), id, ATTR_NAME_HTML_CLASS);
        xb.openElement("div");
        xb.addAttribute("class", "controls");
        if (showMatchMode) {
            xb.writeLabel(MATCH_MODE_LABEL, matchModeId, "match_mode");
            xb.openElement("select");
            xb.addAttribute("id", matchModeId);
            xb.addAttribute("name", matchModeParam);
            xb.addAttribute("class", "match_mode");
            for (TextMatchMode m : TextMatchMode.values()) {
                boolean checked = matchMode == m;
                String option = m.getStringValue();
                xb.writeOption(option, checked, getText(m.getI18nKey()));
            }
            xb.closeElement("select");
            xb.write(" ");
        }
        xb.writeInputText(id, inputName, value, "text", 18, maxLength);
        xb.closeElement("div");
        xb.closeElement("div");
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
