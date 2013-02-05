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

package com.manydesigns.elements.struts1;

import com.manydesigns.elements.ElementsContext;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.i18n.TextProvider;
import com.manydesigns.elements.i18n.SimpleTextProvider;
import ognl.Ognl;
import ognl.OgnlContext;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.struts.chain.contexts.ServletActionContext;
import org.apache.struts.util.MessageResources;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ElementsCommand implements Command {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public boolean execute(Context context) throws Exception {
        ServletActionContext actionContext = (ServletActionContext) context;

        HttpServletRequest request = actionContext.getRequest();
        HttpServletResponse response = actionContext.getResponse();
        ServletContext servletContext = actionContext.getContext();

        MessageResources messageResources = actionContext.getMessageResources();
        TextProvider textProvider;
        if (messageResources == null) {
            textProvider = SimpleTextProvider.create();            
        } else {
            textProvider = new Struts1TextProvider(messageResources);
        }

        OgnlContext ognlContext = (OgnlContext) Ognl.createDefaultContext(null);

        ElementsContext elementsContext =
                ElementsThreadLocals.getElementsContext();

        elementsContext.setHttpServletRequest(request);
        elementsContext.setHttpServletResponse(response);
        elementsContext.setServletContext(servletContext);
        elementsContext.setTextProvider(textProvider);
        elementsContext.setOgnlContext(ognlContext);

        return CONTINUE_PROCESSING;
    }
}
