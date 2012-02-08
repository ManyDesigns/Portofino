import com.manydesigns.portofino.pageactions.text.TextAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions

@RequiresPermissions(level = AccessLevel.VIEW)
class Welcome extends TextAction {

}