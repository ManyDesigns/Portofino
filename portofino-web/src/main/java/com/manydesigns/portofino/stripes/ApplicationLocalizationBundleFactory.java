/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.stripes;

import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.starter.ApplicationStarter;
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
@SuppressWarnings({"UnusedDeclaration"}) //Autodiscovered by Stripes
public class ApplicationLocalizationBundleFactory implements LocalizationBundleFactory {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    private static final Logger logger = LoggerFactory.getLogger(ApplicationLocalizationBundleFactory.class);

    protected ServletContext servletContext;

    public ResourceBundle getErrorMessageBundle(Locale locale) throws MissingResourceException {
        return getApplication().getBundle(locale);
    }

    public ResourceBundle getFormFieldBundle(Locale locale) throws MissingResourceException {
        return getApplication().getBundle(locale);
    }

    private Application getApplication() {
        logger.debug("Retrieving application starter");
        ApplicationStarter applicationStarter =
                (ApplicationStarter) servletContext.getAttribute(
                        ApplicationAttributes.APPLICATION_STARTER);

        logger.debug("Retrieving application");
        try {
            return applicationStarter.getApplication();
        } catch (Exception e) {
            throw new Error("Couldn't start application", e);
        }
    }

    public void init(Configuration configuration) throws Exception {
        servletContext = configuration.getServletContext();
    }
}
