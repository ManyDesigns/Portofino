package portofino.actions.projects._detail.components

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.operations.GuardType

import com.manydesigns.portofino.operations.annotations.Guard
import com.manydesigns.portofino.resourceactions.crud.CrudAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import net.sourceforge.stripes.action.Before
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class MyCrudAction extends CrudAction {

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
    // Create customizations
    //**************************************************************************

    @Override

    @Guard(test="isManager()", type=GuardType.VISIBLE)
    Resolution create() {
        return super.create()    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override

    @Guard(test="isManager()", type=GuardType.VISIBLE)
    Resolution save() {
        return super.save()    //To change body of overridden methods use File | Settings | File Templates.
    }


    @Override
    protected void createSetup(Object object) {
        object.project = project.id;
    }

    @Override
    protected boolean createValidate(Object object) {
        Date now = new Date();
        object.created = now;
        object.last_updated = now;
        return true;
    }

    //**************************************************************************
    // Read customizations
    //**************************************************************************

    protected Resolution getReadView() {
        return new ForwardResolution("/jsp/projects/components/component-read.jsp");
    }

    //**************************************************************************
    // Search customizations
    //**************************************************************************

    protected Resolution getSearchView() {
        return new ForwardResolution("/jsp/projects/components/components-search.jsp");
    }

    //**************************************************************************
    // Edit customizations
    //**************************************************************************

    @Override

    @Guard(test="object != null && isManager()", type=GuardType.VISIBLE)
    Resolution edit() {
        return super.edit()    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override

    @Guard(test="isManager()", type=GuardType.VISIBLE)
    Resolution update() {
        return super.update()    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected boolean editValidate(Object object) {
        Date now = new Date();
        object.last_updated = now;
        return true;
    }


    //**************************************************************************
    // Delete customizations
    //**************************************************************************


    @Guard(test = "object != null && isManager()", type = GuardType.VISIBLE)
    public Resolution delete() {
        return super.delete();
    }

    //**************************************************************************
    // Bulk edit customizations
    //**************************************************************************

    Resolution bulkEdit() {
        throw new UnsupportedOperationException("Bulk operations not supported on components");
    }

    Resolution bulkUpdate() {
        throw new UnsupportedOperationException("Bulk operations not supported on components");
    }

    //**************************************************************************
    // Bulk delete customizations
    //**************************************************************************

    public Resolution bulkDelete() {
        throw new UnsupportedOperationException("Bulk operations not supported on components");
    }


}
