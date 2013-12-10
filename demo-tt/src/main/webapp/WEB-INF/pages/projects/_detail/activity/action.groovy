import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.modules.DatabaseModule
import com.manydesigns.portofino.pageactions.custom.CustomAction
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.tt.ActivityItem
import com.manydesigns.portofino.tt.ActivityItem.Arg
import net.sourceforge.stripes.action.Before
import net.sourceforge.stripes.action.DefaultHandler
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution
import org.hibernate.Session
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions

@RequiresPermissions(level = AccessLevel.VIEW)
class MyCustomAction extends CustomAction {

    Serializable project;

    @Before
    public void prepareProject() {
        project = ElementsThreadLocals.getOgnlContext().get("project");
    }

    @Inject(DatabaseModule.PERSISTENCE)
    private Persistence persistence;

    List<ActivityItem> activityItems = new ArrayList<ActivityItem>();



    @DefaultHandler
    public Resolution execute() {
        Session session = persistence.getSession("tt");
        List items = session.createCriteria("activity")
                .add(Restrictions.eq("project", project.id))
                .addOrder(Order.desc("id"))
                .setMaxResults(30)
                .list();

        Locale locale = context.request.locale;
        for (Object item : items) {
            String userName = "$item.fk_activity_user.first_name $item.fk_activity_user.last_name"

            Object ticket = item.fk_activity_ticket;
            String ticketCode = "$item.project-$item.n"
            String ticketHref = "/projects/$item.project/tickets/$item.project/$item.n"
            String ticketTitle = ticket.title;

            Date timestamp = item.date;
            String imageSrc = "/images/user-placeholder-40x40.png";
            String imageAlt = userName;
            String message = item.message;
            String key = "project." + item.fk_activity_type.type
            ActivityItem activityItem = new ActivityItem(
                    locale,
                    timestamp,
                    imageSrc,
                    imageAlt,
                    message,
                    key,
                    new Arg(userName, null),
                    new Arg(ticketCode, ticketHref),
                    new Arg(ticketTitle, null),
            );
            activityItems.add(activityItem)
        }

        return new ForwardResolution("/jsp/projects/activity.jsp");
    }

}