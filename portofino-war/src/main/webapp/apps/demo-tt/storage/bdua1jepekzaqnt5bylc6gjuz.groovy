import com.manydesigns.portofino.actions.CrudAction
import com.manydesigns.portofino.model.pages.AccessLevel
import com.manydesigns.portofino.system.model.users.annotations.RequiresPermissions
import com.manydesigns.portofino.system.model.users.annotations.SupportsPermissions

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class bdua1jepekzaqnt5bylc6gjuz extends CrudAction {

    void createSetup(object) {
        object.project_id = project.id;
    }

}