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
import junit.framework.TestCase;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class SiteNodeTest extends TestCase{
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
        n1_2_1.setUrl("TableData.action");
        n1_2.getChildNodes().add(n1_2_1);

        CustomFolderNode n1_2_2 = new CustomFolderNode(n1_2);
        n1_2_2.setType("table-design");
        n1_2_2.setDescription("TableData design description");
        n1_2_2.setTitle("TableData design title");
        n1_2_2.setId("TableDesign");
        n1_2_2.setUrl("TableDesign.action");
        n1_2.getChildNodes().add(n1_2_2);


        //1.3
        CustomNode n1_3 = new CustomNode(root);
        n1_3.setDescription("Profile");
        n1_3.setTitle("Profile");
        n1_3.setId("Profile");
        n1_3.setUrl("Profile.action");

        //1.4
        FolderNode n1_4 = new FolderNode(root);
        n1_4.setDescription("user administration");
        n1_4.setTitle("user admin");
        n1_4.setId("userAdmin");
        UseCaseNode n1_4_1 = new UseCaseNode(n1_4);
        n1_4_1.setDescription("user administration");
        n1_4_1.setTitle("user admin");
        n1_4_1.setId("userAdmin");
        n1_4.getChildNodes().add(n1_4_1);


        //Aggiungo i nodi alla radice
        root.getChildNodes().add(n1_1);
        root.getChildNodes().add(n1_2);
        root.getChildNodes().add(n1_3);
        root.getChildNodes().add(n1_4);
        root.init(model);

        SiteNode rootNode = model.getRoot();
    }
}
