/*
* Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.i18n;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.i18n.SimpleTextProvider;
import com.manydesigns.elements.i18n.TextProvider;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class I18nUtils {
    public static final String copyright = "Copyright (C) 2005-2020 ManyDesigns srl";

    public final static String RESOURCE_BUNDLE_MANAGER = "com.manydesigns.portofino.resourceBundleManager";
    public static final String PORTOFINO_MESSAGES_FILE_NAME = "portofino-messages.properties";

    private static final Logger logger = LoggerFactory.getLogger(I18nUtils.class);

    public static void setupResourceBundleManager(FileObject applicationDirectory, ServletContext servletContext) {
        logger.debug("Installing I18n ResourceBundleManager");
        ResourceBundleManager resourceBundleManager = new ResourceBundleManager();
        try {
            Enumeration<URL> messagesSearchPaths = I18nUtils.class.getClassLoader().getResources(PORTOFINO_MESSAGES_FILE_NAME);
            while (messagesSearchPaths.hasMoreElements()) {
                resourceBundleManager.addSearchPath(messagesSearchPaths.nextElement().toString());
            }
            FileObject appMessages = applicationDirectory.resolveFile(PORTOFINO_MESSAGES_FILE_NAME);
            resourceBundleManager.addSearchPath(appMessages.getName().getPath());
        } catch (IOException e) {
            logger.warn("Could not initialize resource bundle manager", e);
        }
        servletContext.setAttribute(I18nUtils.RESOURCE_BUNDLE_MANAGER, resourceBundleManager);
    }

    public static void setupTextProvider(ServletContext servletContext, ServletRequest request) {
        Locale locale = request.getLocale();
        ResourceBundleManager resourceBundleManager =
                (ResourceBundleManager) servletContext.getAttribute(RESOURCE_BUNDLE_MANAGER);
        ResourceBundle portofinoResourceBundle = resourceBundleManager.getBundle(locale);

        //Setup Elements I18n
        ResourceBundle elementsResourceBundle =
                ResourceBundle.getBundle(SimpleTextProvider.DEFAULT_MESSAGE_RESOURCE, locale);

        TextProvider textProvider =
                new MultipleTextProvider(
                        portofinoResourceBundle, elementsResourceBundle);
        ElementsThreadLocals.setTextProvider(textProvider);
    }


}
