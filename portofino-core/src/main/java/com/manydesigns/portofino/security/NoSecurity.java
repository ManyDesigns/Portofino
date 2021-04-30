package com.manydesigns.portofino.security;

import com.manydesigns.portofino.actions.Permissions;
import com.manydesigns.portofino.resourceactions.ActionInstance;
import com.manydesigns.portofino.resourceactions.ResourceAction;
import org.apache.commons.configuration2.Configuration;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NoSecurity extends SecurityFacade {
    public static final NoSecurity AT_ALL = new NoSecurity();

    private NoSecurity() {}

    @Override
    public boolean hasPermissions(Configuration conf, Permissions configuration, AccessLevel accessLevel, String... permissions) {
        return true;
    }

    @Override
    public boolean isOperationAllowed(HttpServletRequest request, ActionInstance actionInstance, ResourceAction resourceAction, Method handler) {
        return true;
    }

    @Override
    public boolean isAdministrator(Configuration conf) {
        return true;
    }

    @Override
    public Object getUserId() {
        return null;
    }

    @Override
    public Set<String> getGroups() {
        return Collections.emptySet();
    }
}
