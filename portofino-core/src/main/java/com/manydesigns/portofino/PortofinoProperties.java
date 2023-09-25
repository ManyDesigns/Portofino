/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Property names
    //**************************************************************************

    //App properties
    public static final String BLOBMANAGER_FACTORY_CLASS = "blobmanager.factory";
    public static final String BLOBS_DIR_PATH = "blobs.dir.path";
    /**
     * Name of the application.
     */
    public static final String APP_NAME = "app.name";
    /**
     * Version of the application.
     */
    public static final String APP_VERSION = "app.version";
    /**
     * Directory containing the HTML file(s) used as the welcome page of the application.
     */
    public static final String APP_WELCOME_DIR = "app.welcome.dir";
    public static final String LOGIN_PATH = "login.path";

    //Server config
    public static final String HOSTNAMES = "portofino.hostnames";
    public static final String URL_ENCODING = "url.encoding";
    public static final String URL_ENCODING_DEFAULT = "UTF-8";

    //The mail address used as the from: field in email messages sent by the application (e.g. password change)
    public static final String MAIL_FROM = "mail.from";

    //Groovy
    public static final String PRELOAD_ACTIONS = "preload.actions";
    public static final String PRELOAD_CLASSES = "preload.classes";

    //BLOB Manager
    public static final String BLOB_MANAGER_TYPE = "blobmanager.type";
    public static final String AWS_REGION = "aws.region";
    public static final String AWS_S3_BUCKET = "aws.s3.bucket";
    public static final String AWS_S3_LOCATION = "aws.s3.location";

    private PortofinoProperties() {}

}
