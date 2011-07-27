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
package com.manydesigns.elements.struts2;

import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import com.opensymphony.xwork2.util.ValueStack;
import org.apache.struts2.components.Component;

import java.io.Writer;
import java.util.List;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class SessionMessages extends Component {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public SessionMessages(ValueStack stack) {
        super(stack);
    }

    public boolean start(Writer writer) {
        XhtmlBuffer xb = new XhtmlBuffer(writer);

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
            return true;
        }

        xb.openElement("div");
        xb.addAttribute("class", "userMessages");

        writeList(xb, errorMessages, "errorMessages");
        writeList(xb, warningMessages, "warningMessages");
        writeList(xb, infoMessages, "infoMessages");

        xb.closeElement("div");

        return true;
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

    public boolean end(Writer writer) {
        return true;
    }

    @Override
    public boolean usesBody() {
        return false;
    }
}
