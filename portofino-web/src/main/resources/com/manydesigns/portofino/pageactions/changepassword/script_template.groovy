import com.manydesigns.portofino.pageactions.changepassword.ChangePasswordAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions

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