import com.manydesigns.elements.forms.Form
import com.manydesigns.portofino.pageactions.crud.CrudAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import com.manydesigns.portofino.tt.TtUtils
import org.apache.shiro.SecurityUtils

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class AdminTicketTypesCrudAction extends CrudAction {
    Object old;

    @Override
    protected void createPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_TICKET_TYPE_CREATED,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                object,
                null,
                null
        );
    }

    @Override
    protected void editSetup(Object object) {
        old = object.clone();
    }

    @Override
    protected void editPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Form newForm = form;
        form = buildForm(formBuilder);
        form.readFromObject(old);
        String message = TtUtils.createDiffMessage(form, newForm);
        if (message != null) {
            Date now = new Date();
            TtUtils.addActivity(session,
                    principal,
                    now,
                    TtUtils.ACTIVITY_TYPE_TICKET_TYPE_UPDATED,
                    message,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    object,
                    null,
                    null
            );
        }
    }

    @Override
    protected void deletePostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_TICKET_TYPE_DELETED,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                object,
                null,
                null
        );
    }
}