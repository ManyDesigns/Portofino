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

package com.manydesigns.elements.struts2;

import com.manydesigns.elements.i18n.TextProvider;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Struts2TextProvider implements TextProvider {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    private com.opensymphony.xwork2.TextProvider provider;

    public Struts2TextProvider(com.opensymphony.xwork2.TextProvider provider) {
        this.provider = provider;
    }

    public String getText(String key, Object... args) {
        String[] strutsArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg != null) {
                strutsArgs[i] = arg.toString();
            }
        }
        
        return provider.getText(key, strutsArgs);
    }
}
