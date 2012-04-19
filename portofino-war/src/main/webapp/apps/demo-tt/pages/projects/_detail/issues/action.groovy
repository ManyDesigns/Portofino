import com.manydesigns.portofino.pageactions.crud.CrudAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class issues extends CrudAction {

    void createSetup(object) {
        object.project_id = ognlContext.project.id;
        object.lock_version = 0;
        object.done_ratio = 0;
        object.author_id = 1;
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