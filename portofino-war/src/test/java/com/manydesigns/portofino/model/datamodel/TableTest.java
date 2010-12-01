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
package com.manydesigns.portofino.model.datamodel;

import com.manydesigns.portofino.model.Model;
import junit.framework.TestCase;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableTest extends TestCase {
    public void testActualEntityNames(){
        Model model = new Model();
        Database db = new Database("portofino");
        Schema schema = new Schema(db, "meta");
        Table table = new Table(schema, " ab!!!..acus$%/()");
        model.getDatabases().add(db);
        table.init(model);

        assertNotNull(table.getActualEntityName());
        assertEquals("portofino_meta__ab_____acus$____", table.getActualEntityName());
        System.out.println(table.getActualEntityName());

        table = new Table(schema, "0DPrpt");
        table.init(model);
        assertEquals("portofino_meta_0dprpt", table.getActualEntityName());
        System.out.println(table.getActualEntityName());

        db = new Database("1portofino");
        schema = new Schema(db, "meta");
        table = new Table(schema, "0DPrpt");
        table.init(model);
        assertEquals("_1portofino_meta_0dprpt", table.getActualEntityName());
        System.out.println(table.getActualEntityName());

        db = new Database("$1portofino");
        schema = new Schema(db, "meta");
        table = new Table(schema, "0DPrpt");
        table.init(model);
        assertEquals("$1portofino_meta_0dprpt", table.getActualEntityName());
        System.out.println(table.getActualEntityName());

        db = new Database(".portofino");
        schema = new Schema(db, "meta");
        table = new Table(schema, "0DPrpt");
        table.init(model);
        assertEquals("_portofino_meta_0dprpt", table.getActualEntityName());
        System.out.println(table.getActualEntityName());

        db = new Database(".portofino");
        schema = new Schema(db, "meta");
        table = new Table(schema, "XYZéèçò°àùì");
        table.init(model);
        assertEquals("_portofino_meta_xyzéèçò_àùì", table.getActualEntityName());
        System.out.println(table.getActualEntityName());

        db = new Database(".portofino");
        schema = new Schema(db, "meta");
        table = new Table(schema, "ĖĔĕĘĘŜŞŝōŎľĿʛʋʊɪɩɨɷ");
        table.init(model);
        assertEquals("_portofino_meta_ĖĔĕĘĘŜŞŝōŎľĿʛʋʊɪɩɨɷ", table.getActualEntityName());
        System.out.println(table.getActualEntityName());
    }
}
