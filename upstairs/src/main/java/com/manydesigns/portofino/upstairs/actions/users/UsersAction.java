package com.manydesigns.portofino.upstairs.actions.users;

import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.shiro.ShiroUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author Alessio Stalla - alessiostalla@gmail.com
 */
@RequiresAuthentication
@RequiresAdministrator
public class UsersAction extends AbstractPageAction {

    private static final Logger logger = LoggerFactory.getLogger(UsersAction.class);

    @Autowired
    protected Persistence persistence;

    @GET
    public Map getUsers() {
        return ShiroUtils.getPortofinoRealm().getUsers();
    }

    @Path("/groups")
    @GET
    public List<String> getGroups() {
        ArrayList<String> groups = new ArrayList<>(ShiroUtils.getPortofinoRealm().getGroups());
        groups.sort(Comparator.naturalOrder());
        return groups;
    }

}
