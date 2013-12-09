import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.buttons.GuardType
import com.manydesigns.portofino.buttons.annotations.Button
import com.manydesigns.portofino.buttons.annotations.Guard
import com.manydesigns.portofino.pageactions.custom.CustomAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.tt.TtUtils
import net.sourceforge.stripes.action.*

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

    @Button(list = "pageHeaderButtons", key = "edit.project.details", order = 1d, icon = "icon-edit")
    @Guard(test="isManager()", type=GuardType.VISIBLE)
    public Resolution editProjectDetails() {
        return new RedirectResolution("/projects/$project.id?edit=");
    }

}