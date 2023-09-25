package portofino.actions.projects

import com.manydesigns.elements.Mode
import com.manydesigns.elements.forms.Form
import com.manydesigns.portofino.operations.GuardType
import com.manydesigns.portofino.operations.annotations.Guard
import com.manydesigns.portofino.resourceactions.crud.CrudAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import com.manydesigns.portofino.tt.ActivityItem
import com.manydesigns.portofino.tt.Refresh
import com.manydesigns.portofino.tt.TtUtils
import org.apache.shiro.SecurityUtils

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import java.sql.Timestamp

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class ProjectsCrudAction extends CrudAction {

    // TODO This stopped working with Groovy 4
    //@Autowired
    Refresh refresh

    Object old

    public static String PROJECT_ACTIVTY_SQL = TtUtils.ACTIVITY_SQL +
            "WHERE act.project = :project_id ORDER BY act.id DESC"

    static {
        logger.info("Loaded action - ${ProjectsCrudAction.class.hashCode()} - ${Refresh.class.hashCode()}")
    }

    //**************************************************************************
    // Role checking
    //**************************************************************************

    public boolean isContributor() {
        return TtUtils.principalHasProjectRole(object, TtUtils.ROLE_CONTRIBUTOR);
    }

    public boolean isEditor() {
        return TtUtils.principalHasProjectRole(object, TtUtils.ROLE_EDITOR);
    }

    public boolean isManager() {
        return TtUtils.principalHasProjectRole(object, TtUtils.ROLE_MANAGER);
    }

    @Override
    boolean isBulkOperationsEnabled() {
        false //Remember, if you set it to true, you also have to disable the editPostProcess logic or bulk updates won't work.
    }

    //**************************************************************************
    // Extension hooks
    //**************************************************************************

    @Override
    protected void createSetup(Object object) {
        object.last_ticket = 0L;
    }

    @Override
    protected boolean createValidate(Object object) {
        Timestamp now = new Timestamp(new Date().time)
        object.created = now
        object.last_updated = now
        true
    }

    @Override
    protected void createPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_PROJECT_CREATED,
                null,
                null,
                object,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        Map member = new HashMap();
        member.project = object.id;
        member.user_ = principal.id;
        member.role = TtUtils.ROLE_MANAGER;
        member.notifications = true;
        session.save("members", (Object)member);

        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_MEMBER_CREATED,
                null,
                principal,
                object,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

    }

    //**************************************************************************
    // Edit customizations
    //**************************************************************************
    @Override
    boolean isEditEnabled() {
        return super.isEditEnabled() && isManager()
    }

    @Override
    protected boolean editValidate(Object object) {
        Date now = new Date();
        object.last_updated = now;
        return true;
    }

    @Override
    protected void editSetup(Object object) {
        old = object.clone()
    }

    @Override
    protected void editPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal
        Form newForm = form
        setupForm(Mode.EDIT)
        form.readFromObject(old)
        String message = TtUtils.createDiffMessage(form, newForm)
        if (message != null) {
            Date now = new Date();
            TtUtils.addActivity(session,
                    principal,
                    now,
                    TtUtils.ACTIVITY_TYPE_PROJECT_UPDATED,
                    message,
                    null,
                    object,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
    }

    //**************************************************************************
    // Delete customizations
    //**************************************************************************

    @Override
    boolean isDeleteEnabled() {
        return super.isDeleteEnabled() && isManager()
    }

    @Override
    protected void deletePostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_PROJECT_DELETED,
                null,
                null,
                object,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        )
    }

    @Override
    protected void doDelete(Object object) {
        session.createQuery('delete from members where project = :project')
                .setParameter('project', object.id)
                .executeUpdate()
        super.doDelete(object)
    }

    @GET
    @Path("activity")
    List<ActivityItem> getProjectActivity() {
        Locale locale = context.request.locale
        List items =
                session.createNativeQuery(PROJECT_ACTIVTY_SQL)
                .setParameter("project_id", object.id)
                .setMaxResults(30).list()

        String keyPrefix = "project.";

        String memberImageFormat = String.format("/projects/%s/members?userImage=&userId=%%s&code=%%s", object.id)

        List<ActivityItem> activityItems = []
        TtUtils.populateActivityItems(items, activityItems, keyPrefix, locale, memberImageFormat)
        return activityItems
    }

    @GET
    @Path("canCreateNewTicket")
    @Guard(test="isContributor()", type= GuardType.VISIBLE)
    boolean canCreateNewTicket() {
        true
    }

}
