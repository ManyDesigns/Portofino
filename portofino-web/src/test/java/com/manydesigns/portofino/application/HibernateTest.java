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
import com.manydesigns.portofino.database.TableCriteria;
import com.manydesigns.portofino.model.DataModelLogic;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.UsersGroups;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.proxy.map.MapProxy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Angelo    Lupo       - angelo.lupo@manydesigns.com
 * @author Paolo     Predonzani - paolo.predonzani@manydesigns.com
 */
public class HibernateTest extends AbstractPortofinoTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() {
        application.closeSessions();
    }

    public void testReadProdotti() {
        Session session = application.getSession("jpetstore");
        Criteria criteria = session.createCriteria("product");
        List resultProd = new ArrayList(criteria.list());

        int sizePrd = resultProd.size();
        assertEquals("prodotti", 16, sizePrd);
    }

    public void testUsers() {
        Session session = application.getSystemSession();
        Criteria criteria = session.createCriteria(DataModelLogic.GROUP_ENTITY_NAME);
        List<Group> groupList = new ArrayList(criteria.list());
        assertEquals( 2, groupList.size());

        criteria = session.createCriteria(UsersGroups.class);
        List<UsersGroups> usergroups = new ArrayList(criteria.list());
        assertEquals( 3, usergroups.size());

        criteria = session.createCriteria(DataModelLogic.USER_ENTITY_NAME);
        List<User> users = new ArrayList(criteria.list());

        assertEquals("numero utenti", 2, users.size());
        User admin = users.get(0);
        List<UsersGroups> groups = admin.getGroups();
        assertEquals("numero gruppi per admin",  2, groups.size());
        

    }
    public void testSearchAndReadCategorieProdotti() {
        Session session = application.getSession("jpetstore");
        Criteria criteria = session.createCriteria("category");
        List resultCat = new ArrayList(criteria.list());


        int sizeCat = resultCat.size();
        assertEquals("categorie", 5, sizeCat);


        Map categoria0 = (Map<String, Object>) resultCat.get(0);
        assertEquals("category", categoria0.get("$type$"));
        assertNotNull(categoria0.get("name"));
        Map categoria1 = (Map<String, Object>)resultCat.get(1);
        assertNotNull(categoria0.get("name"));
        Map categoria2 = (Map<String, Object>)resultCat.get(2);
        assertNotNull(categoria0.get("name"));
        Map categoria3 = (Map<String, Object>)resultCat.get(3);
        assertNotNull(categoria0.get("name"));
        Map categoria4 = (Map<String, Object>)resultCat.get(4);
        assertNotNull(categoria0.get("name"));

        criteria = session.createCriteria("product");
        List resultProd = new ArrayList(criteria.list());

        Table table = DataModelLogic.findTableByQualifiedName(
                application.getModel(), "jpetstore.PUBLIC.category");
        TableAccessor tableAccessor = new TableAccessor(table);
        TableCriteria tableCriteria = new TableCriteria(table);
        HashMap<String, String> category= findCategory(tableAccessor, tableCriteria);

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
            List<Object> listObjs = QueryUtils.getObjects(session, criteria, null, null);
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
        TableCriteria tableCriteria = new TableCriteria(table);

        Session session = application.getSession("jpetstore");
        Criteria criteria = session.createCriteria("category");
        List resultCat = new ArrayList(criteria.list());

        int sizeCat = resultCat.size();
        assertEquals("categorie", 5, sizeCat);
        Map<String, String> categoria0 =  findCategory(tableAccessor, tableCriteria);
        assertEquals("category", categoria0.get("$type$"));
        assertEquals("Fish", categoria0.get("name"));
        categoria0.put("name", "Pesciu");
        session.update("category", categoria0);
        session.getTransaction().commit();
        application.closeSessions();

        //Controllo l'aggiornamento e riporto le cose come stavano
        tableCriteria = new TableCriteria(table);
        categoria0 =  findCategory(tableAccessor, tableCriteria);
        assertEquals("category", categoria0.get("$type$"));
        assertEquals("Pesciu", categoria0.get("name"));
        categoria0.put("name", "Fish");
        session = application.getSession("jpetstore");
        session.update("category", categoria0);
        session.getTransaction().commit();
        application.closeSessions();
    }

    public void testSaveCategoria() {
        Map<String, Object> worms = new HashMap<String, Object>();
        worms.put("$type$", "category");
        worms.put("catid", "VERMI");
        worms.put("name", "worms");
        worms.put("descn",
          "<image src=\"../images/worms_icon.gif\"><font size=\"5\" color=\"blue\">" +
                  "Worms</font>");

        Session session = application.getSession("jpetstore");
        session.save("category", worms);
        session.getTransaction().commit();
    }

    public void testSaveLineItem() {
        Map<String, Object> lineItem = new HashMap<String, Object>();
        lineItem.put("$type$", "lineitem");
        lineItem.put("orderid", 2);
        lineItem.put("linenum", 2);
        lineItem.put("itemid",
          "test");
        lineItem.put("quantity", 20);
        lineItem.put("unitprice", new BigDecimal(10.80));

        Session session = application.getSession("jpetstore");
        session.save("lineitem", lineItem);
        session.getTransaction().commit();

        application.closeSessions();

        //e ora cancello
        try {
            session = application.getSession("jpetstore");
            session.delete("lineitem", lineItem);
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
            testItem.put("$type$", "table1");
            testItem.put("testo", "esempio");
            //salvo
            Session session = application.getSession("hibernatetest");
            session.save("table1", testItem);
            session.getTransaction().commit();

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testDeleteCategoria() {
        try {
            Map<String, Object> worms = new HashMap<String, Object>();
            worms.put("$type$", "category");
            worms.put("catid", "VERMI");
            worms.put("name", "worms");
            worms.put("descn",
          "<image src=\"../images/worms_icon.gif\"><font size=\"5\" color=\"blue\">" +
                      "Worms</font>");

            Session session = application.getSession("jpetstore");
            session.save("category", worms);
            session.getTransaction().commit();
            session.beginTransaction();
            session.delete("category", worms);
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
            Map<String, Object> birdCat =
                    (Map<String, Object>) QueryUtils.getObjectByPk
                            (application, "jpetstore", "category", birdsPk);

            assertEquals(16, session.createCriteria("product").list().size());

            session.delete("category", birdCat);

            //Perdo i due prodotti associati alla categoria Birds
            assertEquals(14, session.createCriteria("product").list().size());


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

            session.save("users", user);
            session.getTransaction().commit();

            fail();
        } catch (Exception e) {
            //corretto
        }
        application.closeSessions();

        //Faccio una seconda operazione
        try {
            Session session = application.getSystemSession();
            Criteria criteria = session.createCriteria(DataModelLogic.USER_ENTITY_NAME);
            List<Object> users = criteria.list();
            assertNotNull(users);
        } catch (Exception e){
            e.printStackTrace();
            fail("La sessione è spaccata");
        }
    }

    public void testGetObjByPk(){
        //Test Chiave singola
        HashMap<String, Object> pk = new HashMap<String, Object>();
        pk.put("catid", "BIRDS");
        Object bird =  QueryUtils.getObjectByPk
                (application, "jpetstore", "category", pk);
        assertEquals("Birds", ((MapProxy) bird).get("name"));

        //Test Chiave composta
        pk = new HashMap<String, Object>();
        pk.put("orderid", 1);
        pk.put("linenum", 1);
        Map lineItem = (Map) QueryUtils.getObjectByPk
                (application, "jpetstore", "lineitem", pk);
        assertEquals("EST-1", lineItem.get("itemid"));
    }

    public void testGetRelatedObjects(){
        HashMap<String, Object> pk = new HashMap<String, Object>();
        pk.put("catid", "BIRDS");
        Object bird = QueryUtils.getObjectByPk
                (application, "jpetstore", "category", pk);
        assertEquals("Birds", ((MapProxy) bird).get("name"));

        List objs = QueryUtils.getRelatedObjects(application, "jpetstore", "category",
                bird, "fk_product_1");
        assertTrue(objs.size()>0);
    }

    public void testFkComposite(){
        Session session = application.getSession("hibernatetest");
        List<Object> list1 = session.createCriteria("table1").list();
        List<Object> list2 = session.createCriteria("table2").list();
        HashMap map = (HashMap)list2.get(0);
        List obj =  (List) map.get("fk_tb_2");
        assertNotNull(obj);
        assertTrue(obj.size()>0);
        Map obj2 = (Map) ((Map)obj.get(0)).get("fk_tb_2");
        assertNotNull(obj2);
        assertEquals(5, obj2.keySet().size());
        List<Object> list3 = session.createCriteria("table3").list();


        /*List<Object> listu =
                QueryUtils.getAllObjects(application, PORTOFINO_PUBLIC_USER);
        List<Object> listg =
                QueryUtils.getAllObjects(application, "portofino.public.groups");
        List<Object> listug =
                QueryUtils.getAllObjects(application, "portofino.public.users_groups");*/
    }
}




