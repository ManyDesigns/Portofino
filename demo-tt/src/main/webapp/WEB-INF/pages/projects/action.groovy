import com.manydesigns.portofino.pageactions.crud.CrudAction
import com.manydesigns.portofino.buttons.annotations.Button
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import net.sourceforge.stripes.action.RedirectResolution
import net.sourceforge.stripes.action.Resolution

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class projects extends CrudAction {

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

}