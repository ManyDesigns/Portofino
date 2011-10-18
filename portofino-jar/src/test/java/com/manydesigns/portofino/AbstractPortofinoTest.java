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
import com.manydesigns.elements.configuration.BeanLookup;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.application.ApplicationStarter;
import com.manydesigns.portofino.model.Model;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
                    new ApplicationStarter(portofinoConfiguration);
            applicationStarter.initializeApplication(appId);
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
                    "portofino-jar".equals(token)||
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
