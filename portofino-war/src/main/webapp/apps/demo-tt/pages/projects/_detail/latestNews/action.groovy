import com.manydesigns.portofino.actions.text.TextAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.system.model.users.annotations.RequiresPermissions

@RequiresPermissions(level = AccessLevel.VIEW)
class latestNews extends TextAction {

}