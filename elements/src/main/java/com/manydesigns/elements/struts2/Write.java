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

import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import com.opensymphony.xwork2.util.ValueStack;
import org.apache.struts2.components.Component;

import java.io.Writer;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Write extends Component {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected String value;

    public Write(ValueStack stack) {
         super(stack);
     }

    public void setValue(String value) {
         this.value = value;
     }

     public boolean start(Writer writer) {
         XhtmlFragment fragment =
                 (XhtmlFragment)getStack()
                         .findValue(value, XhtmlFragment.class);

         if (fragment != null) {
             XhtmlBuffer xb = new XhtmlBuffer(writer);
             fragment.toXhtml(xb);
         }

         return true;
     }

     public boolean end(Writer writer) {
         return true;
     }

     @Override
     public boolean usesBody() {
         return false;
     }
}
