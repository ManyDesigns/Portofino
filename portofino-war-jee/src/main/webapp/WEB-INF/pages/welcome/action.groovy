import com.manydesigns.portofino.*
import com.manydesigns.portofino.buttons.annotations.*
import com.manydesigns.portofino.dispatcher.*
import com.manydesigns.portofino.model.database.*
import com.manydesigns.portofino.pageactions.*
import com.manydesigns.portofino.security.*

import net.sourceforge.stripes.action.*
import org.hibernate.*
import org.hibernate.criterion.*

import com.manydesigns.portofino.pageactions.text.*

@RequiresPermissions(level = AccessLevel.VIEW)
class Welcome extends TextAction {

}