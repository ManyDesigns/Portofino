/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.application.ApplicationStarter;
import com.manydesigns.portofino.connections.ConnectionProvider;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.model.Model;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.h2.tools.RunScript;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public abstract class AbstractPortofinoTest extends AbstractElementsTest {

    // Long-lived Portofino objects
    protected PropertiesConfiguration portofinoConfiguration;
    protected Application application;

    //Connessioni e context
    public Model model;

    //--------------------------------------------------------------------------
    // Setup e teardown
    //--------------------------------------------------------------------------

    @Override
    public void setUp() throws Exception {
        super.setUp();
        portofinoConfiguration = new PropertiesConfiguration(
                            PortofinoProperties.PROPERTIES_RESOURCE);

        String appId = getTestAppId();

        Logger logger = LoggerFactory.getLogger(AbstractPortofinoTest.class);
        logger.info("Creating Context and " +
                "registering on servlet context...");
        // create and register the container first, without exceptions

        try {
            // ElementsThreadLocals è già stato impostato da AbstractElementsTest

            ApplicationStarter applicationStarter =
                    new ApplicationStarter(portofinoConfiguration);
            applicationStarter.initializeApplication(appId);
            application = applicationStarter.getApplication();
            model = application.getModel();


        } catch (Throwable e) {
            logger.error(ExceptionUtils.getRootCauseMessage(e), e);
        }

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }


    //--------------------------------------------------------------------------
    // Parametrizzazione del test
    //--------------------------------------------------------------------------

    public String getTestAppId() {
        return "default";
    }
}
