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

package com.manydesigns.portofino.servlets;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.i18n.SimpleTextProvider;
import com.manydesigns.elements.i18n.TextProvider;
import com.manydesigns.portofino.i18n.MultipleTextProvider;
import com.manydesigns.portofino.i18n.ResourceBundleManager;
import com.manydesigns.portofino.modules.BaseModule;

import javax.servlet.*;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class I18nFilter implements Filter {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected ServletContext servletContext;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        servletContext = filterConfig.getServletContext();
    }

    @Override
    public void doFilter(
            ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        //I18n
        Locale locale = request.getLocale();
        ResourceBundleManager resourceBundleManager =
                (ResourceBundleManager) servletContext.getAttribute(BaseModule.RESOURCE_BUNDLE_MANAGER);
        ResourceBundle portofinoResourceBundle = resourceBundleManager.getBundle(locale);

        LocalizationContext localizationContext =
                new LocalizationContext(portofinoResourceBundle, locale);
        request.setAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".request", localizationContext);

        //Setup Elements I18n
        ResourceBundle elementsResourceBundle =
                ResourceBundle.getBundle(SimpleTextProvider.DEFAULT_MESSAGE_RESOURCE, locale);

        TextProvider textProvider =
                new MultipleTextProvider(
                        portofinoResourceBundle, elementsResourceBundle);
        ElementsThreadLocals.setTextProvider(textProvider);
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        servletContext = null;
    }
}
