package portofino.actions.welcome;

import com.manydesigns.portofino.resourceactions.custom.CustomAction;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;

import javax.ws.rs.GET;

@RequiresPermissions(level = AccessLevel.VIEW)
public class Welcome extends CustomAction {

    @GET
    public String welcomeMessage() {
        return "Welcome to your new Portofino application!";
    }

}
