package com.manydesigns.portofino.pageactions.activitystream

import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.modules.DatabaseModule
import com.manydesigns.portofino.pageactions.activitystream.ActivityItem.Arg
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import net.sourceforge.stripes.util.UrlBuilder
import org.hibernate.Session
import org.hibernate.criterion.Order

@RequiresPermissions(level = AccessLevel.VIEW)
class MyActivityStreamAction extends ActivityStreamAction {

    @Inject(DatabaseModule.PERSISTENCE)
    private Persistence persistence;

    @Override
    public void populateActivityItems() {
        Session session = persistence.getSession("tt");
        List items = session.createCriteria("activity").addOrder(Order.desc("id")).setMaxResults(30).list();

        Locale locale = context.request.locale;
        for (Object item: items) {
//            String userName = "$item.fk_activity_user.first_name $item.fk_activity_user.last_name"
            String userName = "$item.user_"

            String ticketCode = "$item.project-$item.n"
            String ticketHref = "/projects/$item.project/tickets/$item.project/$item.n"
            String ticketTitle = "$item.n";

            Date timestamp = item.timestamp_;
            String imageSrc =
            new UrlBuilder(Locale.getDefault(), context.actionPath, false).
                    setEvent("userImage").
                    addParameter("userId", item.user_).
                    toString();
            String imageHref = null;
            String imageAlt = userName;
            String message = item.message;
            String key = "project." + item.fk_activity_type.type
            ActivityItem activityItem = new ActivityItem(
                    locale,
                    timestamp,
                    imageSrc,
                    imageHref,
                    imageAlt,
                    message,
                    key,
                    new Arg(userName, null),
                    new Arg(ticketCode, ticketHref),
                    new Arg(ticketTitle, null),
            );
            activityItems.add(activityItem)
        }
    }


}