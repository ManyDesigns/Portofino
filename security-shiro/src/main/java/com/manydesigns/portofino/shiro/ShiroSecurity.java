package com.manydesigns.portofino.shiro;

import com.manydesigns.portofino.actions.Permissions;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.SecurityFacade;
import org.apache.commons.configuration2.Configuration;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.manydesigns.portofino.security.SecurityLogic.*;

public class ShiroSecurity extends SecurityFacade {

    public static final SecurityUtilsBean SECURITY_UTILS_BEAN = new SecurityUtilsBean();

    @Override
    public boolean hasPermissions(Configuration conf, Permissions configuration, AccessLevel level, String... permissions) {
        Subject subject = SecurityUtils.getSubject();
        Object principal = subject.getPrincipal();
        if(principal != null) {
            if(isAdministrator(conf)) {
                return true;
            }
            ActionPermission actionPermission = new ActionPermission(configuration, level, permissions);
            return subject.isPermitted(actionPermission);
        } else {
            //Shiro does not check permissions for non authenticated users
            return hasAnonymousPermissions(conf, configuration, level, permissions);
        }
    }

    public static boolean hasAnonymousPermissions
            (Configuration conf, Permissions configuration, AccessLevel level, String... permissions) {
        ActionPermission actionPermission = new ActionPermission(configuration, level, permissions);
        List<String> groups = new ArrayList<>();
        groups.add(getAllGroup(conf));
        groups.add(getAnonymousGroup(conf));
        return new GroupPermission(groups).implies(actionPermission);
    }

    @Override
    public boolean isAdministrator(Configuration conf) {
        String administratorsGroup = getAdministratorsGroup(conf);
        Subject subject = SecurityUtils.getSubject();
        return subject.isAuthenticated() && subject.hasRole(administratorsGroup);
    }

    @Override
    public Object getSecurityUtilsBean() {
        return SECURITY_UTILS_BEAN;
    }

    @Override
    public Object getUserId() {
        Subject subject = SecurityUtils.getSubject();
        Object principal = subject.getPrincipal();
        if (principal == null) {
            logger.debug("No user found");
        } else {
            try {
                return ShiroUtils.getUserId(subject);
            } catch (Exception e) {
                logger.warn("Could not retrieve user id. This usually happens if Security.groovy has been changed in an incompatible way.", e);
            }
        }
        return null;
    }

    @Override
    public Set<String> getGroups() {
        return ShiroUtils.getPortofinoRealm().getGroups();
    }
}
