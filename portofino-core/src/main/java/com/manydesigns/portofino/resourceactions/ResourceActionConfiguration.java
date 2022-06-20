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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link com.manydesigns.portofino.resourceactions.ResourceAction} configuration
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@XmlRootElement(name = "action")
@XmlAccessorType(value = XmlAccessType.NONE)
public class ResourceActionConfiguration {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    protected Permissions permissions;
    protected final List<AdditionalChild> additionalChildren = new ArrayList<>();

    public ResourceActionConfiguration() {
        permissions = new Permissions();
    }

    public void init() {
        if(permissions != null) {
            permissions.init();
        }
    }

    @XmlElement()
    public Permissions getPermissions() {
        return permissions;
    }

    public void setPermissions(Permissions permissions) {
        if (permissions == null) {
            permissions = new Permissions();
            permissions.init();
        }
        this.permissions = permissions;
    }

    @XmlElement(name = "additional-child", type = AdditionalChild.class)
    public List<AdditionalChild> getAdditionalChildren() {
        return additionalChildren;
    }

    public void setAdditionalChildren(List<AdditionalChild> children) {
        additionalChildren.clear();
        additionalChildren.addAll(children);
    }
}
