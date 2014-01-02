import com.manydesigns.portofino.pageactions.custom.CustomAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import net.sourceforge.stripes.action.DefaultHandler
import net.sourceforge.stripes.action.RedirectResolution
import net.sourceforge.stripes.action.Resolution
import org.apache.shiro.authz.annotation.RequiresAuthentication

@RequiresAuthentication
@RequiresPermissions(level = AccessLevel.VIEW)
class MyCustomAction extends CustomAction {

    //Automatically generated on Mon Oct 28 13:16:47 CET 2013 by ManyDesigns Portofino
    //Write your code here

    @DefaultHandler
    public Resolution execute() {
        return new RedirectResolution("/admin/users");
    }

}