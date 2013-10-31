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

package com.manydesigns.portofino.stripes;

import com.manydesigns.portofino.i18n.ResourceBundleManager;
import com.manydesigns.portofino.modules.BaseModule;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.localization.LocalizationBundleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PortofinoLocalizationBundleFactory implements LocalizationBundleFactory {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(PortofinoLocalizationBundleFactory.class);

    protected ResourceBundleManager resourceBundleManager;

    public ResourceBundle getErrorMessageBundle(Locale locale) throws MissingResourceException {
        return resourceBundleManager.getBundle(locale);
    }

    public ResourceBundle getFormFieldBundle(Locale locale) throws MissingResourceException {
        return resourceBundleManager.getBundle(locale);
    }

    public void init(Configuration configuration) throws Exception {
        ServletContext servletContext = configuration.getServletContext();
        resourceBundleManager =
                (ResourceBundleManager) servletContext.getAttribute(BaseModule.RESOURCE_BUNDLE_MANAGER);
    }
}
