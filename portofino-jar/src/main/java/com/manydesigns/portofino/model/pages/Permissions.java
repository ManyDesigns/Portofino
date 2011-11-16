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
import org.apache.commons.collections.CollectionUtils;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
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
    public static final String VIEW = "view";
    public static final String EDIT = "edit";
    public static final String DENY = "__deny";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Set<String> view;
    protected final Set<String> edit;
    protected final Set<String> deny;

    protected WithPermissions parent;

    protected Map<String, Set<String>> actualPermissions;

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public Permissions() {
        view = new HashSet<String>();
        edit = new HashSet<String>();
        deny = new HashSet<String>();
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        this.parent = (WithPermissions) parent;
    }

    public void reset() {
        actualPermissions = null;
    }

    public void init(Model model) {
        actualPermissions = new HashMap<String, Set<String>>();
        actualPermissions.put(DENY, new HashSet<String>(deny));
        actualPermissions.put(VIEW, new HashSet<String>(view));
        actualPermissions.put(EDIT, new HashSet<String>(edit));

        //Inherited permissions
        WithPermissions ancestor = (parent != null) ? parent.getParent() : null;
        if(ancestor != null) {
            Map<String, Set<String>> parentPermissions =
                        ancestor.getPermissions().getActualPermissions();
            for(Map.Entry<String, Set<String>> entry : actualPermissions.entrySet()) {
                Set<String> set = entry.getValue();
                if(set.isEmpty()) {
                    set.addAll(parentPermissions.get(entry.getKey()));
                }
            }
        }

        //Edit implies view
        Set<String> view = actualPermissions.get(VIEW);
        if(!view.isEmpty()) {
            view.addAll(actualPermissions.get(EDIT));
        }
    }

    public void link(Model model) {}

    public void visitChildren(ModelVisitor visitor) {}

    public String getQualifiedName() {
        return null;
    }

    //**************************************************************************
    // Permission verification
    //**************************************************************************

    public boolean isAllowed(String operation, List<String> groups) {
        if (CollectionUtils.containsAny(actualPermissions.get(DENY), groups)) {
            return false;
        }

        Set<String> perm = actualPermissions.get(operation);

        if(perm == null || perm.isEmpty()) {
            //View by default is allowed
            return VIEW.equals(operation);
        }

        return CollectionUtils.containsAny(perm, groups);
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlElementWrapper(name="view")
    @XmlElement(name = "group", type = java.lang.String.class)
    public Set<String> getView() {
        return view;
    }

    @XmlElementWrapper(name="edit")
    @XmlElement(name = "group", type = java.lang.String.class)
    public Set<String> getEdit() {
        return edit;
    }


    @XmlElementWrapper(name="deny")
    @XmlElement(name = "group", type = java.lang.String.class)
    public Set<String> getDeny() {
        return deny;
    }

    public WithPermissions getParent() {
        return parent;
    }

    public Map<String, Set<String>> getActualPermissions() {
        return actualPermissions;
    }
}
