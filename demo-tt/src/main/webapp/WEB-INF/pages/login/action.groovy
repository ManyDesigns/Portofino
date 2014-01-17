import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.Mode
import com.manydesigns.elements.forms.FormBuilder
import com.manydesigns.portofino.pageactions.login.DefaultLoginAction
import com.manydesigns.portofino.shiro.PortofinoRealm

class MyLogin extends DefaultLoginAction {

    @Override
    protected String getRememberedUserName(Serializable principal) {
        return principal.email;
    }

    @Override
    protected boolean checkPasswordStrength(String password, List<String> errorMessages) {

        if (password == null) {
            errorMessages.add(ElementsThreadLocals.getText("empty.password"))
            return false;
        }
        if (password.length() < 8) {
            errorMessages.add(ElementsThreadLocals.getText("password.too.short"))
            return false;
        }
        return true;
    }

    @Override
    protected void setupSignUpForm(PortofinoRealm realm) {
        FormBuilder formBuilder = new FormBuilder(realm.getSelfRegisteredUserClassAccessor())
                .configMode(Mode.CREATE)
                .configFields("email", "password", "first_name", "last_name");
        signUpForm = formBuilder.build();
        signUpForm.findFieldByPropertyName("password").setRequired(true);
    }

    @Override
    protected boolean validateSignUpPassword(List<String> errorMessages) {
        String password = signUpForm.findFieldByPropertyName("password").getValue();
        return checkPasswordStrength(password, errorMessages);
    }


}