/*
* Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.dispatcher;

import com.manydesigns.portofino.actions.RequestAttributes;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.NameBasedActionResolver;
import net.sourceforge.stripes.exception.StripesServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ModelActionResolver extends NameBasedActionResolver {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    private static final Logger logger = LoggerFactory.getLogger(ModelActionResolver.class);

    @Override
    public void init(Configuration configuration) throws Exception {
        super.init(configuration);
        configuration.getServletContext();
    }

    @Override
    public ActionBean getActionBean(ActionBeanContext context, String urlBinding) throws StripesServletException {
        HttpServletRequest request = context.getRequest();
        Dispatch dispatch = (Dispatch) request.getAttribute(RequestAttributes.DISPATCH);
        if(dispatch != null) {
            Class<? extends ActionBean> actionBeanClass = dispatch.getActionBeanClass();
            try {
                ActionBean actionBean = makeNewActionBean(actionBeanClass, context);
                setActionBeanContext(actionBean, context);
                request.setAttribute(urlBinding, actionBean); //???
                assertGetContextWorks(actionBean);
                return actionBean;
            } catch (Exception e) {
                logger.error("Coulnd't instantiate action bean", e);
            }
        }
        return super.getActionBean(context, urlBinding);
    }

    @Override
    public Class<? extends ActionBean> getActionBeanType(String path) {
        Dispatch dispatch = Dispatcher.getCurrentDispatch();
        if(dispatch != null) {
            return dispatch.getActionBeanClass();
        } else {
            return super.getActionBeanType(path);
        }
    }
}
