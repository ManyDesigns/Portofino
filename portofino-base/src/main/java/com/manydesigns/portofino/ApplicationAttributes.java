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

package com.manydesigns.portofino;

/**
 * Keys for attributes stored in application scope.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ApplicationAttributes {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public final static String CLASS_LOADER = "com.manydesigns.portofino.application.classLoader";
    public final static String ELEMENTS_CONFIGURATION = "elementsConfiguration";
    public final static String PORTOFINO_CONFIGURATION = "portofinoConfiguration";
    public final static String SERVLET_CONTEXT = "servletContext";
    public final static String SERVER_INFO = "serverInfo";
    public final static String APPLICATION_DIRECTORY = "com.manydesigns.portofino.application.directory";
    public final static String APPLICATION_STARTER = "applicationStarter";
    public final static String MAIL_QUEUE = "mailQueue";
    public final static String MAIL_SENDER = "mailSender";
    public final static String MAIL_CONFIGURATION = "mailConfiguration";
    public final static String EHCACHE_MANAGER = "portofino.ehcache.manager";
    public final static String MODULE_REGISTRY = "com.manydesigns.portofino.modules.ModuleRegistry";
    public final static String ADMIN_MENU = "com.manydesigns.portofino.menu.Menu.admin";
}
