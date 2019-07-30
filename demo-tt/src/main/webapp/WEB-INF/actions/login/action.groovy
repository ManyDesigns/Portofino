import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.reflection.ClassAccessor
import com.manydesigns.elements.reflection.FilteredClassAccessor
import com.manydesigns.portofino.resourceactions.login.DefaultLoginAction

class Login extends DefaultLoginAction {

    @Override
    protected boolean checkPasswordStrength(String password, List<String> errorMessages) {
        if (password == null) {
            errorMessages.add(ElementsThreadLocals.getText("empty.password"))
            return false
        }
        if (password.length() < 8) {
            errorMessages.add(ElementsThreadLocals.getText("password.too.short", 8))
            return false
        }
        true
    }

    @Override
    ClassAccessor getNewUserClassAccessor() {
        def classAccessor = new FilteredClassAccessor(
                super.getNewUserClassAccessor(),
                true, "email", "password", "first_name", "last_name")
        classAccessor
    }
}
