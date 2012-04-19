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

import com.manydesigns.elements.ElementsContext;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.i18n.SimpleTextProvider;
import com.manydesigns.elements.i18n.TextProvider;
import com.manydesigns.elements.ognl.CustomTypeConverter;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import ognl.OgnlContext;
import org.apache.struts2.StrutsStatics;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ElementsInterceptor implements Interceptor {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public void destroy() {}

    public void init() {}

    public String intercept(ActionInvocation invocation) throws Exception {
        Object action = invocation.getAction();
        ActionContext context = invocation.getInvocationContext();

        HttpServletRequest req =
                (HttpServletRequest)context.get(StrutsStatics.HTTP_REQUEST);
        HttpServletResponse res =
                (HttpServletResponse)context.get(StrutsStatics.HTTP_RESPONSE);
        ServletContext servletContext =
                (ServletContext)context.get(StrutsStatics.SERVLET_CONTEXT);
        OgnlContext ognlContext = (OgnlContext)context.getContextMap();

        CustomTypeConverter converter
                = new CustomTypeConverter(ognlContext.getTypeConverter());
        ognlContext.setTypeConverter(converter);

        ElementsContext elementsContext =
                ElementsThreadLocals.getElementsContext();

        try {
            elementsContext.setHttpServletRequest(req);
            elementsContext.setHttpServletResponse(res);
            elementsContext.setServletContext(servletContext);
            elementsContext.setOgnlContext(ognlContext);

            TextProvider textProvider;
            if (action instanceof com.opensymphony.xwork2.TextProvider) {
                textProvider =
                        new Struts2TextProvider(
                                (com.opensymphony.xwork2.TextProvider)action);
            } else {
                textProvider = SimpleTextProvider.create();
            }
            elementsContext.setTextProvider(textProvider);

            return invocation.invoke();
        } finally {
            ElementsThreadLocals.removeElementsContext();
        }
    }
}
