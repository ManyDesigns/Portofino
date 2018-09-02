import com.manydesigns.portofino.pageactions.custom.CustomAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import org.apache.shiro.authz.annotation.RequiresAuthentication

import javax.ws.rs.GET
import javax.ws.rs.core.Response

@RequiresAuthentication
@RequiresPermissions(level = AccessLevel.VIEW)
class MyCustomAction extends CustomAction {

    //Automatically generated on Mon Oct 28 13:16:47 CET 2013 by ManyDesigns Portofino
    //Write your code here

    @GET
    Response redirect() {
        Response.temporaryRedirect(new URI('/admin/users')).build()
    }

}