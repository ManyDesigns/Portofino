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
package com.manydesigns.portofino;

import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.parsing.HTMLParserFactory;
import com.meterware.servletunit.ServletRunner;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Hashtable;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public abstract class CommonTestUtil extends AbstractPortofinoTest{
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected ServletRunner servletRunner;
    private static final String FILECONFIGURATION = "portofino.properties";
    private static final long MILLIS_SLEEP_CLEANUP = 500;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        //Creo e setto il file di properties di configurazione
        setupPortofinoProperties();
        setUpWorkingDirectory();
        setupContainer();
        System.out.println("Context creati e legati correttamente");
    }

    private void setupPortofinoProperties() throws IOException{
//        portofinoConfiguration.setProperty(PortofinoProperties.MODEL_LOCATION,
//                "portofino-war/src/test/resources/portofino-model.xml");
    }

    private void setUpWorkingDirectory() {
        try {
            FileUtils.copyDirectory(new File("src/main/webapp"), new File("/tmp/portofinoTest"));
            FileUtils.copyDirectory(new File("src/test/resources"), new File("/tmp/portofinoTest/portofino-war/src/test/resources"));
               
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupContainer() throws Exception {
        HTMLParserFactory.useJTidyParser();
        HttpUnitOptions.setScriptingEnabled(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
       


        File f = new File("/tmp/portofinoTest/WEB-INF/web.xml");
        final String workingDirectory = "/tmp/portofinoTest";

        servletRunner = new ServletRunner(f, workingDirectory, null );

        servletRunner.registerServlet("/util.js",
                "com.manydesigns.portofino.FileServlet");
        servletRunner.registerServlet("/jscalendar/calendar.js",
                "com.manydesigns.portofino.FileServlet");
        servletRunner.registerServlet("/jscalendar/lang/calendar-it.js",
                "com.manydesigns.portofino.FileServlet");
        servletRunner.registerServlet("/jscalendar/lang/calendar-en.js",
                "com.manydesigns.portofino.FileServlet");
        servletRunner.registerServlet("/jscalendar/calendar-setup.js",
                "com.manydesigns.portofino.FileServlet");
        servletRunner.registerServlet("/xmlhttpForVersioning.js",
                "com.manydesigns.portofino.FileServlet");
        
        servletRunner.registerServlet("/yui-2.8.1/build/yuiloader-dom-event/yuiloader-dom-event.js",
                "com.manydesigns.portofino.FileServlet");
        servletRunner.registerServlet("/jquery-ui-1.8.5/js/jquery-1.4.2.min.js",
                    "com.manydesigns.portofino.FileServlet");
        servletRunner.registerServlet("/jquery-ui-1.8.5/js/jquery-ui-1.8.5.custom.min.js",
                    "com.manydesigns.portofino.FileServlet");
        servletRunner.registerServlet("/jquery-treetable-2.3.0/jquery.treeTable.min.js",
                    "com.manydesigns.portofino.FileServlet");
        servletRunner.registerServlet("/elements.js",
                    "com.manydesigns.portofino.FileServlet");
        servletRunner.registerServlet("/skins/default/portofino.js",
                    "com.manydesigns.portofino.FileServlet");

        servletRunner.registerServlet("*.action",
                "org.apache.struts2.dispatcher.ng.servlet.StrutsServlet");


        // recupera gli attributi del servlet context tramite reflection
        Object webApplication = getObjectField(servletRunner, "_application");
        Object servletContext = getObjectField(webApplication, "_servletContext");
        Hashtable servletContextAttributes =
                (Hashtable) getObjectField(servletContext, "_attributes");

        System.out.print("Retrieving container from servlet context... ");

    }

    private Object getObjectField(Object object, String fieldName)
            throws IllegalAccessException {
        Field fields[] = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (fieldName.equals(field.getName())) {
                // forziamo accessibilità campi privati
                field.setAccessible(true);
                return field.get(object);
            }
        }
        return null;
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        // shut down servlet runner
        //servletRunner.shutDown();
        servletRunner = null;
        // Esegue garbage collection e finalizzazioni per chiudere
        //tutte le connesioni eventualmente appese
        System.gc();
        // Thread.currentThread().yield();

        // Dà un po' di tempo a gc() per completare
        // Thread.sleep(MILLIS_SLEEP_CLEANUP);

        System.runFinalization();

        // Dà un po' di tempo a runFinalization() per completare
        Thread.sleep(500);

        // Lasciare che altri processi (db server)
        // riescano a chiudere bene ogni risorsa aperta
        Thread.yield();
        FileUtils.deleteDirectory(new File("/tmp/portofinoTest"));
    }
}
