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

package com.manydesigns.portofino.model.pages;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.ModelVisitor;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
public class Permissions implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final List<Group> groups;

    protected final Map<String, AccessLevel> actualLevels;
    //<group, set<permission>>
    protected final Map<String, Set<String>> actualPermissions;

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public Permissions() {
        groups = new ArrayList<Group>();

        actualLevels = new HashMap<String, AccessLevel>();
        actualPermissions = new HashMap<String, Set<String>>();
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {
    }

    public void reset() {
        actualLevels.clear();
        actualPermissions.clear();
    }

    public void init(Model model) {}

    public void link(Model model) {
        for(Group group : groups) {
            actualLevels.put(group.getName(), group.getActualAccessLevel());
            actualPermissions.put(group.getName(), group.getPermissions());
        }

        //Inherited permissions
        // TODO: rivedere in ottica locale
//        WithPermissions ancestor = (parent != null) ? parent.getParent() : null;
//        if(ancestor != null) {
//            Map<String, AccessLevel> parentLevels = ancestor.getPermissions().getActualLevels();
//            for(Map.Entry<String, AccessLevel> entry : parentLevels.entrySet()) {
//                String key = entry.getKey();
//                AccessLevel value = entry.getValue();
//                if(value == AccessLevel.DENY || actualLevels.get(key) == null) {
//                    actualLevels.put(key, value);
//                }
//            }
//        }
    }

    public void visitChildren(ModelVisitor visitor) {
        for(Group group : groups) {
            visitor.visit(group);
        }
    }

    public String getQualifiedName() {
        return null;
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlElement(name = "group", type = Group.class)
    public List<Group> getGroups() {
        return groups;
    }

    public Map<String, Set<String>> getActualPermissions() {
        return actualPermissions;
    }

    public Map<String, AccessLevel> getActualLevels() {
        return actualLevels;
    }
}
