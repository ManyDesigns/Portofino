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

package com.manydesigns.portofino.buttons;

import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.StringUtils;
import org.apache.taglibs.standard.tag.common.fmt.BundleSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.List;
import java.util.MissingResourceException;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ButtonsTag extends TagSupport {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    private String list;
    private Object bean;
    private String cssClass;

    private static final Logger logger = LoggerFactory.getLogger(ButtonsTag.class);

    @Override
    public int doStartTag() throws JspException {
        LocalizationContext localizationContext;
        String prefix = "";
        Tag t = findAncestorWithClass(this, BundleSupport.class);
        if (t != null) {
            // use resource bundle from parent <bundle> tag
            BundleSupport parent = (BundleSupport) t;
            localizationContext = parent.getLocalizationContext();
            prefix = parent.getPrefix();
        } else {
            localizationContext = BundleSupport.getLocalizationContext(pageContext);
        }

        List<ButtonInfo> buttons = Logic.getButtonsForClass(bean.getClass()).get(list);
        if(buttons != null) {
            for(ButtonInfo button : buttons) {
                XhtmlBuffer buffer = new XhtmlBuffer(this.pageContext.getOut());
                buffer.openElement("button");
                buffer.addAttribute("name", button.getName());
                String value = getValue(button, localizationContext, prefix);
                if(cssClass != null) {
                    buffer.addAttribute("class", cssClass);
                }
                buffer.addAttribute("type", "submit");
                buffer.write(value);
                buffer.closeElement("button");
            }
        }
        return SKIP_BODY;
    }

    protected String getValue(ButtonInfo button, LocalizationContext localizationContext, String prefix) {
        String key = button.getButton().key();
        if(!StringUtils.isEmpty(key)) {
            try {
                String value = localizationContext.getResourceBundle().getString(prefix + key);
                return value;
            } catch (MissingResourceException e) {
                logger.warn("Resource for button " + button.getName() +
                            " in list " + button.getButton().list() +
                            " not found", e);
            }
        }
        return button.getName();
    }

    @Override
    public void release() {
        super.release();
        list = null;
        bean = null;
    }

    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }
}
