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
package com.manydesigns.portofino.context;

import com.manydesigns.portofino.AbstractPortofinoTest;
import com.manydesigns.portofino.model.datamodel.ForeignKey;
import com.manydesigns.portofino.model.Model;

import java.util.HashMap;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class VirtualRelationshipsTest  extends AbstractPortofinoTest {
    private static final String PORTOFINO_PUBLIC_USER = "portofino.PUBLIC.users";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        application.openSession();
    }

    public void tearDown() {
        application.closeSession();
    }

    public void testModel(){
        Model model = application.getModel();
        List<ForeignKey> list = model.getAllForeignKeys();
        boolean test = false;

        for(ForeignKey fk : list){
            if (fk.isVirtual()){
                test=true;
            }
        }

        assertTrue(test);
    }

    public void testRelationshipInSameDB(){
        //Relazione virtuale nello stesso database
        HashMap<String, Object> pk = new HashMap<String, Object>();
        pk.put("comune", "genova");
        pk.put("provincia", "genova");
        pk.put("regione", "liguria");
        Object comune = application.getObjectByPk
                ("hibernatetest.PUBLIC.comune", pk);

        List objs = application.getRelatedObjects("hibernatetest.PUBLIC.comune",
                comune, "fk_delibera_1");
        assertEquals(1, objs.size());

    }

    public void testRelationshipInDifferentDB(){
        //Relazione virtuale fra database
        HashMap<String, Object> pkCat = new HashMap<String, Object>();
        pkCat.put("catid", "FISH");
        Object catObj = application.getObjectByPk
                ("jpetstore.PUBLIC.category", pkCat);
        List cats = application.getRelatedObjects("jpetstore.PUBLIC.category",
                catObj, "fk_delibera_2");
        assertNotNull(cats);

        HashMap<String, Object> delibera = new HashMap<String, Object>();
        delibera = (HashMap<String, Object>) cats.get(0);
        assertEquals("FISH",delibera.get("catid"));
        assertEquals("genova",delibera.get("comune"));
        assertEquals("genova",delibera.get("provincia"));
        assertEquals("liguria",delibera.get("regione"));
    }
}
