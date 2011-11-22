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
package com.manydesigns.portofino.application;

import com.manydesigns.portofino.AbstractPortofinoTest;
import com.manydesigns.portofino.database.SessionUtils;
import com.manydesigns.portofino.logic.DataModelLogic;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.UsersGroups;
import org.hibernate.Session;
import org.hibernate.proxy.map.MapProxy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Angelo    Lupo       - angelo.lupo@manydesigns.com
 * @author Paolo     Predonzani - paolo.predonzani@manydesigns.com
 */
public class HibernateTest extends AbstractPortofinoTest {
    private static final String PORTOFINO_PUBLIC_USER = "portofino.public.users";

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() {
        application.closeSessions();
    }

    public void testReadProdotti() {
        List<Object> resultProd =
                SessionUtils.getAllObjects(application, "jpetstore.PUBLIC.product");
        int sizePrd = resultProd.size();
        assertEquals("prodotti", 16, sizePrd);
    }

    public void testUsers() {
        List<Object> groupList =
                SessionUtils.getAllObjects(application, "portofino.public.groups");
        assertEquals( 2, groupList.size());

        List<Object> usergroups =
                SessionUtils.getAllObjects(application, "portofino.public.users_groups");
        assertEquals( 3, usergroups.size());

        List<Object> users =
                SessionUtils.getAllObjects(application, PORTOFINO_PUBLIC_USER);
        assertEquals("numero utenti", 2, users.size());
        User admin = (User) users.get(0);
        List<UsersGroups> groups = admin.getGroups();
        assertEquals("numero gruppi per admin",  2, groups.size());
        

    }
    public void testSearchAndReadCategorieProdotti() {
        List<Object> resultCat =
                SessionUtils.getAllObjects(application, "jpetstore.PUBLIC.category");

        int sizeCat = resultCat.size();
        assertEquals("categorie", 5, sizeCat);


        Map categoria0 = (Map<String, Object>) resultCat.get(0);
        assertEquals("jpetstore_public_category", categoria0.get("$type$"));
        assertNotNull(categoria0.get("name"));
        Map categoria1 = (Map<String, Object>)resultCat.get(1);
        assertNotNull(categoria0.get("name"));
        Map categoria2 = (Map<String, Object>)resultCat.get(2);
        assertNotNull(categoria0.get("name"));
        Map categoria3 = (Map<String, Object>)resultCat.get(3);
        assertNotNull(categoria0.get("name"));
        Map categoria4 = (Map<String, Object>)resultCat.get(4);
        assertNotNull(categoria0.get("name"));

        List<Object> resultProd =
                SessionUtils.getAllObjects(application, "jpetstore.PUBLIC.product");

        Table table = DataModelLogic.findTableByQualifiedName(
                application.getModel(), "jpetstore.PUBLIC.category");
        TableAccessor tableAccessor = new TableAccessor(table);
        TableCriteria criteria = new TableCriteria(table);
        HashMap<String, String> category= findCategory(tableAccessor, criteria);

        int sizePrd = resultProd.size();
        assertEquals("prodotti", 16, sizePrd);
        Map prd0 = (Map<String, Object>)resultProd.get(0);
        assertEquals("FI-SW-01", prd0.get("productid") );
        assertEquals("Angelfish", prd0.get("name"));
    }

    private HashMap<String, String> findCategory(TableAccessor tableAccessor, TableCriteria criteria) {
        HashMap<String, String> category=null;
        try {
            criteria.eq(tableAccessor.getProperty("catid"), "FISH");
            Session session = application.getSession(tableAccessor.getTable().getDatabaseName());
            List<Object> listObjs = SessionUtils.getObjects(session, criteria, null, null);
            assertEquals(1, listObjs.size());
            category = (HashMap<String, String>) listObjs.get(0);
            String catid = category.get("catid");
            assertEquals("FISH", catid);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            fail();
        }
        return category;
    }

    public void testSearchAndUpdateCategorie() {
        Table table = DataModelLogic.findTableByQualifiedName(
                application.getModel(), "jpetstore.PUBLIC.category");
        TableAccessor tableAccessor = new TableAccessor(table);
        TableCriteria criteria = new TableCriteria(table);

        List<Object> resultCat =
                SessionUtils.getAllObjects(application, "jpetstore.PUBLIC.category");
        int sizeCat = resultCat.size();
        assertEquals("categorie", 5, sizeCat);
        Map<String, String> categoria0 =  findCategory(tableAccessor, criteria);
        assertEquals("jpetstore_public_category", categoria0.get("$type$"));
        assertEquals("Fish", categoria0.get("name"));
        categoria0.put("name", "Pesciu");
        Session session = application.getSession("jpetstore");
        session.update("jpetstore_PUBLIC_category", categoria0);
        session.getTransaction().commit();
        application.closeSessions();

        //Controllo l'aggiornamento e riporto le cose come stavano
        criteria = new TableCriteria(table);
        categoria0 =  findCategory(tableAccessor, criteria);
        assertEquals("jpetstore_public_category", categoria0.get("$type$"));
        assertEquals("Pesciu", categoria0.get("name"));
        categoria0.put("name", "Fish");
        session = application.getSession("jpetstore");
        session.update("jpetstore_PUBLIC_category", categoria0);
        session.getTransaction().commit();
        application.closeSessions();
    }

    public void testSaveCategoria() {
        Map<String, Object> worms = new HashMap<String, Object>();
        worms.put("$type$", "jpetstore.PUBLIC.category");
        worms.put("catid", "VERMI");
        worms.put("name", "worms");
        worms.put("descn",
          "<image src=\"../images/worms_icon.gif\"><font size=\"5\" color=\"blue\">" +
                  "Worms</font>");

        Session session = application.getSession("jpetstore");
        session.save("jpetstore_PUBLIC_category", worms);
        session.getTransaction().commit();
    }

