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

package com.manydesigns.elements.components;

import com.manydesigns.elements.Element;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Html implements Element {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected String html;
    protected Mode mode = Mode.EDIT;

    //**************************************************************************
    // Costruttori
    //**************************************************************************
    public Html(String html) {
        this.html = html;
    }

    //**************************************************************************
    // Implementazione di Element
    //**************************************************************************
    public void readFromRequest(HttpServletRequest req) {}

    public boolean validate() {
        return true;
    }


    public void readFromObject(Object o) {}

    public void writeToObject(Object o) {}

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        if (mode.isEdit() || mode.isPreview() || mode.isView()) {
            xb.writeNoHtmlEscape(html);
        } else if (mode.isHidden()) {
            // do nothing
        } else {
            throw new IllegalStateException("Unknown mode: " + mode);
        }
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    //**************************************************************************
    // Getter/setter
    //**************************************************************************
    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
