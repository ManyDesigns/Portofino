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

package com.manydesigns.elements.servlet;

import com.manydesigns.elements.Element;
import com.manydesigns.elements.xml.XhtmlBuffer;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspWriter;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class WriteTag extends TagSupport {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";
    protected String value;
    protected String scope;

    public void setValue(String value) {
        this.value = value;
    }

    public void setScope(String scope) {
        this.scope = scope.toUpperCase();
    }

    public int doStartTag() {
        JspWriter out = pageContext.getOut();
        Element element;
        try {
            if(("APPLICATION").equals(scope)){
                element= (Element)
                        pageContext.getServletContext().getAttribute(value);
            } else if(("SESSION").equals(scope)){
                element= (Element)
                        pageContext.getSession().getAttribute(value);
            }else if(("PAGE").equals(scope)){
                element= (Element)
                        pageContext.getAttribute(value);
            } else {
                //Request
                element= (Element)
                        pageContext.getRequest().getAttribute(value);
            }
            XhtmlBuffer xb = new XhtmlBuffer(out);
            element.toXhtml(xb);
        } catch (Exception e) {
            throw new Error(e);
        }
        return SKIP_BODY;
    }
}
