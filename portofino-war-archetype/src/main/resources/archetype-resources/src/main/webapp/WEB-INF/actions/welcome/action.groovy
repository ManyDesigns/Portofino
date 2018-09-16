import com.manydesigns.portofino.*
import com.manydesigns.portofino.buttons.annotations.*
import com.manydesigns.portofino.dispatcher.*
import com.manydesigns.portofino.model.database.*
import com.manydesigns.portofino.pageactions.*
import com.manydesigns.portofino.security.*

import javax.ws.rs.*

import org.hibernate.*
import org.hibernate.criterion.*


@RequiresPermissions(level = AccessLevel.VIEW)
class Welcome extends CustomAction {

    @GET
    String welcomeMessage() {
        'Welcome to your new Portofino application!'
    }

}