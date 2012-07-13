import com.manydesigns.portofino.shiro.AbstractApplicationRealmDelegate
import com.manydesigns.portofino.shiro.ApplicationRealm
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Security extends AbstractApplicationRealmDelegate {

    private static final Logger logger = LoggerFactory.getLogger(Security.class);

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    private static final String GUEST_LOGIN = "guest";
    private static final String GUEST_PASSWORD = "guest";

    @Override
    protected Collection<String> loadAuthorizationInfo(ApplicationRealm realm, String principal) {
        if (ADMIN_LOGIN.equals(principal)) {
            return [ getAdministratorsGroup(realm) ]
        } else {
            return []
        }
    }

    AuthenticationInfo getAuthenticationInfo(ApplicationRealm realm, String userName, String password) {
        if (ADMIN_LOGIN.equals(userName) && ADMIN_PASSWORD.equals(password) ||
            GUEST_LOGIN.equals(userName) && GUEST_PASSWORD.equals(password)) {
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(userName, password.toCharArray(), realm.name);
            return info;
        } else {
            throw new AuthenticationException("Login failed");
        }
    }

    Set<String> getUsers(ApplicationRealm realm) {
        Set<String> result = new LinkedHashSet<String>();
        result.add(ADMIN_LOGIN);
        result.add(GUEST_LOGIN);
        return result;
    }

}
