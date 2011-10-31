import com.manydesigns.portofino.system.model.users.User
import com.manydesigns.portofino.logic.SecurityLogic

def login(username, password) {
    if("guest".equalsIgnoreCase(username)) {
        User guestUser = new User("__guest__");
        guestUser.setAgreedToTerms(true);
        guestUser.setUserName("guest");
        guestUser.setState(SecurityLogic.ACTIVE);
        guestUser.setEmail("guest@example.com");
        return guestUser;
    } else {
        return SecurityLogic.defaultLogin(loginAction.getApplication(), username, password);
    }
}