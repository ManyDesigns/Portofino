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

package com.manydesigns.portofino.actions;

import com.manydesigns.portofino.annotations.InjectNavigation;
import com.manydesigns.portofino.navigation.Navigation;
import com.manydesigns.portofino.navigation.NavigationNode;
import com.opensymphony.xwork2.ActionSupport;

import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class FolderAction extends ActionSupport {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @InjectNavigation
    public Navigation navigation;

    public String redirectUrl;

    public String execute() {
        NavigationNode navigationNode = navigation.getSelectedNavigationNode();
        List<NavigationNode> childNodes;
        if (navigationNode == null) { // TODO: generalizzare e togliere questo if
            childNodes = navigation.getRootNodes();
        } else {
            childNodes = navigationNode.getChildNodes();
        }
        if (childNodes.isEmpty()) {
            return null;
        } else {
            NavigationNode firstChild = childNodes.get(0);
            redirectUrl = firstChild.getUrl();
            return SUCCESS;
        }
    }
}
