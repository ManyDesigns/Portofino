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

package com.manydesigns.elements.servlet;

import com.manydesigns.elements.xml.XhtmlBuffer;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class SessionMessagesTag extends TagSupport {
    public int doStartTag() {
        JspWriter out = pageContext.getOut();
        XhtmlBuffer xb = new XhtmlBuffer(out);

        List<String> errorMessages =
                com.manydesigns.elements.messages.SessionMessages
                        .consumeErrorMessages();
        List<String> warningMessages =
                com.manydesigns.elements.messages.SessionMessages
                        .consumeWarningMessages();
        List<String> infoMessages =
                com.manydesigns.elements.messages.SessionMessages
                        .consumeInfoMessages();

        if (errorMessages.size() == 0 &&
                warningMessages.size() == 0 &&
                infoMessages.size() == 0) {
            return SKIP_BODY;
        }

        xb.openElement("div");
        xb.addAttribute("class", "userMessages");

        writeList(xb, errorMessages, "errorMessages");
        writeList(xb, warningMessages, "warningMessages");
        writeList(xb, infoMessages, "infoMessages");

        xb.closeElement("div");
        return SKIP_BODY;
    }

    private void writeList(XhtmlBuffer xb,
                           List<String> errorMessages,
                           String htmlClass) {
        if (errorMessages.size() == 0) {
            return;
        }

        xb.openElement("ul");
        xb.addAttribute("class", htmlClass);
        for (String current : errorMessages) {
            xb.openElement("li");
            xb.writeNoHtmlEscape(current);
            xb.closeElement("li");
        }
        xb.closeElement("ul");
    }


}
