import com.manydesigns.portofino.security.SecurityLogic;
import com.manydesigns.portofino.shiro.AbstractPortofinoRealm;
import org.apache.shiro.authc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Security extends AbstractPortofinoRealm {

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
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;
        String userName = usernamePasswordToken.getUsername();
        String password = new String(usernamePasswordToken.getPassword());
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
            return Collections.singletonList(SecurityLogic.getAdministratorsGroup(portofinoConfiguration));
        } else {
            return Collections.emptyList();
        }
    }

    //--------------------------------------------------------------------------
    // Users CRUD
    //--------------------------------------------------------------------------

    public Map<Serializable, String> getUsers() {
        Map result = new LinkedHashMap();
        result.put(ADMIN_LOGIN, ADMIN_LOGIN);
        result.put(GUEST_LOGIN, GUEST_LOGIN);
        return result;
    }

    @Override
    public String getUserPrettyName(Serializable user) {
        return (String) user;
    }

    @Override
    public Serializable getUserId(Serializable user) {
        return user;
    }

    @Override
    public String getUsername(Serializable user) {
        return (String) user;
    }

    @Override
    public String getEmail(Serializable user) {
        throw new UnsupportedOperationException("User has no email property.");
    }

    @Override
    public Serializable getUserById(String username) {
        return username;
    }

    @Override
    public String encryptPassword(String password) {
        return password;
    }

}
