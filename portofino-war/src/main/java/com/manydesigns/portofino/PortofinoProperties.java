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

package com.manydesigns.portofino;

import com.manydesigns.elements.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class PortofinoProperties {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Default and custom properties location
    //**************************************************************************

    public final static String PROPERTIES_RESOURCE =
            "portofino.properties";
    public final static String CUSTOM_PROPERTIES_RESOURCE =
            "portofino-custom.properties";


    //**************************************************************************
    // Property names
    //**************************************************************************

    public static final String PORTOFINO_VERSION_PROPERTY =
            "portofino.version";
    public static final String CONNECTIONS_LOCATION_PROPERTY =
            "connections.location";
    public static final String MODEL_LOCATION_PROPERTY =
            "model.location";
    public static final String CONTEXT_CLASS_PROPERTY =
            "context.class";
    public static final String APPLICATION_NAME_PROPERTY =
            "application.name";
    public static final String DATABASE_PLATFORMS_MANAGER_PROPERTY =
            "database.platforms.manager";
    public static final String DATABASE_PLATFORMS_LIST_PROPERTY =
            "database.platforms.list";
    public static final String SECURITY_TYPE_PROPERTY =
            "security.type";
    public static final String PORTOFINO_STOREDIR_PROPERTY =
            "portofino.store.dir";
    public static final String PORTOFINO_WORKDIR_PROPERTY =
            "portofino.work.dir";
    public static final String CONNECTION_FILE_PROPERTY =
            "connection.file";

    //Email properties
    public static final String MAIL_ENABLED = "mail.enabled";
    public static final String MAIL_SMTP_SENDER = "mail.smtp.sender";
    public static final String MAIL_SMTP_HOST = "mail.smtp.host";
    public static final String MAIL_SMTP_PORT = "mail.smtp.port";
    public static final String MAIL_SMTP_SSL_ENABLED = "mail.smtp.ssl.enabled";
    public static final String MAIL_SMTP_LOGIN = "mail.smtp.login";
    public static final String MAIL_SMTP_PASSWORD = "mail.smtp.password";
    public static final String MAIL_POP3_HOST = "mail.pop3.host";
    public static final String MAIL_POP3_PROTOCOL = "mail.pop3.protocol";
    public static final String MAIL_POP3_PORT = "mail.pop3.port";
    public static final String MAIL_POP3_LOGIN = "mail.pop3.login";
    public static final String MAIL_POP3_PASSWORD = "mail.pop3.password";
    public static final String MAIL_BOUNCE_ENABLED = "mail.bounce.enabled";
    public static final String MAIL_POP3_SSL_ENABLED = "mail.pop3.ssl.enabled";
    public static final String KEEP_SENT = "mail.keep.sent";

    //Password properties
    public static final String PWD_ENCRYPTED = "pwd.encrypted";

    

    //**************************************************************************
    // Static fields, singleton initialization and retrieval
    //**************************************************************************

    private static final Properties properties;
    public static final Logger logger =
            LoggerFactory.getLogger(PortofinoProperties.class);


    static {
        properties = new Properties();
        reloadProperties();
    }

    public static void reloadProperties() {
        properties.clear();

        loadProperties(PROPERTIES_RESOURCE);
        loadProperties(CUSTOM_PROPERTIES_RESOURCE);
    }

    public static void loadProperties(String resource) {
        InputStream stream = ReflectionUtil.getResourceAsStream(resource);
        if (stream == null) {
            logger.info("Properties resource not found: {}", resource);
            return;
        }
        try {
            properties.load(stream);
            logger.info("Properties loaded from: {}", resource);
        } catch (Throwable e) {
            logger.warn(String.format(
                    "Error loading properties from: %s", resource), e);
        }
    }

    public static Properties getProperties() {
        return properties;
    }

    //**************************************************************************
    // Dummy constructor
    //**************************************************************************

    private PortofinoProperties() {}

}
