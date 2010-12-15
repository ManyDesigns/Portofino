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

package com.manydesigns.portofino.actions;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.struts2.Struts2Utils;
import com.manydesigns.portofino.model.site.usecases.Button;
import com.opensymphony.xwork2.util.CompoundRoot;
import com.opensymphony.xwork2.util.ValueStack;
import ognl.OgnlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class CrudButton {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    protected final Button button;
    protected boolean enabled;


    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger =
            LoggerFactory.getLogger(CrudButton.class);

    //--------------------------------------------------------------------------
    // Guard management
    //--------------------------------------------------------------------------

    public CrudButton(Button button) {
        this.button = button;
    }

    
    //--------------------------------------------------------------------------
    // Guard management
    //--------------------------------------------------------------------------

    public void runGuard() {
        String guard = button.getGuard();
        if (guard == null) {
            enabled = true;
        }

        // Ognl context
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();

        // Ognl root
        ValueStack valueStack = Struts2Utils.getValueStack();
        CompoundRoot root = valueStack.getRoot();

        Object result = OgnlUtils.getValueQuietly(guard, ognlContext, root);

        enabled = Boolean.TRUE.equals(result);
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

    public boolean isEnabled() {
        return enabled;
    }

    public Button getButton() {
        return button;
    }
}
