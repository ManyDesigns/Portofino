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
package com.manydesigns.portofino.model;

import com.manydesigns.portofino.model.site.*;
import com.manydesigns.portofino.system.model.users.Group;
import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class SiteNodeTest extends TestCase{

    Model model;
    RootNode root;
    SiteNode n1_1;
    SiteNode n1_2;
    SiteNode n1_3;
    SiteNode n1_4;
    CustomFolderNode n1_2_1;
    CustomFolderNode n1_2_2;
    SiteNode n1_2_3;

    Permissions n1_1_perm;
    Permissions n1_2_perm;
    Permissions n1_3_perm;
    Permissions n1_4_perm;
    Permissions n1_2_1_perm;
    Permissions n1_2_2_perm;
    Permissions n1_2_3_perm;

    Permissions permissions;
    Permissions permissions2;
    Permissions permissions3;
    List<String> groups;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        model = new Model();
        //1. Radice
        root = new RootNode();
        root.setId("/");
        root.setTitle("portofino4");
        root.setDescription("portofino application");

        //Nessun permesso

        //1.1
        n1_1 = new DocumentNode();
        n1_1.setParent(root);
        n1_1_perm = new Permissions();
        n1_1.setPermissions(n1_1_perm);
        n1_1.setDescription("homepage description");
        n1_1.setTitle("homepage title");
        n1_1.setId("homepage");

        n1_1_perm.getAllow().add(Group.ANONYMOUS);

        //1.2
        n1_2 = new FolderNode();
        n1_2.setParent(root);
        n1_2_perm = new Permissions();
        n1_2.setPermissions(n1_2_perm);
        n1_2.setDescription("Model description");
        n1_2.setTitle("Model title");
        n1_2.setId("model");

        n1_2_perm.getAllow().add(Group.REGISTERED);

        n1_2_1 = new CustomFolderNode();
        n1_2_1.setParent(n1_2);
        n1_2_1_perm = new Permissions();
        n1_2_1.setPermissions(n1_2_1_perm);
        n1_2_1.setType("table-data");
        n1_2_1.setDescription("TableData description");
        n1_2_1.setTitle("TableData title");
        n1_2_1.setId("TableData");
        n1_2_1.setUrl("/model/TableData.action");
        n1_2.getChildNodes().add(n1_2_1);

        n1_2_1_perm.getAllow().add("admins");

        n1_2_2 = new CustomFolderNode();
        n1_2_2.setParent(n1_2);
        n1_2_2_perm = new Permissions();
        n1_2_2.setPermissions(n1_2_2_perm);
        n1_2_2.setType("table-design");
        n1_2_2.setDescription("TableData design description");
        n1_2_2.setTitle("TableData design title");
        n1_2_2.setId("TableDesign");
        n1_2_2.setUrl("/model/TableDesign.action");
        n1_2.getChildNodes().add(n1_2_2);



        n1_2_3 = new CustomNode();
        n1_2_3.setParent(n1_2);
        n1_2_3_perm = new Permissions();
        n1_2_3.setPermissions(n1_2_3_perm);
        n1_2_3.setDescription("Somewhere description");
        n1_2_3.setTitle("Somewhere");
        n1_2_3.setId("somewhere");
        n1_2_3.setUrl("http://www.manydesigns.com/");
        n1_2.getChildNodes().add(n1_2_3);

        n1_2_3_perm.getDeny().add("cattivi");

        //1.3
        n1_3 = new CustomNode();
        n1_3.setParent(root);
        n1_3_perm = new Permissions();
        n1_3.setPermissions(n1_3_perm);
        n1_3.setDescription("Profile");
        n1_3.setTitle("Profile");
        n1_3.setId("Profile");
        n1_3.setUrl("/Profile.action");

        n1_3_perm.getDeny().add("cattivi");
        n1_3_perm.getAllow().add("buoni");

        //1.4
        n1_4 = new FolderNode();
        n1_4.setParent(root);
        n1_4_perm = new Permissions();
        n1_4.setPermissions(n1_4_perm);
        n1_4.setDescription("user administration");
        n1_4.setTitle("user admin");
        n1_4.setId("userAdmin");
        //Aggiungo i nodi alla radice
        root.getChildNodes().add(n1_1);
        root.getChildNodes().add(n1_2);
        root.getChildNodes().add(n1_3);
        root.getChildNodes().add(n1_4);

        model.setRootNode(root);


        root.reset();
        root.init(model);


        permissions = new Permissions();
        permissions.getAllow().add("buoni");
        permissions.getDeny().add("cattivi");

        permissions2 = new Permissions();
        permissions2.getDeny().add("cattivi");

        permissions3 = new Permissions();
        permissions3.getAllow().add("buoni");

        groups = new ArrayList<String>();

    }

    public void testSiteNodes (){
        assertEquals("/", root.getActualUrl());
        assertEquals("/", root.getActualId());

        assertEquals("/homepage/Document.action", n1_1.getActualUrl());
        assertEquals("/homepage", n1_1.getActualId());

        assertEquals("/model/Index.action", n1_2.getActualUrl());
        assertEquals("/model", n1_2.getActualId());

        assertEquals("/model/TableData.action", n1_2_1.getActualUrl());
        assertEquals("/model/TableData", n1_2_1.getActualId());

        assertEquals("/model/TableDesign.action", n1_2_2.getActualUrl());
        assertEquals("/model/TableDesign", n1_2_2.getActualId());

        assertEquals("http://www.manydesigns.com/", n1_2_3.getActualUrl());
        assertEquals("/model/somewhere", n1_2_3.getActualId());

        assertEquals("/Profile.action", n1_3.getActualUrl());
        assertEquals("/Profile", n1_3.getActualId());
        assertEquals("/userAdmin/Index.action", n1_4.getActualUrl());
        assertEquals("/userAdmin", n1_4.getActualId());
    }





    public void testAnonymous() {
        groups.add(Group.ANONYMOUS);

        assertTrue(root.isAllowed(groups));
        assertTrue(n1_1.isAllowed(groups));
        assertFalse(n1_2.isAllowed(groups));

        assertFalse(n1_2_1.isAllowed(groups));
        assertFalse(n1_2_2.isAllowed(groups));
        assertFalse(n1_2_3.isAllowed(groups));

        assertFalse(n1_3.isAllowed(groups));
        assertTrue(n1_4.isAllowed(groups));
    }

    public void testRegistered() {
        groups.add(Group.ANONYMOUS);
        groups.add(Group.REGISTERED);

        assertTrue(root.isAllowed(groups));
        assertTrue(n1_1.isAllowed(groups));
        assertTrue(n1_2.isAllowed(groups));

        assertFalse(n1_2_1.isAllowed(groups));
        assertTrue(n1_2_2.isAllowed(groups));
        assertTrue(n1_2_3.isAllowed(groups));

        assertFalse(n1_3.isAllowed(groups));
        assertTrue(n1_4.isAllowed(groups));
    }

    public void testAdmins() {
        groups.add(Group.ANONYMOUS);
        groups.add(Group.REGISTERED);
        groups.add("admins");

        assertTrue(root.isAllowed(groups));
        assertTrue(n1_1.isAllowed(groups));
        assertTrue(n1_2.isAllowed(groups));

        //allow solo admins
        assertTrue(n1_2_1.isAllowed(groups));

        assertTrue(n1_2_2.isAllowed(groups));
        assertTrue(n1_2_3.isAllowed(groups));

        assertFalse(n1_3.isAllowed(groups));
        assertTrue(n1_4.isAllowed(groups));
    }

    public void testCattivi() {
        groups.add(Group.ANONYMOUS);
        groups.add(Group.REGISTERED);
        groups.add("cattivi");

        assertTrue(root.isAllowed(groups));
        assertTrue(n1_1.isAllowed(groups));
        assertTrue(n1_2.isAllowed(groups));

        assertFalse(n1_2_1.isAllowed(groups));
        assertTrue(n1_2_2.isAllowed(groups));

        //deny
        assertFalse(n1_2_3.isAllowed(groups));

        assertFalse(n1_3.isAllowed(groups));
        assertTrue(n1_4.isAllowed(groups));
    }

    public void testBuoni() {
        groups.add(Group.ANONYMOUS);
        groups.add(Group.REGISTERED);
        groups.add("buoni");

        assertTrue(root.isAllowed(groups));
        assertTrue(n1_1.isAllowed(groups));
        assertTrue(n1_2.isAllowed(groups));

        assertFalse(n1_2_1.isAllowed(groups));
        assertTrue(n1_2_2.isAllowed(groups));
        assertTrue(n1_2_3.isAllowed(groups));

        //Vede questo in più di un registered
        assertTrue(n1_3.isAllowed(groups));
        assertTrue(n1_4.isAllowed(groups));
    }

    public void testBuoniCattivi() {
        groups.add(Group.ANONYMOUS);
        groups.add(Group.REGISTERED);
        groups.add("buoni");
        groups.add("cattivi");

        assertTrue(root.isAllowed(groups));
        assertTrue(n1_1.isAllowed(groups));
        assertTrue(n1_2.isAllowed(groups));

        assertFalse(n1_2_1.isAllowed(groups));
        assertTrue(n1_2_2.isAllowed(groups));
        
        //deny su cattivi
        assertFalse(n1_2_3.isAllowed(groups));

        //Pue essendo buono, essendo anche cattivo non vede il nodo 1.3
        assertFalse(n1_3.isAllowed(groups));
        assertTrue(n1_4.isAllowed(groups));
    }

    // test su permissions (con liste allow e deny entrambe riempite)
    public void testPermissions1_1() {
        assertFalse(permissions.isAllowed(groups));
    }

    public void testPermissions1_2() {
        groups.add("buoni");
        assertTrue(permissions.isAllowed(groups));
    }

    public void testPermissions1_3() {
        groups.add("cattivi");
        assertFalse(permissions.isAllowed(groups));
    }

    public void testPermissions1_4() {
        groups.add("buoni");
        groups.add("cattivi");
        assertFalse(permissions.isAllowed(groups));
    }

    public void testPermissions1_5() {
        groups.add("altro");
        assertFalse(permissions.isAllowed(groups));
    }



    // test su permissions (con solo lista deny riempita)
    public void testPermissions2_1() {
        assertTrue(permissions2.isAllowed(groups));
    }

    public void testPermissions2_3() {
        groups.add("cattivi");
        assertFalse(permissions2.isAllowed(groups));
    }

    public void testPermissions2_5() {
        groups.add("altro");
        assertTrue(permissions2.isAllowed(groups));
    }


    
    // test su permissions (con solo lista allow riempita)
    public void testPermissions3_1() {
        assertFalse(permissions3.isAllowed(groups));
    }

    public void testPermissions3_2() {
        groups.add("buoni");
        assertTrue(permissions3.isAllowed(groups));
    }

    public void testPermissions3_5() {
        groups.add("altro");
        assertFalse(permissions3.isAllowed(groups));
    }



}
