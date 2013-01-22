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

package com.manydesigns.portofino.stripes;

import com.manydesigns.elements.servlet.ServletUtils;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.util.UrlBuilder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ForbiddenAccessResolution implements Resolution {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public final static Logger logger =
            LoggerFactory.getLogger(ForbiddenAccessResolution.class);

    public static final int UNAUTHORIZED = 403;

    private String errorMessage;

    public ForbiddenAccessResolution() {}

    public ForbiddenAccessResolution(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Subject subject = SecurityUtils.getSubject();
        String userId = null;
        if (subject.isAuthenticated()) {
            userId = subject.getPrincipal().toString();
        }
        String originalPath = ServletUtils.getOriginalPath(request);
        UrlBuilder urlBuilder =
                new UrlBuilder(Locale.getDefault(), originalPath, false);
        Map parameters = request.getParameterMap();
        urlBuilder.addParameters(parameters);
        String returnUrl = urlBuilder.toString();
        if (userId == null){
            logger.info("Anonymous user not allowed. Redirecting to login.");

            new RedirectResolution("/actions/user/login")
                    .addParameter("returnUrl", returnUrl)
                    .addParameter("cancelReturnUrl", "/")
                    .execute(request, response);
        } else {
            logger.warn("User {} not authorized for url {}.", userId, returnUrl);
            new ErrorResolution(UNAUTHORIZED, errorMessage).execute(request, response);
        }
    }
}
