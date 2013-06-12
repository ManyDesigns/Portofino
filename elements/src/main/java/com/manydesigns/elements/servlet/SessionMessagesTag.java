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

        if(!errorMessages.isEmpty()) {
            xb.openElement("div");
            xb.addAttribute("class", "alert alert-error fade in");
            writeCloseButton(xb);
            writeList(xb, errorMessages, "errorMessages");
            xb.closeElement("div");
        }

        if(!warningMessages.isEmpty()) {
            xb.openElement("div");
            xb.addAttribute("class", "alert fade in");
            writeCloseButton(xb);
            writeList(xb, warningMessages, "warningMessages");
            xb.closeElement("div");
        }

        if(!infoMessages.isEmpty()) {
            xb.openElement("div");
            xb.addAttribute("class", "alert alert-success fade in");
            writeCloseButton(xb);
            writeList(xb, infoMessages, "infoMessages");
            xb.closeElement("div");
        }

        return SKIP_BODY;
    }

    private void writeCloseButton(XhtmlBuffer xb) {
        xb.writeNoHtmlEscape("<button data-dismiss=\"alert\" class=\"close\" type=\"button\">&times;</button>");
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
