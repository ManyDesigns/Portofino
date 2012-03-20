/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.elements.struts2;

import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.util.Util;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.util.ValueStack;

import java.util.Collections;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Struts2Utils {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public static String buildActionUrl(String method) {
        return buildActionUrl(method, Collections.EMPTY_MAP);
    }

    public static String buildActionUrl(String method, 
                                        Map<String,Object> parameters) {
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

        // add parameters
        boolean first = true;
        for (Map.Entry<String,Object> parameter : parameters.entrySet()) {
            String parameterName = parameter.getKey();
            Object parameterValue = parameter.getValue();
            String StringValue = OgnlUtils.convertValueToString(parameterValue);

            // parameters with null value are ignored
            if (parameterValue == null) {
                continue;
            }

            if (first) {
                sb.append("?");
                sb.append(Util.urlencode(parameterName));
                sb.append("=");
                sb.append(Util.urlencode(StringValue));
                first = false;
            } else {
                sb.append("&");
                sb.append(Util.urlencode(parameterName));
                sb.append("=");
                sb.append(Util.urlencode(StringValue));
            }
        }

        return sb.toString();
    }

    public static ValueStack getValueStack() {
        ActionContext actionContext = ActionContext.getContext();
        return actionContext.getValueStack();
    }

}
