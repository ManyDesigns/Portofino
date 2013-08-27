import com.manydesigns.portofino.pageactions.custom.CustomAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import net.sourceforge.stripes.action.DefaultHandler
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution

@RequiresPermissions(level = AccessLevel.VIEW)
class latestProjects extends CustomAction {

    @DefaultHandler
    @Override
    public Resolution execute() {
        String fwd = getAppJsp("/latestProjects.jsp");
        return forwardTo(fwd);
    }

}