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

package com.manydesigns.portofino.navigation;

import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.model.site.SiteNode;

import java.text.MessageFormat;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableNavigationNode implements NavigationNode {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Table table;
    protected final String url;
    protected final String title;
    protected final String description;
    protected final SiteNode siteNode;
    protected List<NavigationNode> childNodes;


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public TableNavigationNode(SiteNode siteNode, Table table,
                               String urlFormat,
                               String titleFormat,
                               String descriptionFormat) {
        this.siteNode = siteNode;
        this.table = table;
        this.url =
                Util.getAbsoluteUrl(
                        MessageFormat.format(
                                urlFormat, table.getQualifiedName()));
        this.title = MessageFormat.format(
                titleFormat, table.getQualifiedName());
        this.description = MessageFormat.format(
                descriptionFormat, table.getQualifiedName());
    }




    //**************************************************************************
    // NavigationNode implementation
    //**************************************************************************

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<NavigationNode> getChildNodes() {
        return null;
    }

    public SiteNode getSiteNode() {
        return siteNode;
    }
}
