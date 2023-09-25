/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.resourceactions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.manydesigns.portofino.security.AccessLevel;

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
public class Permissions {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    @JsonProperty("groups")
    protected final List<Group> groups;

    protected final Map<String, AccessLevel> actualLevels;
    //<group, set<permission>>
    protected final Map<String, Set<String>> actualPermissions;

    //**************************************************************************
    // Construction and initialization
    //**************************************************************************

    public Permissions() {
        groups = new ArrayList<>();

        actualLevels = new HashMap<>();
        actualPermissions = new HashMap<>();
    }

    public void init() {
        for(Group group : groups) {
            group.init();
            actualLevels.put(group.getName(), group.getAccessLevel());
            actualPermissions.put(group.getName(), group.getPermissions());
        }
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlElement(name = "group", type = Group.class)
    @JsonIgnore
    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups.clear();
        this.groups.addAll(groups);
    }

    public Map<String, Set<String>> getActualPermissions() {
        return actualPermissions;
    }

    public Map<String, AccessLevel> getActualLevels() {
        return actualLevels;
    }
}
