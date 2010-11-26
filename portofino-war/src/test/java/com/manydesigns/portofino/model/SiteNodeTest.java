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

import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class SiteNodeTest extends TestCase{

    Permissions permissions;
    List<String> groups;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        permissions = new Permissions();
        permissions.getAllow().add("buoni");
        permissions.getDeny().add("cattivi");
        groups = new ArrayList<String>();
    }

    public void testSiteNodes (){
        Model model = new Model();
        //1. Radice
        RootNode root = new RootNode();
        root.setId("/");
        root.setTitle("portofino4");
        root.setDescription("portofino application");

        //1.1
        DocumentNode n1_1 = new DocumentNode(root);
        n1_1.setDescription("homepage description");
        n1_1.setTitle("homepage title");
        n1_1.setId("homepage");

        //1.2
        FolderNode n1_2 = new FolderNode(root);
        n1_2.setDescription("Model description");
        n1_2.setTitle("Model title");
        n1_2.setId("model");

        CustomFolderNode n1_2_1 = new CustomFolderNode(n1_2);
        n1_2_1.setType("table-data");
        n1_2_1.setDescription("TableData description");
        n1_2_1.setTitle("TableData title");
        n1_2_1.setId("TableData");
        n1_2_1.setUrl("/model/TableData.action");
        n1_2.getChildNodes().add(n1_2_1);

        CustomFolderNode n1_2_2 = new CustomFolderNode(n1_2);
        n1_2_2.setType("table-design");
        n1_2_2.setDescription("TableData design description");
        n1_2_2.setTitle("TableData design title");
        n1_2_2.setId("TableDesign");
        n1_2_2.setUrl("/model/TableDesign.action");
        n1_2.getChildNodes().add(n1_2_2);

        CustomNode n1_2_3 = new CustomNode(n1_2);
        n1_2_3.setDescription("Somewhere description");
        n1_2_3.setTitle("Somewhere");
        n1_2_3.setId("somewhere");
        n1_2_3.setUrl("http://www.manydesigns.com/");
        n1_2.getChildNodes().add(n1_2_3);


        //1.3
        CustomNode n1_3 = new CustomNode(root);
        n1_3.setDescription("Profile");
        n1_3.setTitle("Profile");
        n1_3.setId("Profile");
        n1_3.setUrl("/Profile.action");

        //1.4
        FolderNode n1_4 = new FolderNode(root);
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

    public void testPermissions1() {
        assertTrue(permissions.isAllowed(groups));
    }

    public void testPermissions2() {
        groups.add("buoni");
        assertTrue(permissions.isAllowed(groups));
    }

    public void testPermissions3() {
        groups.add("cattivi");
        assertFalse(permissions.isAllowed(groups));
    }

    public void testPermissions4() {
        groups.add("buoni");
        groups.add("cattivi");
        assertFalse(permissions.isAllowed(groups));
    }

    public void testPermissions5() {
        groups.add(Group.ANONYMOUS);
        assertFalse(permissions.isAllowed(groups));
    }

    public void testPermissions6() {
        groups.add(Group.REGISTERED);
        assertFalse(permissions.isAllowed(groups));
    }
}
