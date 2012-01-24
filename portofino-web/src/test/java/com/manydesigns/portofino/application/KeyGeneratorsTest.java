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
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.system.model.users.Group;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class KeyGeneratorsTest extends AbstractPortofinoTest {

    Model model;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        model = application.getModel();
    }

    public void tearDown() {
        application.closeSessions();
    }

    public void testAutoIncrementGenerator(){
        Map<String, Object> supplier = new HashMap<String, Object>();
        String supplierTable = "jpetstore.PUBLIC.supplier";
        String supplierEntity = "jpetstore_public_supplier";
        supplier.put("$type$", supplierEntity);
        supplier.put("status", "99");
        supplier.put("name", "Giampiero");
        Session session = application.getSession("jpetstore");
        session.save(supplierEntity, supplier);
        session.getTransaction().commit();
        Table table = DataModelLogic.findTableByQualifiedName(
                model, supplierTable);
        TableAccessor tableAccessor = new TableAccessor(table);
        TableCriteria criteria = new TableCriteria(table);
        final int expectedId = 3;
        try {
            criteria.eq(tableAccessor.getProperty("suppid"), expectedId);
            List listObjs = QueryUtils.getObjects(session, criteria, null, null);
            assertEquals(1, listObjs.size());
            Map<String,String> supp = (Map<String, String>) listObjs.get(0);
            String name = supp.get("name");
            assertEquals("Giampiero", name);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testSequenceGenerator(){
        Map<String, Object> supplier = new HashMap<String, Object>();
        final String testTable = "hibernatetest.PUBLIC.test";
        final String testEntity = "hibernatetest_public_test";
        supplier.put("$type$", testEntity);
        Session session = application.getSession("hibernatetest");
        session.save(testEntity, supplier);
        session.getTransaction().commit();
        Table table = DataModelLogic.findTableByQualifiedName(
                model, testTable);
        TableAccessor tableAccessor = new TableAccessor(table);
        TableCriteria criteria = new TableCriteria(table);
        final long expectedId = 1;
        try {
            criteria.eq(tableAccessor.getProperty("id"), expectedId);
            List listObjs = QueryUtils.getObjects(session, criteria, null, null);
            assertEquals(1, listObjs.size());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testTableGenerator(){
        Map<String, Object> order = new HashMap<String, Object>();
        final int expectedId = 1000;
        final String ordersTable = "jpetstore.PUBLIC.orders";
        String ordersEntity = "jpetstore_public_orders";
        order.put("$type$", ordersEntity);
        order.put("userid", "99");
        order.put("orderdate", new Date());
        order.put("shipaddr1", "99");
        order.put("shipaddr2", "99");
        order.put("shipcity", "99");
        order.put("shipstate", "99");
        order.put("shipzip", "99");
        order.put("shipcountry", "99");
        order.put("billaddr1", "99");
        order.put("billaddr2", "99");
        order.put("billcity", "99");
        order.put("billstate", "99");
        order.put("billzip", "99");
        order.put("billcountry", "99");
        order.put("courier", "99");
        order.put("totalprice", new BigDecimal(99L));
        order.put("billtofirstname", "99");
        order.put("billtolastname", "99");
        order.put("shiptofirstname", "99");
        order.put("shiptolastname", "99");
        order.put("shipcountry", "99");
        order.put("creditcard", "99");
        order.put("exprdate", "99");
        order.put("cardtype", "99");
        order.put("locale", "99");
        Session session = application.getSession("jpetstore");
        session.save(ordersEntity ,order);
        QueryUtils.commit(application, "jpetstore");
        Table table = DataModelLogic.findTableByQualifiedName(
                model, ordersTable);
        TableAccessor tableAccessor = new TableAccessor(table);
        TableCriteria criteria = new TableCriteria(table);
        try {
            criteria.eq(tableAccessor.getProperty("orderid"), expectedId);
            List listObjs = QueryUtils.getObjects(session, criteria, null, null);
            assertEquals(1, listObjs.size());
            Map<String,String> supp = (Map<String, String>) listObjs.get(0);
            String name = supp.get("userid");
            assertEquals("99", name);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            fail();
        }

    }

    public void testIdentityGenerator(){
        Group myGroup = new Group();
        myGroup.setCreationDate(new Timestamp(new Date().getTime()));
        myGroup.setName("testGroup");
        myGroup.setDescription("this is a description");
        Session session = application.getSystemSession();
        session.save("groups", myGroup);
        session.getTransaction().commit();

        Table table = DataModelLogic.findTableByEntityName(
                application.getSystemDatabase(), DataModelLogic.GROUP_ENTITY_NAME);
        TableAccessor tableAccessor = new TableAccessor(table);
        TableCriteria criteria = new TableCriteria(table);
        final long expectedId = 3L;
        try {
            criteria.eq(tableAccessor.getProperty("groupId"), expectedId);
            List<Object> listObjs = QueryUtils.getObjects(session, criteria, null, null);
            assertEquals(1, listObjs.size());
            myGroup = (Group) listObjs.get(0);
            String name = myGroup.getName();
            assertEquals("testGroup", name);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            fail();
        }
    }
}
