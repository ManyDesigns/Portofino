import com.manydesigns.portofino.pageactions.custom.CustomAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import net.sourceforge.stripes.action.DefaultHandler
import net.sourceforge.stripes.action.Resolution

@RequiresPermissions(level = AccessLevel.VIEW)
class MyCustomAction extends CustomAction {

    //Automatically generated on Mon Oct 28 12:27:18 CET 2013 by ManyDesigns Portofino
    //Write your code here

    @DefaultHandler
    public Resolution execute() {
        String fwd = "/m/pageactions/pageactions/custom/example.jsp";
        return forwardTo(fwd);
    }

}