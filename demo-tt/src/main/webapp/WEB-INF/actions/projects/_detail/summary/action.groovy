import com.manydesigns.portofino.tt.TtUtils

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.operations.GuardType

import com.manydesigns.portofino.operations.annotations.Guard
import com.manydesigns.portofino.resourceactions.custom.CustomAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import javax.ws.rs.GET
import net.sourceforge.stripes.action.*
import javax.ws.rs.Produces

@RequiresPermissions(level = AccessLevel.VIEW)
class ProjectSummaryAction extends CustomAction {

    Serializable project;

    @Before
    public void prepareProject() {
        project = ElementsThreadLocals.getOgnlContext().get("project");
    }

    //**************************************************************************
    // Role checking
    //**************************************************************************

    public boolean isContributor() {
        return TtUtils.principalHasProjectRole(project, TtUtils.ROLE_CONTRIBUTOR);
    }

    public boolean isManager() {
        return TtUtils.principalHasProjectRole(project, TtUtils.ROLE_MANAGER);
    }

    //**************************************************************************
    // Web methods
    //**************************************************************************

    @DefaultHandler
    public Resolution execute() {
        return new ForwardResolution("/jsp/projects/summary.jsp");
    }


    @Guard(test="isContributor()", type=GuardType.VISIBLE)
    public Resolution createNewTicket() {
        return new RedirectResolution("/projects/$project.id/tickets?create=");
    }


    @Guard(test="isManager()", type=GuardType.VISIBLE)
    public Resolution editProjectDetails() {
        return new RedirectResolution("/projects/$project.id?edit=");
    }
}
