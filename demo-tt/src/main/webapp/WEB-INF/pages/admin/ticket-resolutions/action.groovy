import com.manydesigns.portofino.tt.TtUtils

import com.manydesigns.elements.Mode
import com.manydesigns.elements.forms.Form
import com.manydesigns.portofino.pageactions.crud.CrudAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authz.annotation.RequiresAuthentication

@RequiresAuthentication
@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class AdminTicketResolutionsCrudAction extends CrudAction {
    Object old;

    @Override
    protected void createPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_TICKET_RESOLUTION_CREATED,
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
                null,
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
        setupForm(Mode.EDIT);
        form.readFromObject(old);
        String message = TtUtils.createDiffMessage(form, newForm);
        if (message != null) {
            Date now = new Date();
            TtUtils.addActivity(session,
                    principal,
                    now,
                    TtUtils.ACTIVITY_TYPE_TICKET_RESOLUTION_UPDATED,
                    message,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    object,
                    null,
                    null,
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
                TtUtils.ACTIVITY_TYPE_TICKET_RESOLUTION_DELETED,
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
                null,
                null,
                null
        );
    }
}