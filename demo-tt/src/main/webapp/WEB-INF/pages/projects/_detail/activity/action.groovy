import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.pageactions.custom.CustomAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import net.sourceforge.stripes.action.Before
import net.sourceforge.stripes.action.DefaultHandler
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution

@RequiresPermissions(level = AccessLevel.VIEW)
class MyCustomAction extends CustomAction {

    Serializable ticket;

    @Before
    public void prepareProject() {
        ticket = ElementsThreadLocals.getOgnlContext().get("ticket");
    }


    @DefaultHandler
    public Resolution execute() {
        String fwd = "/jsp/projects/tickets/activity.jsp";
        return new ForwardResolution(fwd);
    }

}