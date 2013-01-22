/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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
            "Copyright (c) 2005-2013, ManyDesigns srl";

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
