package com.manydesigns.portofino.resourceactions.crud.security;

import com.manydesigns.portofino.resourceactions.Group;
import com.manydesigns.portofino.resourceactions.Permissions;
import com.manydesigns.portofino.resourceactions.crud.AbstractCrudAction;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.SecurityFacade;
import com.manydesigns.portofino.security.SecurityLogic;
import org.apache.commons.configuration2.Configuration;
import org.jetbrains.annotations.NotNull;

public abstract class EntityPermissionsChecks {
    public static boolean isPermitted(
            Configuration configuration, SecurityFacade security, String[] requiredPermissions, EntityPermissions ep) {
        if(ep == null) {
            return true;
        }
        Permissions permissions = getPermissions(configuration, ep);

        return security.hasPermissions(
                configuration, permissions, AccessLevel.VIEW, requiredPermissions);
    }

    @NotNull
    public static Permissions getPermissions(Configuration configuration, @NotNull EntityPermissions ep) {
        Permissions permissions = new Permissions();
        String allGroup = SecurityLogic.getAllGroup(configuration);
        configurePermission(permissions, allGroup, AbstractCrudAction.PERMISSION_CREATE, ep.create());
        configurePermission(permissions, allGroup, AbstractCrudAction.PERMISSION_DELETE, ep.delete());
        configurePermission(permissions, allGroup, AbstractCrudAction.PERMISSION_EDIT, ep.update());
        configurePermission(permissions, allGroup, AbstractCrudAction.PERMISSION_READ, ep.read());
        permissions.init();
        return permissions;
    }

    private static void configurePermission(
            Permissions permissions, String allGroup, String permission, String[] groups) {
        for(String groupName : groups) {
            if(groupName.equals("*")) {
                groupName = allGroup;
            }
            String finalGroup = groupName;
            Group group = permissions.getGroups().stream()
                    .filter(g -> g.getName().equals(finalGroup))
                    .findFirst().orElseGet(() -> {
                        Group grp = new Group();
                        grp.setName(finalGroup);
                        grp.setAccessLevel(AccessLevel.VIEW.name());
                        permissions.getGroups().add(grp);
                        return grp;
                    });
            if(permission != null) {
                group.getPermissions().add(permission);
            }
        }
    }
}
