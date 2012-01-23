import com.manydesigns.portofino.pageactions.jsp.JspAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.system.model.users.annotations.RequiresPermissions

@RequiresPermissions(level = AccessLevel.VIEW)
class prjstatus extends JspAction {

}