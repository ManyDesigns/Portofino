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

package com.manydesigns.elements.servlet;

import com.manydesigns.elements.Element;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspWriter;
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

        List<XhtmlFragment> errorMessages =
                com.manydesigns.elements.messages.SessionMessages
                        .consumeErrorMessages();
        List<XhtmlFragment> warningMessages =
                com.manydesigns.elements.messages.SessionMessages
                        .consumeWarningMessages();
        List<XhtmlFragment> infoMessages =
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
                           List<XhtmlFragment> errorMessages,
                           String htmlClass) {
        if (errorMessages.size() == 0) {
            return;
        }

        xb.openElement("ul");
        xb.addAttribute("class", htmlClass);
        for (XhtmlFragment current : errorMessages) {
            xb.openElement("li");
            current.toXhtml(xb);
            xb.closeElement("li");
        }
        xb.closeElement("ul");
    }


}
