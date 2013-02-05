/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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
package com.manydesigns.portofino.system.model.users;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Group implements Serializable{
    public static final String copyright
            = "Copyright (c) 2005-2013, ManyDesigns srl";

    String groupId;
    String name;
    String description;
    String creatorId;
    Timestamp deletionDate;
    Timestamp creationDate;

    List<User> users = new ArrayList<User>();

    public Group() {

    }
    
    public Group(String groupId){
        this.groupId=groupId;
    }

    public Group(String groupId, String name, String description,
                 String creatorId, Timestamp deletionDate, Timestamp creationDate) {
        this.groupId = groupId;
        this.name = name;
        this.description = description;
        this.creatorId = creatorId;
        this.deletionDate = deletionDate;
        this.creationDate = creationDate;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public Timestamp getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(Timestamp deletionDate) {
        this.deletionDate = deletionDate;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Group)) return false;

        Group group = (Group) o;

        return getGroupId().equals(group.getGroupId());

    }

    @Override
    public int hashCode() {
        return getGroupId().hashCode();
    }
}

