import com.manydesigns.portofino.shiro.AbstractPortofinoRealm
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.apache.shiro.authc.*

class Security extends AbstractPortofinoRealm {

    private static final Logger logger = LoggerFactory.getLogger(Security.class);

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    private static final String GUEST_LOGIN = "guest";
    private static final String GUEST_PASSWORD = "guest";

    //--------------------------------------------------------------------------
    // Authentication
    //--------------------------------------------------------------------------

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        return loadAuthenticationInfo(token);
    }

    AuthenticationInfo loadAuthenticationInfo(UsernamePasswordToken usernamePasswordToken) {
        String userName = usernamePasswordToken.username;
        String password = new String(usernamePasswordToken.password);
        if (ADMIN_LOGIN.equals(userName) && ADMIN_PASSWORD.equals(password) ||
            GUEST_LOGIN.equals(userName) && GUEST_PASSWORD.equals(password)) {
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(userName, password.toCharArray(), getName());
            return info;
        } else {
            throw new AuthenticationException("Login failed");
        }
    }

    //--------------------------------------------------------------------------
    // Authorization
    //--------------------------------------------------------------------------

    @Override
    protected Collection<String> loadAuthorizationInfo(Serializable principal) {
        if (ADMIN_LOGIN.equals(principal)) {
            return [ getAdministratorsGroup() ]
        } else {
            return []
        }
    }

    Set<String> getUsers() {
        Set<String> result = new LinkedHashSet<String>();
        result.add(ADMIN_LOGIN);
        result.add(GUEST_LOGIN);
        return result;
    }

}
