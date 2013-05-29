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

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.configuration.BeanLookup;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.starter.ApplicationStarter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.StringTokenizer;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public abstract class AbstractPortofinoTest extends AbstractElementsTest {

    // Long-lived Portofino objects
    protected PropertiesConfiguration portofinoConfiguration;
    protected ApplicationStarter applicationStarter;
    protected Application application;

    //Connessioni e context
    public Model model;
    private String version;
    private String dir;

    //--------------------------------------------------------------------------
    // Setup e teardown
    //--------------------------------------------------------------------------

    @Override
    public void setUp() throws Exception {
        super.setUp();


        TestServerInfo serverInfo = new TestServerInfo();
        BeanLookup serverInfoLookup = new BeanLookup(serverInfo);
        ConfigurationInterpolator.registerGlobalLookup(
                ApplicationAttributes.SERVER_INFO,
                serverInfoLookup);

        portofinoConfiguration = new PropertiesConfiguration(
                            PortofinoProperties.PROPERTIES_RESOURCE);

        version = portofinoConfiguration.getString("portofino.version");
        dir = getPrjDir(portofinoConfiguration.getString("portofino.build.directory"));

        String appId = getTestAppId();

        Logger logger = LoggerFactory.getLogger(AbstractPortofinoTest.class);
        logger.info("Creating Context and " +
                "registering on servlet context...");
        // create and register the container first, without exceptions

        try {
            // ElementsThreadLocals è già stato impostato da AbstractElementsTest

            applicationStarter =
                    new ApplicationStarter(null, portofinoConfiguration, appId);
            applicationStarter.initializeApplication();
            application = applicationStarter.getApplication();
            model = application.getModel();


        } catch (Throwable e) {
            logger.error(ExceptionUtils.getRootCauseMessage(e), e);
        }

    }

    private String getPrjDir(String dir) {
        String sep = System.getProperty("file.separator");
        String result;
        result = dir.startsWith(sep)?sep:"";
        StringTokenizer tokenizer = new StringTokenizer(dir, sep);
        boolean first = true;
        while (tokenizer.hasMoreTokens()){
            String token = tokenizer.nextToken();
            if("elements-struts1".equals(token)||
                    "elements-struts2".equals(token)||
                    "elements".equals(token)||
                    "elements-extras".equals(token)||
                    "portofino-core".equals(token)||
                    "portofino-web".equals(token)||
                    "portofino-war".equals(token)){
                break;
            }
            if(first){
                result = result+token;
                first = false;
            } else {
                result = result + sep + token;

            }

        }
        return result;
    }

    @Override
    public void tearDown() throws Exception {

        super.tearDown();
        application.closeSessions();
        applicationStarter.destroy();
/*        String currDir;
        currDir = MessageFormat.format("{0}/portofino-war/target/portofino-war-{1}/WEB-INF/db",
                    dir, version);
        File directory = new File(currDir);
        File[] files = directory.listFiles();
        for (File file : files)
        {
            if (!file.delete())
            {
                System.out.println("Failed to delete "+file);
            }
        } */
    }


    //--------------------------------------------------------------------------
    // Parametrizzazione del test
    //--------------------------------------------------------------------------

    public String getTestAppId() {
        return "default";
    }

    public class TestServerInfo {
        String realPath;

        public String getRealPath(){
            String currDir;
            currDir = MessageFormat.format("{0}/portofino-war/target/portofino-war-{1}",
                    dir, version);
            return currDir;
        }
    }
}
