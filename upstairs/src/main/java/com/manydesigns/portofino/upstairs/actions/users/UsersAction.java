package com.manydesigns.portofino.upstairs.actions.users;

import com.manydesigns.elements.messages.RequestMessages;
import com.manydesigns.portofino.model.database.Column;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.resourceactions.AbstractResourceAction;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.shiro.ShiroUtils;
import com.manydesigns.portofino.upstairs.actions.UpstairsAction;
import com.manydesigns.portofino.upstairs.actions.support.WizardInfo;
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
public class UsersAction extends AbstractResourceAction {

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

    @Path("/check-wizard")
    @POST
    public boolean createApplication(WizardInfo wizard) throws Exception {
        Table userTable = UpstairsAction.getTable(persistence.getModel(), wizard.usersTable);
        if(userTable == null) {
            return true;
        }
        Column userPasswordColumn = UpstairsAction.getColumn(userTable, wizard.userPasswordProperty);
        if(userPasswordColumn == null) {
            return true;
        }
        if(userPasswordColumn.getActualJavaType() != String.class) {
            RequestMessages.addErrorMessage("The type of the password column, " + userPasswordColumn.getColumnName() + ", is not string: " + userPasswordColumn.getActualJavaType().getSimpleName());
            return false;
        }
        if(userPasswordColumn.getLength() < 32) { //TODO: would make sense to conditionalize this on the encryption algorithm + encoding combination
            RequestMessages.addErrorMessage("The length of the password column, " + userPasswordColumn.getColumnName() + ", is less than 32: " + userPasswordColumn.getLength());
            return false;
        }
        return true;
    }

}
