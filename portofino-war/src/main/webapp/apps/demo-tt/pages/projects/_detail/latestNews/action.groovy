import com.manydesigns.portofino.pageactions.text.TextAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.system.model.users.annotations.RequiresPermissions

@RequiresPermissions(level = AccessLevel.VIEW)
class latestNews extends TextAction {

}