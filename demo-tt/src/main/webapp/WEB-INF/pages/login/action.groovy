import com.manydesigns.portofino.pageactions.login.DefaultLoginAction

class MyLogin extends DefaultLoginAction {

    @Override
    protected String getRememberedUserName(Serializable principal) {
        return principal.email;
    }


}