import com.manydesigns.portofino.pageactions.crud.CrudAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import com.manydesigns.elements.Mode
import com.manydesigns.elements.fields.SelectField

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class issues extends CrudAction {
    @Override
    protected void setupForm(Mode mode) {
        super.setupForm(mode)
        SelectField sf = (SelectField) form.findFieldByPropertyName("tracker_id");
        sf.setCreateNewValueHref("/portofino4/trackers?create=&popup=true")
        sf.setCreateNewValueText("create new")
    }



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