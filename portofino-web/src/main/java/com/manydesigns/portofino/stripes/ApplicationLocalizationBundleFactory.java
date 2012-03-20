/*
* Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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
