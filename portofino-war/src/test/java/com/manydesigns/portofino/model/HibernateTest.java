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

import com.manydesigns.portofino.context.MDContext;
import com.manydesigns.portofino.context.hibernate.MDContextHibernateImpl;
import junit.framework.TestCase;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Angelo    Lupo       - angelo.lupo@manydesigns.com
 * @author Paolo     Predonzani - paolo.predonzani@manydesigns.com
 */
public class HibernateTest extends TestCase {
    MDContext context;

    public void setUp() {
        context = new MDContextHibernateImpl();
        context.loadXmlModelAsResource(
                "databases/jpetstore/postgresql/jpetstore-postgres.xml");
        context.openSession();
    }


    public void tearDown() {
        context.closeSession();
    }

    public void testReadProdotti() {
        List<Map<String, Object>> resultProd =
                context.getAllObjects("jpetstore.public.product");

        int sizePrd = resultProd.size();
        assertEquals("prodotti", 16, sizePrd);

    }

    public void testReadCategorieProdotti() {
        List<Map<String, Object>> resultCat =
                context.getAllObjects("jpetstore.public.category");

        int sizeCat = resultCat.size();
        assertEquals("categorie", 5, sizeCat);


        Map categoria0 = resultCat.get(0);
        assertEquals("jpetstore.public.category", categoria0.get("$type$"));
        assertEquals("Fish", categoria0.get("name"));
        Map categoria1 = resultCat.get(1);
        assertEquals("Dogs", categoria1.get("name"));
        Map categoria2 = resultCat.get(2);
        assertEquals("Reptiles", categoria2.get("name"));
        Map categoria3 = resultCat.get(3);
        assertEquals("Cats", categoria3.get("name"));
        Map categoria4 = resultCat.get(4);
        assertEquals("Birds", categoria4.get("name"));

        List<Map<String, Object>> resultProd =
                context.getAllObjects("jpetstore.public.product");

        int sizePrd = resultProd.size();
        assertEquals("prodotti", sizePrd, 16);
        Map prd0 = resultProd.get(0);
        assertEquals("FI-SW-01", prd0.get("productid") );
        assertEquals("Angelfish", prd0.get("name"));
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
        context.saveObject(worms);
    }

    public void testSaveLineItem() {
        context.openSession();
        Map<String, Object> lineItem = new HashMap<String, Object>();
        lineItem.put("$type$", "jpetstore.public.lineitem");
        lineItem.put("orderid", 1);
        lineItem.put("linenum", 1);
        lineItem.put("itemid",
          "test");
        lineItem.put("quantity", 20);
        lineItem.put("unitprice", new BigDecimal(10.80));

        context.saveObject(lineItem);
    }

   /* public void testGetObjectByPk(){
        String qualifiedTableName="jpetstore.public.category";
        HashMap<String, Object> pk=new HashMap();
        //pk.put("catid", )
        context.openSession();
        @SuppressWarnings({"unchecked"}) Map<String, Object> result =
                (Map<String, Object>)context.getObjectByPk(qualifiedTableName, pk);

    }*/

    public void testSaveTestElement() {
        context.openSession();
        Map<String, Object> lineItem = new HashMap<String, Object>();
        lineItem.put("$type$", "jpetstore.public.test");
        lineItem.put("a", "z");
        context.saveObject(lineItem);
    }


    public void testDeleteCategoria() {
        context.openSession();
        Map<String, Object> worms = new HashMap<String, Object>();
        worms.put("$type$", "jpetstore.public.category");
        worms.put("catid", "VERMI");
        worms.put("name", "worms");
        worms.put("descn",
          "<image src=\"../images/worms_icon.gif\"><font size=\"5\" color=\"blue\">" +
                  "Worms</font>");
        context.deleteObject(worms);
    }
}




