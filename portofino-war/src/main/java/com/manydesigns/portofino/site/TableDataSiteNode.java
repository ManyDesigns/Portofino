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
import com.manydesigns.portofino.context.Context;
import com.manydesigns.portofino.context.PortofinoThreadLocals;
import com.manydesigns.portofino.model.Table;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableDataSiteNode extends SimpleSiteNode {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public TableDataSiteNode() {
    }

    public TableDataSiteNode(String url, String title, String description) {
        super(url, title, description);
    }

    public List<SiteNode> getChildNodes() {
        Context context = PortofinoThreadLocals.getContext();
        if (context == null || context.getModel() == null) {
            return null;
        }
        List<Table> tables = context.getModel().getAllTables();
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
                    MessageFormat.format("/model/{0}/TableData.action",
                            table.getQualifiedName()));
        }

        public String getTitle() {
            return table.getQualifiedName();
        }

        public String getDescription() {
            return "Table data view: " + table.getQualifiedName();
        }

        public List<SiteNode> getChildNodes() {
            return null;
        }
    }
}
