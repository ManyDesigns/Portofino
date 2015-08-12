/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.pages;

import com.manydesigns.portofino.security.AccessLevel;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.*;
import java.util.HashSet;
import java.util.Set;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"accessLevel","name","permissions"})
public class Group {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Set<String> permissions;
    protected String accessLevel;
    protected String name;

    protected AccessLevel actualAccessLevel;

    //**************************************************************************
    // Construction and initialization
    //**************************************************************************

    public Group() {
        permissions = new HashSet<String>();
    }

    public void init() {
        actualAccessLevel = null;
        if(!StringUtils.isEmpty(accessLevel)) {
            actualAccessLevel = AccessLevel.valueOf(accessLevel);
        }
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlElement(name = "permission", type = String.class)
    public Set<String> getPermissions() {
        return permissions;
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name = "level")
    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public AccessLevel getActualAccessLevel() {
        return actualAccessLevel;
    }
}
