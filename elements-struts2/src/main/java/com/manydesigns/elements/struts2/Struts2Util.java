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

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.CompoundRoot;
import com.manydesigns.elements.ElementsThreadLocals;
import ognl.OgnlContext;
import ognl.Ognl;
import ognl.OgnlException;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Struts2Util {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public static String buildActionUrl(String method) {
        ActionContext actionContext = ActionContext.getContext();
        ActionInvocation actionInvocation = actionContext.getActionInvocation();
        ActionProxy actionProxy = actionInvocation.getProxy();
        String namespace = actionProxy.getNamespace();
        String actionName = actionProxy.getActionName();

        StringBuilder sb = new StringBuilder();
        if ("/".equals(namespace)) {
            sb.append("/");
        } else {
            sb.append(namespace);
            sb.append("/");
        }
        sb.append(actionName);
        if (method != null) {
            sb.append("!");
            sb.append(method);
        }
        sb.append(".action");
        return sb.toString();
    }

    public static ValueStack getValueStack() {
        ActionContext actionContext = ActionContext.getContext();
        return actionContext.getValueStack();
    }

    public static Object getValue(String expression) throws OgnlException {
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
        ValueStack valueStack = Struts2Util.getValueStack();
        CompoundRoot root = valueStack.getRoot();
        return Ognl.getValue(expression, ognlContext, root);

    }
}
