/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.calendar;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.xml.XhtmlBuffer;

/**
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PresentationHelper {
    
    public static void writeDialogCloseButtonInFooter(XhtmlBuffer xhtmlBuffer) {
        xhtmlBuffer.openElement("button");
        xhtmlBuffer.addAttribute("class", "btn btn-primary");
        xhtmlBuffer.addAttribute("data-dismiss", "modal");
        xhtmlBuffer.addAttribute("aria-hidden", "true");
        xhtmlBuffer.write(ElementsThreadLocals.getText("close"));
        xhtmlBuffer.closeElement("button");
    }

    public static void writeDialogCloseButtonInHeader(XhtmlBuffer xhtmlBuffer) {
        xhtmlBuffer.openElement("button");
        xhtmlBuffer.addAttribute("type", "button");
        xhtmlBuffer.addAttribute("class", "close");
        xhtmlBuffer.addAttribute("data-dismiss", "modal");
        xhtmlBuffer.addAttribute("aria-hidden", "true");
        xhtmlBuffer.writeNoHtmlEscape("&times;");
        xhtmlBuffer.closeElement("button");
    }

    public static void closeDialog(XhtmlBuffer xhtmlBuffer) {
        xhtmlBuffer.closeElement("div");
        xhtmlBuffer.closeElement("div");
        xhtmlBuffer.closeElement("div");
    }

    public static void openDialog(XhtmlBuffer xhtmlBuffer, String dialogId, String dialogLabelId) {
        xhtmlBuffer.openElement("div");
        xhtmlBuffer.addAttribute("id", dialogId);
        xhtmlBuffer.addAttribute("class", "modal");
        xhtmlBuffer.addAttribute("tabindex", "-1");
        xhtmlBuffer.addAttribute("role", "dialog");
        xhtmlBuffer.addAttribute("aria-hidden", "true");
        if(dialogLabelId != null) {
            xhtmlBuffer.addAttribute("aria-labelledby", dialogLabelId);
        }

        xhtmlBuffer.openElement("div");
        xhtmlBuffer.addAttribute("class", "modal-dialog");
        xhtmlBuffer.openElement("div");
        xhtmlBuffer.addAttribute("class", "modal-content");
    }
    
}