    public void testSaveLineItem() {
        Map<String, Object> lineItem = new HashMap<String, Object>();
        lineItem.put("$type$", "jpetstore.PUBLIC.lineitem");
        lineItem.put("orderid", 2);
        lineItem.put("linenum", 2);
        lineItem.put("itemid",
          "test");
        lineItem.put("quantity", 20);
        lineItem.put("unitprice", new BigDecimal(10.80));

        Session session = application.getSession("jpetstore");
        session.save("jpetstore_PUBLIC_lineitem", lineItem);
        session.getTransaction().commit();

        application.closeSessions();

        //e ora cancello
        try {
            session = application.getSession("jpetstore");
            session.delete("jpetstore_PUBLIC_lineitem", lineItem);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail();
        }
        application.closeSessions();
    }


    public void testSaveTestElement() {
        try {
            Map<String, Object> testItem = new HashMap<String, Object>();
            testItem.put("$type$", "hibernatetest.PUBLIC.table1");
            testItem.put("testo", "esempio");
            //salvo
            Session session = application.getSession("hibernatetest");
            session.save("hibernatetest_PUBLIC_table1", testItem);
            session.getTransaction().commit();

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testDeleteCategoria() {
        try {
            Map<String, Object> worms = new HashMap<String, Object>();
            worms.put("$type$", "jpetstore.PUBLIC.category");
            worms.put("catid", "VERMI");
            worms.put("name", "worms");
            worms.put("descn",
          "<image src=\"../images/worms_icon.gif\"><font size=\"5\" color=\"blue\">" +
                      "Worms</font>");

            Session session = application.getSession("jpetstore");
            session.save("hibernatetest_PUBLIC_category", worms);
            session.getTransaction().commit();
            session.beginTransaction();
            session.delete("hibernatetest_PUBLIC_category", worms);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }

    public void testDeleteCategoriaAndCascadedProducys() {
        try {
            HashMap birdsPk = new HashMap();
            birdsPk.put("catid", "BIRDS");
            Session session = application.getSession("jpetstore");
            Map<String, Object> birdCat = (Map<String, Object>) SessionUtils.getObjectByPk(application, "jpetstore.PUBLIC.category", birdsPk);
            assertEquals(16, SessionUtils.getAllObjects(application, "jpetstore.PUBLIC.product").size());

            session.delete("jpetstore_PUBLIC_category", birdCat);

            //Perdo i due prodotti associati alla categoria Birds
            assertEquals(14, SessionUtils.getAllObjects(application, "jpetstore.PUBLIC.product").size());


            //test commit globale
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }

        public void testSpaccaSession(){

        try {
            Session session = application.getSession("jpetstore");
            //Inserisco un valore troppo grande per email
            // e pwd in modo tale da rompere la insert
            User user = new User();
            user.setEmail("123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890");
            user.setPwd("123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890" +
            "123456789012345678901234567890123456789012345678901234567890");

            session.save("portofino_public_users", user);
            session.getTransaction().commit();

            fail();
        } catch (Exception e) {
            //corretto
        }
        application.closeSessions();

        //Faccio una seconda operazione
        try{
            List<Object> users= SessionUtils.getAllObjects(application, PORTOFINO_PUBLIC_USER);
            assertNotNull(users);
        }catch (Exception e){
            e.printStackTrace();
            fail("La sessione è spaccata");
        }
    }

    public void testGetObjByPk(){
        //Test Chiave singola
        HashMap<String, Object> pk = new HashMap<String, Object>();
        pk.put("catid", "BIRDS");
        Object bird =  SessionUtils.getObjectByPk
                (application, "jpetstore.PUBLIC.category", pk);
        assertEquals("Birds", ((MapProxy) bird).get("name"));

        //Test Chiave composta
        pk = new HashMap<String, Object>();
        pk.put("orderid", 1);
        pk.put("linenum", 1);
        Map lineItem = (Map) SessionUtils.getObjectByPk
                (application, "jpetstore.PUBLIC.lineitem", pk);
        assertEquals("EST-1", lineItem.get("itemid"));
    }

    public void testGetRelatedObjects(){
        HashMap<String, Object> pk = new HashMap<String, Object>();
        pk.put("catid", "BIRDS");
        Object bird = SessionUtils.getObjectByPk
                (application, "jpetstore.PUBLIC.category", pk);
        assertEquals("Birds", ((MapProxy) bird).get("name"));

        List objs = SessionUtils.getRelatedObjects(application, "jpetstore.PUBLIC.category",
                bird, "fk_product_1");
        assertTrue(objs.size()>0);
    }

    public void testFkComposite(){
        List<Object> list1 =
                SessionUtils.getAllObjects(application, "hibernatetest.PUBLIC.table1");
        List<Object> list2 =
                SessionUtils.getAllObjects(application, "hibernatetest.PUBLIC.table2");
        HashMap map = (HashMap)list2.get(0);
        List obj =  (List) map.get("fk_tb_2");
        assertNotNull(obj);
        assertTrue(obj.size()>0);
        Map obj2 = (Map) ((Map)obj.get(0)).get("fk_tb_2");
        assertNotNull(obj2);
        assertEquals(5, obj2.keySet().size());
        List<Object> list3 =
                SessionUtils.getAllObjects(application, "hibernatetest.PUBLIC.table3");


        List<Object> listu =
                SessionUtils.getAllObjects(application, PORTOFINO_PUBLIC_USER);
        List<Object> listg =
                SessionUtils.getAllObjects(application, "portofino.public.groups");
        List<Object> listug =
                SessionUtils.getAllObjects(application, "portofino.public.users_groups");
    }
}




