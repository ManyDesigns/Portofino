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

package com.manydesigns.elements.struts1;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.struts.chain.contexts.ServletActionContext;
import org.apache.struts.util.MessageResources;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.ElementsContext;
import com.manydesigns.elements.i18n.TextProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import ognl.OgnlContext;
import ognl.Ognl;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ElementsCommand implements Command {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public boolean execute(Context context) throws Exception {
        ServletActionContext actionContext = (ServletActionContext) context;

        HttpServletRequest request = actionContext.getRequest();
        HttpServletResponse response = actionContext.getResponse();
        ServletContext servletContext = actionContext.getContext();

        MessageResources messageResources = actionContext.getMessageResources();
        TextProvider textProvider = new Struts1TextProvider(messageResources);

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
