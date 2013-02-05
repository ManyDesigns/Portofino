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

package com.manydesigns.elements;

import org.apache.commons.lang.builder.ToStringBuilder;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
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
     * bulk update forms: regular inputs
     */
    BULK_EDIT(16, "BULK_EDIT"),

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
    private final static int BULK_MASK = 16;

    private final boolean create;
    private final boolean bulk;

    private final boolean edit;
    private final boolean preview;
    private final boolean view;
    private final boolean hidden;

    private final String name;

    Mode(int value, String name) {
        int baseMode = value & BASE_MODE_MASK;
        switch(baseMode) {
            case 0:
                edit = true;
                preview = false;
                view = false;
                hidden = false;
                break;
            case 1:
                edit = false;
                preview = true;
                view = false;
                hidden = false;
                break;
            case 2:
                edit = false;
                preview = false;
                view = true;
                hidden = false;
                break;
            case 3:
                edit = false;
                preview = false;
                view = false;
                hidden = true;
                break;
            default:
                throw new InternalError("Unrecognized mode: " + value);
        }
        create = (value & CREATE_MASK) != 0;
        bulk = (value & BULK_MASK) != 0;
        this.name = name;
    }

    public boolean isCreate() {
        return create;
    }

    public boolean isBulk() {
        return bulk;
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

    public boolean isView(boolean insertable, boolean updatable) {
        return view
                || (!hidden && !create && !updatable)
                || (!hidden && create && !insertable)
                ;
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
