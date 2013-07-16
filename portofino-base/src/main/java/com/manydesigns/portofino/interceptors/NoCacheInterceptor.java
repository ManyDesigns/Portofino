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

package com.manydesigns.portofino.interceptors;

import com.manydesigns.elements.servlet.ServletConstants;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@Intercepts(LifecycleStage.RequestInit)
public class NoCacheInterceptor implements Interceptor {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public Resolution intercept(ExecutionContext context) throws Exception {
        HttpServletResponse response = context.getActionBeanContext().getResponse();
        // Avoid caching of dynamic pages
        //HTTP 1.0
        response.setHeader(ServletConstants.HTTP_PRAGMA, ServletConstants.HTTP_PRAGMA_NO_CACHE);
        response.setDateHeader(ServletConstants.HTTP_EXPIRES, 0);

        //HTTP 1.1
        response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_NO_CACHE);
        response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_NO_STORE);
        //response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_MUST_REVALIDATE);
        //response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_MAX_AGE + 0);

        return context.proceed();
    }
}
