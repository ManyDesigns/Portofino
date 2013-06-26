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

package com.manydesigns.portofino.security;

/**
 * Enumerates the possible <i>access levels</i> to a page. In order of priority:
 * <ul>
 *     <li><strong><code>NONE</code></strong> - neither grants, nor forbids access.</li>
 *     <li><strong><code>VIEW</code></strong> - grants access to the page in read-only mode.</li>
 *     <li><strong><code>EDIT</code></strong> - grants access to the page in edit mode: some operations are permitted
 *     (depending on the type of page), but at least modifying the Groovy source code of the page is forbidden.</li>
 *     <li><strong><code>DEVELOP</code></strong> - grants every permissions except those reserved for the administrator
 *     (superuser), including that of editing the Groovy source code of the page.</li>
 *     <li><strong><code>DENY</code></strong> - denies access to the page.</li>
 * </ul>
 *
 * The level with the greatest priority wins over the others.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public enum AccessLevel {

    NONE(0), VIEW(1), EDIT(2), DEVELOP(3), DENY(Integer.MAX_VALUE);

    private AccessLevel(int level) {
        this.level = level;
    }

    public boolean isGreaterThanOrEqual(AccessLevel accessLevel) {
        return level >= accessLevel.level;
    }

    private final int level;

    public static final String copyright=
            "Copyright (c) 2005-2013, ManyDesigns srl";
}
