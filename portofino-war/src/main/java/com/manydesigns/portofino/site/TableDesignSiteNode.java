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

package com.manydesigns.portofino.site;

import com.manydesigns.elements.Util;
import com.manydesigns.portofino.context.MDContext;
import com.manydesigns.portofino.model.Table;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableDesignSiteNode implements SiteNode {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected final MDContext context;

    public TableDesignSiteNode(MDContext context) {
        this.context = context;
    }

    public String getUrl() {
        return Util.getAbsoluteUrl("/TableDesign.action");
    }

    public String getTitle() {
        return "Table design";
    }

    public String getDescription() {
        return "Redirects to the first table available";
    }

    public List<SiteNode> getChildNodes() {
        List<Table> tables = context.getDataModel().getAllTables();
        List<SiteNode> result = new ArrayList<SiteNode>();
        for (Table table : tables) {
            result.add(new TableSiteNode(table));
        }
        return result;
    }

    class TableSiteNode implements SiteNode {
        protected final Table table;

        public TableSiteNode(Table table) {
            this.table = table;
        }

        public String getUrl() {
            return Util.getAbsoluteUrl(
                    MessageFormat.format("/{0}/TableDesign.action",
                            table.getQualifiedName()));
        }

        public String getTitle() {
            return table.getQualifiedName();
        }

        public String getDescription() {
            return "Single table design view";
        }

        public List<SiteNode> getChildNodes() {
            return null;
        }
    }
}
