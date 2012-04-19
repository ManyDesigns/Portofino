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

import com.manydesigns.portofino.pageactions.changepassword.*

@RequiresPermissions(level = AccessLevel.VIEW)
class MyChangePasswordAction extends ChangePasswordAction {

    //Automatically generated on %{new java.util.Date()} by ManyDesigns Portofino
    //Write your code here

    @Override
    protected String encrypt(String password) {
        //By default, the password is not encrypted
        return password;
    }

}