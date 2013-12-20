package com.manydesigns.portofino.pageactions.activitystream

import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.modules.DatabaseModule
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.tt.TtUtils
import org.hibernate.Session

@RequiresPermissions(level = AccessLevel.VIEW)
class MyActivityStreamAction extends ActivityStreamAction {

    public static String SYSTEM_ACTIVTY_SQL = TtUtils.ACTIVITY_SQL +
            "WHERE act.project IS NULL ORDER BY act.id DESC";

    @Inject(DatabaseModule.PERSISTENCE)
    private Persistence persistence;

    @Override
    public void populateActivityItems() {
        Locale locale = context.request.locale;
        Session session = persistence.getSession("tt");
        List items = session.createSQLQuery(SYSTEM_ACTIVTY_SQL).setMaxResults(30).list();

        String keyPrefix = "system.";

        TtUtils.populateActivityItems(items, activityItems, keyPrefix, locale, context);
    }

}