/*
 * Copyright (C) 2005-2009 ManyDesigns srl.  All rights reserved.
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
package com.manydesigns.portofino.model;

import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.portofino.context.Context;
import com.manydesigns.portofino.context.hibernate.HibernateContextImpl;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.users.Group;
import com.manydesigns.portofino.users.User;
import com.manydesigns.portofino.users.UsersGroups;
import junit.framework.TestCase;
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
public class HibernateTest extends TestCase {
    Context context;

    public void setUp() {
        context = new HibernateContextImpl();
        context.loadConnectionsAsResource("portofino-connections.xml");
        context.loadXmlModelAsResource(
                "databases/jpetstore/postgresql/jpetstore-postgres.xml");
        context.openSession();
    }


    public void tearDown() {
        context.closeSession();
    }

    public void testReadProdotti() {
        List<Object> resultProd =
                context.getAllObjects("jpetstore.public.product");
        int sizePrd = resultProd.size();
        assertTrue("prodotti", sizePrd>=16);

    }

    public void testUsers() {
        List<Object> groupList =
                context.getAllObjects("portofino.public.group_");
        assertEquals( 2, groupList.size());

        List<Object> usergroups =
                context.getAllObjects("portofino.public.users_groups");
        assertEquals( 3, usergroups.size());

        List<Object> users =
                context.getAllObjects("portofino.public.user_");
        assertEquals("numero utenti", 2, users.size());
        User admin = (User) users.get(0);
        List<UsersGroups> groups = admin.getGroups();
        assertEquals("numero gruppi per admin", 1, groups.size());
        UsersGroups ug1 = groups.get(0);
        Group g1 = ug1.getGroup();
        assertNotNull(g1);

    }
    public void testSearchAndReadCategorieProdotti() {
        List<Object> resultCat =
                context.getAllObjects("jpetstore.public.category");

        int sizeCat = resultCat.size();
        assertTrue("categorie", sizeCat>0);


        Map categoria0 = (Map<String, Object>) resultCat.get(0);
        assertEquals("jpetstore.public.category", categoria0.get("$type$"));
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
                context.getAllObjects("jpetstore.public.product");

        Table table = context.getModel()
                .findTableByQualifiedName("jpetstore.public.category");
        TableAccessor tableAccessor = new TableAccessor(table);
        Criteria criteria = new Criteria(tableAccessor);
        HashMap<String, String> category= findCategory(tableAccessor, criteria);

        int sizePrd = resultProd.size();
        assertTrue("prodotti", sizePrd >= 16);
        Map prd0 = (Map<String, Object>)resultProd.get(0);
        assertEquals("FI-SW-01", prd0.get("productid") );
        assertEquals("Angelfish", prd0.get("name"));
    }

    private HashMap<String, String> findCategory(TableAccessor tableAccessor, Criteria criteria) {
        HashMap<String, String> category=null;
        try {
            criteria.eq(tableAccessor.getProperty("catid"), "FISH");
            List<Object> listObjs = context.getObjects(criteria);
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
        context.openSession();
        Table table = context.getModel()
                .findTableByQualifiedName("jpetstore.public.category");
        TableAccessor tableAccessor = new TableAccessor(table);
        Criteria criteria = new Criteria(tableAccessor);

        List<Object> resultCat =
                context.getAllObjects("jpetstore.public.category");
        int sizeCat = resultCat.size();
        assertTrue("categorie", sizeCat>0);
        Map<String, String> categoria0 =  findCategory(tableAccessor, criteria);
        assertEquals("jpetstore.public.category", categoria0.get("$type$"));
        assertEquals("Fish", categoria0.get("name"));
        categoria0.put("name", "Pesciu");
        context.updateObject("jpetstore.public.category", categoria0);
        context.commit("jpetstore");
        context.closeSession();

        //Controllo l'aggiornamento e riporto le cose come stavano
        context.openSession();
        criteria = new Criteria(tableAccessor);
        categoria0 =  findCategory(tableAccessor, criteria);
        assertEquals("jpetstore.public.category", categoria0.get("$type$"));
        assertEquals("Pesciu", categoria0.get("name"));
        categoria0.put("name", "Fish");
        context.saveOrUpdateObject("jpetstore.public.category", categoria0);
        context.commit("jpetstore");
        context.closeSession();
    }

    public void testSaveCategoria() {
        context.openSession();
        Map<String, Object> worms = new HashMap<String, Object>();
        worms.put("$type$", "jpetstore.public.category");
        worms.put("catid", "VERMI");
        worms.put("name", "worms");
        worms.put("descn",
          "<image src=\"../images/worms_icon.gif\"><font size=\"5\" color=\"blue\">" +
                  "Worms</font>");
        context.saveObject("jpetstore.public.category",worms);
        context.commit("jpetstore");
    }

    public void testSaveLineItem() {
        context.openSession();
        Map<String, Object> lineItem = new HashMap<String, Object>();
        lineItem.put("$type$", "jpetstore.public.lineitem");
        lineItem.put("orderid", 2);
        lineItem.put("linenum", 2);
        lineItem.put("itemid",
          "test");
        lineItem.put("quantity", 20);
        lineItem.put("unitprice", new BigDecimal(10.80));

        context.saveObject("jpetstore.public.lineitem", lineItem);
        context.commit("jpetstore");
        context.closeSession();

        //e ora cancello
        context.openSession();
        try {
            context.deleteObject("jpetstore.public.lineitem", lineItem);
            context.commit("jpetstore");
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail();
        }
        context.closeSession();
    }


    public void testSaveTestElement() {
        try {
            context.openSession();
            Map<String, Object> testItem = new HashMap<String, Object>();
            testItem.put("$type$", "hibernatetest.public.table1");
            testItem.put("testo", "esempio");
            //salvo
            context.saveObject("hibernatetest.public.table1", testItem);
            context.commit("hibernatetest");

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testDeleteCategoria() {
        try {
            context.openSession();
            Map<String, Object> worms = new HashMap<String, Object>();
            worms.put("$type$", "jpetstore.public.category");
            worms.put("catid", "VERMI");
            worms.put("name", "worms");
            worms.put("descn",
          "<image src=\"../images/worms_icon.gif\"><font size=\"5\" color=\"blue\">" +
                      "Worms</font>");
            context.saveOrUpdateObject("jpetstore.public.category", worms);
            context.commit("jpetstore");
            context.deleteObject("jpetstore.public.category", worms);
            //test commit globale
            context.commit();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }

        public void testSpaccaSession(){
        context.openSession();

        try {
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

            context.saveObject("portofino.public.user_", user);
            context.commit("portofino");

            fail();
        } catch (Exception e) {
            //corretto
        }
        context.closeSession();
        context.openSession();

        //Faccio una seconda operazione
        try{
            List<Object> users= context.getAllObjects("portofino.public.user_");
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
        Object bird =  context.getObjectByPk
                ("jpetstore.public.category", pk);
        assertEquals("Birds", ((MapProxy) bird).get("name"));

        //Test Chiave composta
        pk = new HashMap<String, Object>();
        pk.put("orderid", 1);
        pk.put("linenum", 1);
        Map lineItem = (Map) context.getObjectByPk
                ("jpetstore.public.lineitem", pk);
        assertEquals("test2", lineItem.get("itemid"));
    }

    public void testGetRelatedObjects(){
        HashMap<String, Object> pk = new HashMap<String, Object>();
        pk.put("catid", "BIRDS");
        Object bird = context.getObjectByPk
                ("jpetstore.public.category", pk);
        assertEquals("Birds", ((MapProxy) bird).get("name"));

        List objs = context.getRelatedObjects("jpetstore.public.category",
                bird, "fk_product_1");
        assertTrue(objs.size()>0);
    }

    public void testFkComposite(){
        List<Object> list1 =
                context.getAllObjects("hibernatetest.public.table1");
        List<Object> list2 =
                context.getAllObjects("hibernatetest.public.table2");
        HashMap map = (HashMap)list2.get(0);
        List obj =  (List) map.get("fk_tb_2");
        assertNotNull(obj);
        assertTrue(obj.size()>0);
        Map obj2 = (Map) ((Map)obj.get(0)).get("fk_tb_2");
        assertNotNull(obj2);
        assertEquals(5, obj2.keySet().size());
        List<Object> list3 =
                context.getAllObjects("hibernatetest.public.table3");


        List<Object> listu =
                context.getAllObjects("portofino.public.user_");
        List<Object> listg =
                context.getAllObjects("portofino.public.group_");
        List<Object> listug =
                context.getAllObjects("portofino.public.users_groups");
    }

}




