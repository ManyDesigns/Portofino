import com.manydesigns.portofino.security.SecurityLogic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.manydesigns.portofino.shiro.*
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
            return [ SecurityLogic.getAdministratorsGroup(portofinoConfiguration) ]
        } else {
            return []
        }
    }

    //--------------------------------------------------------------------------
    // Users CRUD
    //--------------------------------------------------------------------------

    Map<Serializable, String> getUsers() {
        def result = new LinkedHashMap();
        result.put(ADMIN_LOGIN, ADMIN_LOGIN);
        result.put(GUEST_LOGIN, GUEST_LOGIN);
        return result;
    }

    @Override
    String getUserPrettyName(Serializable user) {
        user
    }

    @Override
    Serializable getUserId(Serializable user) {
        user
    }

    @Override
    String getUsername(Serializable user) {
        user
    }

    @Override
    String getEmail(Serializable user) {
        throw new UnsupportedOperationException("User has no email property.");
    }

    @Override
    public Serializable getUserById(String username) {
        username
    }
    
    @Override
    public String encryptPassword(String password) {
        password
    }

}
