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

import junit.framework.TestCase;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class HibernateTest extends TestCase {
    public void setUp(){
    }

    public void tearDown(){
    }

    public void testHbm(){
        // Test che non servono a nulla mi servono solo per verificare come configurare
        // programmaticamente hibernate poi sono portati sotto la directory classica dei test
        /*Session session = HibernateUtil.getSessionFactory("1").getCurrentSession();
        session.beginTransaction();
        int i = 17;
        Map persona = (Map) session.load("persona", new Integer(i));

        session.delete("persona", persona);
        session.getTransaction().commit();
        */
    }
}