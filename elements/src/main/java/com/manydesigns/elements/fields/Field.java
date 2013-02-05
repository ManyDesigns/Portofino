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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.FormElement;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public interface Field extends FormElement {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    PropertyAccessor getPropertyAccessor();

    void valueToXhtml(XhtmlBuffer xb);
    void labelToXhtml(XhtmlBuffer xb);
    void helpToXhtml(XhtmlBuffer xb);
    void errorsToXhtml(XhtmlBuffer xb);

    public String  getId();
    void setId(String id);

    String getInputName();
    void setInputName(String inputName);

    boolean isRequired();
    void setRequired(boolean required);

    String getHref();
    void setHref(String href);

    String getTitle();
    void setTitle(String alt);

    String getStringValue();

    boolean isEnabled();
    void setEnabled(boolean enabled);

    boolean isInsertable();
    void setInsertable(boolean insertable);

    boolean isUpdatable();
    void setUpdatable(boolean updatable);

    Object getValue();

    String getDisplayValue();
}
