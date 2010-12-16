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
*/
public abstract class CommonTestUtil extends AbstractPortofinoTest{
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

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
        PortofinoProperties.loadProperties("portofino_webtest.properties");    }

    private void setUpWorkingDirectory() {
        try {
            FileUtils.copyDirectory(new File("portofino-war/src/main/webapp"), new File("/tmp/portofinoTest"));
            FileUtils.copyDirectory(new File("portofino-war/src/test/resources"), new File("/tmp/portofinoTest/portofino-war/src/test/resources"));
               
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
        servletRunner = new ServletRunner(f, workingDirectory, workingDirectory );

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
