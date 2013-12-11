package com.manydesigns.portofino.pageactions.activitystream

import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import net.sourceforge.stripes.action.Resolution

@RequiresPermissions(level = AccessLevel.VIEW)
class MyActivityStreamAction extends ActivityStreamAction {

    //Automatically generated on %{new java.util.Date()} by ManyDesigns Portofino
    //Write your code here

    @Override
    void populateActivityItems() {
        super.populateActivityItems();
    }

    @Override
    protected Resolution getViewResolution() {
        return super.getViewResolution();
    }

}