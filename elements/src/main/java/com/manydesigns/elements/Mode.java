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

package com.manydesigns.elements;

import org.apache.commons.lang.builder.ToStringBuilder;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public enum Mode {
    /**
     * create forms: regular inputs
     */
    CREATE(8, "CREATE"),

    /**
     * update forms: regular inputs
     */
    EDIT(0, "EDIT"),

    /**
     * create preview/confirmation pages: plain text values + hidden inputs
     */
    CREATE_PREVIEW(9, "CREATE_PREVIEW"),

    /**
     * update preview/confirmation pages: plain text values + hidden inputs
     */
    PREVIEW(1, "PREVIEW"),

    /**
     * read/view pages: plain text values, no inputs
     */
    VIEW(2, "VIEW"),

    /**
     * create - no visual display, only hidden inputs.
     */
    CREATE_HIDDEN(11, "CREATE_HIDDEN"),

    /**
     * update - no visual display, only hidden inputs.
     */
    HIDDEN(3, "HIDDEN");

    private final static int BASE_MODE_MASK = 3;
    private final static int CREATE_MASK = 8;

    private boolean create;

    private boolean edit;
    private boolean preview;
    private boolean view;
    private boolean hidden;

    private final String name;

    Mode(int value, String name) {
        int baseMode = value & BASE_MODE_MASK;
        switch(baseMode) {
            case 0:
                edit = true;
                break;
            case 1:
                preview = true;
                break;
            case 2:
                view = true;
                break;
            case 3:
                hidden = true;
                break;
            default:
                throw new InternalError("Unrecognized mode: " + value);
        }
        if ((value & CREATE_MASK) != 0) {
            create = true;
        }
        this.name = name;
    }

    public boolean isCreate() {
        return create;
    }

    public boolean isEdit() {
        return edit;
    }

    public boolean isPreview() {
        return preview;
    }

    public boolean isView() {
        return view;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isView(boolean immutable) {
        return view || (!hidden && !create && immutable);
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("create", create)
                .append("edit", edit)
                .append("preview", preview)
                .append("view", view)
                .append("hidden", hidden)
                .toString();
    }
}
