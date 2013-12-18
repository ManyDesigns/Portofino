package com.manydesigns.portofino.pageactions.crud

import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import net.sourceforge.stripes.action.Resolution

@SupportsPermissions([CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE])
@RequiresPermissions(level = AccessLevel.VIEW)
class MyCrudAction extends CrudAction {

    @Override
    Resolution create() {
        throw new UnsupportedOperationException("Users cannot be created");
    }

    @Override
    Resolution save() {
        throw new UnsupportedOperationException("Users cannot be created");
    }


}