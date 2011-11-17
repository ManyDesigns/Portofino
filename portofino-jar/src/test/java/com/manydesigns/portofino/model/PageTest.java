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
package com.manydesigns.portofino.model;

import com.manydesigns.portofino.model.pages.*;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PageTest extends TestCase {

    Model model;
    RootPage root;
    Page n1_1;
    Page n1_2;
    Page n1_3;
    Page n1_4;
    CustomFolderPage n1_2_1;
    CustomFolderPage n1_2_2;
    Page n1_2_3;

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
        root = new RootPage();
        root.setFragment("/");
        root.setTitle("portofino4");
        root.setDescription("portofino application");

        //Nessun permesso

        //1.1
        n1_1 = new TextPage();
        n1_1.setParent(root);
        n1_1_perm = new Permissions();
        n1_1.setPermissions(n1_1_perm);
        n1_1_perm.setParent(n1_1);
        n1_1.setDescription("homepage description");
        n1_1.setTitle("homepage title");
        n1_1.setFragment("homepage");

        n1_1_perm.getView().add("anonymous");

        //1.2
        n1_2 = new FolderPage();
        n1_2.setParent(root);
        n1_2_perm = new Permissions();
        n1_2.setPermissions(n1_2_perm);
        n1_2_perm.setParent(n1_2);
        n1_2.setDescription("Model description");
        n1_2.setTitle("Model title");
        n1_2.setFragment("model");

        n1_2_perm.getView().add("registered");

        n1_2_1 = new CustomFolderPage();
        n1_2_1.setParent(n1_2);
        n1_2_1_perm = new Permissions();
        n1_2_1.setPermissions(n1_2_1_perm);
        n1_2_1_perm.setParent(n1_2_1);
        n1_2_1.setType("table-data");
        n1_2_1.setDescription("TableData description");
        n1_2_1.setTitle("TableData title");
        n1_2_1.setFragment("TableData");
        n1_2_1.setUrl("/model/TableData.action");
        n1_2.getChildPages().add(n1_2_1);

        n1_2_1_perm.getView().add("admins");

        n1_2_2 = new CustomFolderPage();
        n1_2_2.setParent(n1_2);
        n1_2_2_perm = new Permissions();
        n1_2_2.setPermissions(n1_2_2_perm);
        n1_2_2_perm.setParent(n1_2_2);
        n1_2_2.setType("table-design");
        n1_2_2.setDescription("TableData design description");
        n1_2_2.setTitle("TableData design title");
        n1_2_2.setFragment("TableDesign");
        n1_2_2.setUrl("/model/TableDesign.action");
        n1_2.getChildPages().add(n1_2_2);



        n1_2_3 = new CustomPage();
        n1_2_3.setParent(n1_2);
        n1_2_3_perm = new Permissions();
        n1_2_3.setPermissions(n1_2_3_perm);
        n1_2_3_perm.setParent(n1_2_3);
        n1_2_3.setDescription("Somewhere description");
        n1_2_3.setTitle("Somewhere");
        n1_2_3.setFragment("somewhere");
        n1_2_3.setUrl("http://www.manydesigns.com/");
        n1_2.getChildPages().add(n1_2_3);

        n1_2_3_perm.getDeny().add("cattivi");

        //1.3
        n1_3 = new CustomPage();
        n1_3.setParent(root);
        n1_3_perm = new Permissions();
        n1_3.setPermissions(n1_3_perm);
        n1_3_perm.setParent(n1_3);
        n1_3.setDescription("Profile");
        n1_3.setTitle("Profile");
        n1_3.setFragment("Profile");
        n1_3.setUrl("/Profile.action");

        n1_3_perm.getDeny().add("cattivi");
        n1_3_perm.getView().add("buoni");

        //1.4
        n1_4 = new FolderPage();
        n1_4.setParent(root);
        n1_4_perm = new Permissions();
        n1_4.setPermissions(n1_4_perm);
        n1_4_perm.setParent(n1_4);
        n1_4.setDescription("user administration");
        n1_4.setTitle("user admin");
        n1_4.setFragment("userAdmin");
        //Aggiungo i nodi alla radice
        root.getChildPages().add(n1_1);
        root.getChildPages().add(n1_2);
        root.getChildPages().add(n1_3);
        root.getChildPages().add(n1_4);

        model.setRootPage(root);


        model.init();


        permissions = new Permissions();
        permissions.getView().add("buoni");
        permissions.getDeny().add("cattivi");
        permissions.init(null);

        permissions2 = new Permissions();
        permissions2.getDeny().add("cattivi");
        permissions2.init(null);

        permissions3 = new Permissions();
        permissions3.getView().add("buoni");
        permissions3.init(null);

        groups = new ArrayList<String>();

    }


    public void testAnonymous() {
        groups.add("anonymous");

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
        groups.add("anonymous");
        groups.add("registered");

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
        groups.add("anonymous");
        groups.add("registered");
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
        groups.add("anonymous");
        groups.add("registered");
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
        groups.add("anonymous");
        groups.add("registered");
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
        groups.add("anonymous");
        groups.add("registered");
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
        assertFalse(permissions.isAllowed(Permissions.VIEW, groups));
    }

    public void testPermissions1_2() {
        groups.add("buoni");
        assertTrue(permissions.isAllowed(Permissions.VIEW, groups));
    }

    public void testPermissions1_3() {
        groups.add("cattivi");
        assertFalse(permissions.isAllowed(Permissions.VIEW, groups));
    }

    public void testPermissions1_4() {
        groups.add("buoni");
        groups.add("cattivi");
        assertFalse(permissions.isAllowed(Permissions.VIEW, groups));
    }

    public void testPermissions1_5() {
        groups.add("altro");
        assertFalse(permissions.isAllowed(Permissions.VIEW, groups));
    }



    // test su permissions (con solo lista deny riempita)
    public void testPermissions2_1() {
        assertTrue(permissions2.isAllowed(Permissions.VIEW, groups));
    }

    public void testPermissions2_3() {
        groups.add("cattivi");
        assertFalse(permissions2.isAllowed(Permissions.VIEW, groups));
    }

    public void testPermissions2_5() {
        groups.add("altro");
        assertTrue(permissions2.isAllowed(Permissions.VIEW, groups));
    }


    
    // test su permissions (con solo lista allow riempita)
    public void testPermissions3_1() {
        assertFalse(permissions3.isAllowed(Permissions.VIEW, groups));
    }

    public void testPermissions3_2() {
        groups.add("buoni");
        assertTrue(permissions3.isAllowed(Permissions.VIEW, groups));
    }

    public void testPermissions3_5() {
        groups.add("altro");
        assertFalse(permissions3.isAllowed(Permissions.VIEW, groups));
    }



}
