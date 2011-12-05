import com.manydesigns.portofino.actions.CrudAction
import com.manydesigns.portofino.buttons.annotations.Button
import com.manydesigns.portofino.model.pages.AccessLevel
import com.manydesigns.portofino.system.model.users.annotations.RequiresPermissions
import com.manydesigns.portofino.system.model.users.annotations.SupportsPermissions
import net.sourceforge.stripes.action.RedirectResolution
import net.sourceforge.stripes.action.Resolution

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class _1 extends CrudAction {

    void createSetup(object) {
        object.status = 1;
    }

    boolean createValidate(object) {
        Date now = new Date();
        object.created_on = now;
        object.updated_on = now;
        return true;
    }
    
    boolean editValidate(object) {
        object.updated_on = new Date();
        return true;
    }

    @Button(list = "crud-search", key="Hello!")
    public Resolution doSomething() {
        System.out.println("Funzioooona!");
        return new RedirectResolution(dispatch.getOriginalPath());
    }
    
}