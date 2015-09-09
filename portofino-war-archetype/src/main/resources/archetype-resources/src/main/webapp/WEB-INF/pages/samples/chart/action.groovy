
import com.manydesigns.portofino.security.*
import net.sourceforge.stripes.action.*
import com.manydesigns.portofino.pageactions.custom.*

@RequiresPermissions(level = AccessLevel.VIEW)
class ChartjsExample extends CustomAction {

    @DefaultHandler
    public Resolution execute() {
        String fwd = "/WEB-INF/pages/samples/chart/chartjs.jsp";
        return new ForwardResolution(fwd);
    }

}