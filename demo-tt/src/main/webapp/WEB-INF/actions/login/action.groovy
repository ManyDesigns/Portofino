import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.Mode
import com.manydesigns.elements.forms.FormBuilder
import com.manydesigns.portofino.resourceactions.login.DefaultLoginAction
import com.manydesigns.portofino.shiro.PortofinoRealm

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

    protected void setupSignUpForm(PortofinoRealm realm) {
        FormBuilder formBuilder = new FormBuilder(realm.getSelfRegisteredUserClassAccessor())
                .configMode(Mode.CREATE)
                .configFields("email", "password", "first_name", "last_name");
        signUpForm = formBuilder.build();
        signUpForm.findFieldByPropertyName("password").setRequired(true);
    }
}
