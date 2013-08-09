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

package com.manydesigns.portofino.pageactions;

import com.manydesigns.portofino.pages.Page;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class EmbeddedPageAction implements Comparable<EmbeddedPageAction> {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final String path;
    protected final String id;
    protected final Integer index;
    protected final Page page;

    public EmbeddedPageAction(String id, Integer index, String path, Page page) {
        this.id = id;
        this.index = index;
        this.path = path;
        this.page = page;
    }

    public String getPath() {
        return path;
    }

    public String getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public Page getPage() {
        return page;
    }

    public int compareTo(EmbeddedPageAction that) {
        if (this.index == null) {
            if (that.index == null) {
                return this.path.compareTo(that.path);
            } else {
                return -1;
            }
        } else {
            if (that.index == null) {
                return 1;
            } else {
                return this.index.compareTo(that.index);
            }
        }
    }
}
