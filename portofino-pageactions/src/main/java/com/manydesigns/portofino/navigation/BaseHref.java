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

package com.manydesigns.portofino.navigation;

import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.controller.StripesConstants;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class BaseHref {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static void emit(HttpServletRequest request, XhtmlBuffer xb) {
        //Setup base href - uniform handling of .../resource and .../resource/
        ActionBean actionBean = (ActionBean) request.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN);
        if(actionBean instanceof AbstractActionBean) {
            String baseHref =
                    request.getContextPath() +
                    ((AbstractActionBean) actionBean).getContext().getActionPath();
            //Remove all trailing slashes
            while (baseHref.length() > 1 && baseHref.endsWith("/")) {
                baseHref = baseHref.substring(0, baseHref.length() - 1);
            }
            //Add a single trailing slash so all relative URLs use this page as the root
            baseHref += "/";
            //Try to make the base HREF absolute
            try {
                URL url = new URL(request.getRequestURL().toString());
                String port = url.getPort() > 0 ? ":" + url.getPort() : "";
                baseHref = url.getProtocol() + "://" + url.getHost() + port + baseHref;
            } catch (MalformedURLException e) {
                //Ignore
            }

            xb.openElement("base");
            xb.addAttribute("href", baseHref);
            xb.closeElement("base");
        }
    }

}
