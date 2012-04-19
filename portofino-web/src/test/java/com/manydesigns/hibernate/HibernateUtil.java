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
package com.manydesigns.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Map;
import java.util.HashMap;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class HibernateUtil {

    private static final Map<String,SessionFactory> sessionFactories = new HashMap<String,SessionFactory>();

    private static SessionFactory buildSessionFactory(String name) {
        try {
            Configuration result = new Configuration().setProperty("default_entity_mode", "dynamic-map");
            result = result
                    .addResource("src/test/java/com/manydesigns/hibernate/persona.hbm.xml");
            result = result
                    .setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:5432/hibernatetest")
                    .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
                    .setProperty("hibernate.connection.username", "manydesigns")
                    .setProperty("hibernate.connection.password", "manydesigns")
                    .setProperty("hibernate.current_session_context_class",
                            "org.hibernate.context.ThreadLocalSessionContext")

                    .setProperty("hibernate.show_sql", "true");
                    //.configure(MessageFormat.format("hibernate_{0}.cfg.xml", name))
             return result.buildSessionFactory();


        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory(String name) {
        SessionFactory session = sessionFactories.get(name);
        if (session==null)
        {
            session = buildSessionFactory(name);
            sessionFactories.put(name, session);
        }
        return session;
    }

}
