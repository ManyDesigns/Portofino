package com.manydesigns.portofino.pageactions.text

import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions

@RequiresPermissions(level = AccessLevel.VIEW)
class MyTextAction extends com.manydesigns.portofino.pageactions.text.TextAction {

    //Automatically generated on Mon Dec 09 16:54:25 CET 2013 by ManyDesigns Portofino
    //Write your code here

    @Override
    protected String computeTextFileName() {
        return super.computeTextFileName()
    }

}