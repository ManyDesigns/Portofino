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

import com.opensymphony.xwork2.interceptor.Interceptor;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionContext;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.TextProvider;
import com.manydesigns.elements.fields.helpers.registry.DefaultRegistryBuilder;
import com.manydesigns.elements.fields.helpers.registry.FieldHelperRegistry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import org.apache.struts2.StrutsStatics;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ElementsInterceptor implements Interceptor {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected FieldHelperRegistry fieldHelperRegistry;

    public void destroy() {}

    public void init() {
        DefaultRegistryBuilder registryBuilder = new DefaultRegistryBuilder();
        fieldHelperRegistry = registryBuilder.build();
    }

    public String intercept(ActionInvocation invocation) throws Exception {
        Object action = invocation.getAction();
        ActionContext context = invocation.getInvocationContext();

        HttpServletRequest req =
                (HttpServletRequest)context.get(StrutsStatics.HTTP_REQUEST);
        HttpServletResponse res =
                (HttpServletResponse)context.get(StrutsStatics.HTTP_RESPONSE);
        ServletContext servletContext =
                (ServletContext)context.get(StrutsStatics.SERVLET_CONTEXT);

        HttpServletRequest oldReq =
                ElementsThreadLocals.getHttpServletRequest();
        HttpServletResponse oldRes =
                ElementsThreadLocals.getHttpServletResponse();
        ServletContext oldServletContext =
                ElementsThreadLocals.getServletContext();
        TextProvider oldTextProvider =
                ElementsThreadLocals.getTextProvider();


        try {
            ElementsThreadLocals.setFieldHelper(fieldHelperRegistry);
            
            ElementsThreadLocals.setHttpServletRequest(req);
            ElementsThreadLocals.setHttpServletResponse(res);
            ElementsThreadLocals.setServletContext(servletContext);

            if (action instanceof com.opensymphony.xwork2.TextProvider) {
                ElementsThreadLocals.setTextProvider(
                        new StrutsTextProvider(
                                (com.opensymphony.xwork2.TextProvider)action));
            } else {
                ElementsThreadLocals.setTextProvider(null);
            }

            return invocation.invoke();
        } finally {
            ElementsThreadLocals.setHttpServletRequest(oldReq);
            ElementsThreadLocals.setHttpServletResponse(oldRes);
            ElementsThreadLocals.setServletContext(oldServletContext);
            ElementsThreadLocals.setTextProvider(oldTextProvider);
        }
    }
}
