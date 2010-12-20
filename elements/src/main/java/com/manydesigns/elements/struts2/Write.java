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
*/
public class Write extends Component {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

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
