import com.manydesigns.portofino.actions.chart.ChartAction
import com.manydesigns.portofino.model.pages.AccessLevel
import com.manydesigns.portofino.system.model.users.annotations.RequiresPermissions

@RequiresPermissions(level = AccessLevel.VIEW)
class issues_by_status_bar extends ChartAction {

}