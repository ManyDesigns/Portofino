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

package com.manydesigns.portofino.actions.model;

import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import com.manydesigns.portofino.xml.XmlDiffer;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class TreeTableDiffer implements XhtmlFragment {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public final static String STATUS_OK = "Ok";
    public final static String STATUS_ONLY_ON_DB = "Only on db";
    public final static String STATUS_NOT_ON_DB = "Not on db";

    protected final XhtmlBuffer xb;
    protected int nodeCounter;

    boolean showBothNull;
    boolean showSourceNull;
    boolean showTargetNull;
    boolean showEqual;
    boolean showDifferent;

    public TreeTableDiffer() {
        super();
        xb = new XhtmlBuffer();
        nodeCounter = 1;
    }

    public void run(XmlDiffer.Differ rootDiffer) {
        run(rootDiffer, null);
    }

    public void run(XmlDiffer.Differ differ, String htmlClass) {
        if (!recursivelyShow(differ)) {
            return;
        }
        String id = generateId();
        xb.openElement("tr");
        xb.addAttribute("id", id);
        if (htmlClass != null) {
            xb.addAttribute("class", htmlClass);
        }

        xb.openElement("td");
        xb.write(differ.getName());
        xb.closeElement("td");

        writeTypeAndStatus(differ.getType(), differ.getStatus());

        xb.closeElement("tr");

        String childOfId = generateChildOfId(id);
        // scan attribute differs
        for (XmlDiffer.Differ attributeDiffer : differ.getAttributeDiffers()) {
            run(attributeDiffer, childOfId);
        }
        // scan child differs
        for (XmlDiffer.Differ childDiffer : differ.getChildDiffers()) {
            run(childDiffer, childOfId);
        }
    }

    boolean recursivelyShow(XmlDiffer.Differ differ) {
        XmlDiffer.Status status = differ.getStatus();
        switch (status) {
            case EQUAL:
                if (showEqual) return true;
                break;
            case DIFFERENT:
                if (showDifferent) return true;
                break;
            case BOTH_NULL:
                if (showBothNull) return true;
                break;
            case SOURCE_NULL:
                if (showSourceNull) return true;
                break;
            case TARGET_NULL:
                if (showTargetNull) return true;
                break;
            default:
                throw new Error("Unknown case");
        }

        for (XmlDiffer.Differ current : differ.getAttributeDiffers()) {
            if (recursivelyShow(current)) {
                return true;
            }
        }

        for (XmlDiffer.Differ current : differ.getChildDiffers()) {
            if (recursivelyShow(current)) {
                return true;
            }
        }

        return false;
    }

    private String generateId() {
        return MessageFormat.format("id-{0}", Integer.toString(nodeCounter++));
    }

    private String generateChildOfId(String parentId) {
        return MessageFormat.format("child-of-{0}", parentId);
    }

    private void writeTypeAndStatus(String type, XmlDiffer.Status status) {
        xb.openElement("td");
        xb.write(type);
        xb.closeElement("td");

        xb.openElement("td");
        switch(status) {
            case EQUAL:
                xb.addAttribute("class", "status_green");
                break;
            case BOTH_NULL:
                xb.addAttribute("class", "status_green");
                break;
            case SOURCE_NULL:
                xb.addAttribute("class", "status_red");
                break;
            case TARGET_NULL:
                xb.addAttribute("class", "status_red");
                break;
            case DIFFERENT:
                xb.addAttribute("class", "status_red");
                break;
            default:
                throw new Error("Unknown case");
        }
        xb.write(status.getLabel());
        xb.closeElement("td");
    }

    //--------------------------------------------------------------------------
    // XhtmlFragment implementation
    //--------------------------------------------------------------------------
    public void toXhtml(@NotNull XhtmlBuffer toBuffer) {
        xb.toXhtml(toBuffer);
    }

    //--------------------------------------------------------------------------
    // Getter/setter
    //--------------------------------------------------------------------------

    public boolean isShowBothNull() {
        return showBothNull;
    }

    public void setShowBothNull(boolean showBothNull) {
        this.showBothNull = showBothNull;
    }

    public boolean isShowSourceNull() {
        return showSourceNull;
    }

    public void setShowSourceNull(boolean showSourceNull) {
        this.showSourceNull = showSourceNull;
    }

    public boolean isShowTargetNull() {
        return showTargetNull;
    }

    public void setShowTargetNull(boolean showTargetNull) {
        this.showTargetNull = showTargetNull;
    }

    public boolean isShowEqual() {
        return showEqual;
    }

    public void setShowEqual(boolean showEqual) {
        this.showEqual = showEqual;
    }

    public boolean isShowDifferent() {
        return showDifferent;
    }

    public void setShowDifferent(boolean showDifferent) {
        this.showDifferent = showDifferent;
    }
}
