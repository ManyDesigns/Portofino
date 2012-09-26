import javax.servlet.*

import com.manydesigns.elements.messages.*
import com.manydesigns.elements.reflection.*
import com.manydesigns.portofino.*
import com.manydesigns.portofino.application.*
import com.manydesigns.portofino.buttons.annotations.*
import com.manydesigns.portofino.dispatcher.*
import com.manydesigns.portofino.model.database.*
import com.manydesigns.portofino.pageactions.*
import com.manydesigns.portofino.security.*

import net.sourceforge.stripes.action.*
import org.hibernate.*
import org.hibernate.criterion.*

import com.manydesigns.portofino.pageactions.chart.ChartAction

@RequiresPermissions(level = AccessLevel.VIEW)
class MyChartAction extends ChartAction {

    //Automatically generated on %{new java.util.Date()} by ManyDesigns Portofino
    //Write your code here

}