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
 * Keys for configuration properties.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public final class PortofinoProperties {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

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

    public static final String PORTOFINO_VERSION =
            "portofino.version";
    public static final String WAR_REAL_PATH =
            "war.real.path";
    public static final String BLOBS_DIR_PATH =
            "blobs.dir.path";
    public static final String APP_NAME =
            "app.name";
    public static final String HOSTNAMES = "portofino.hostnames";
    public static final String URL_ENCODING = "url.encoding";
    public static final String TEMP_FILE_SERVICE_CLASS = "temp.file.service.class";

    //Login
    public static final String LOGIN_PAGE = "login.page";

    private PortofinoProperties() {}
}
