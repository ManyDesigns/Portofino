import com.manydesigns.elements.util.RandomUtil
import com.manydesigns.portofino.PortofinoProperties
import com.manydesigns.portofino.application.Application
import com.manydesigns.portofino.application.QueryUtils
import com.manydesigns.portofino.shiro.ApplicationRealm
import com.manydesigns.portofino.shiro.ApplicationRealmDelegate
import com.manydesigns.portofino.shiro.GroupPermission
import com.manydesigns.portofino.system.model.users.User
import com.manydesigns.portofino.system.model.users.UserConstants
import com.manydesigns.portofino.system.model.users.UsersGroups
import java.sql.Timestamp
import org.apache.commons.configuration.Configuration
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.DisabledAccountException
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.Transaction
import org.hibernate.criterion.Restrictions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Security implements ApplicationRealmDelegate {

    private static final Logger logger = LoggerFactory.getLogger(Security.class);

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    AuthorizationInfo getAuthorizationInfo(ApplicationRealm realm, Object userName) {
        Application application = realm.getApplication();
        Set<String> groups = new HashSet<String>();
        Configuration conf = application.getPortofinoProperties();
        groups.add(conf.getString(PortofinoProperties.GROUP_ALL));
        if (userName == null) {
            groups.add(conf.getString(PortofinoProperties.GROUP_ANONYMOUS));
        } else {
            groups.add(conf.getString(PortofinoProperties.GROUP_REGISTERED));

            if (ADMIN_LOGIN.equals(userName)) {
                groups.add(conf.getString(PortofinoProperties.GROUP_ADMINISTRATORS));
            }
        }
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(groups);
        Permission permission = new GroupPermission(groups);
        info.setObjectPermissions(Collections.singleton(permission));
        return info;
    }

    AuthenticationInfo getAuthenticationInfo(ApplicationRealm realm, String userName, String password) {
        if (ADMIN_LOGIN.equals(userName) && ADMIN_PASSWORD.equals(password)) {
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
        return result;
    }

    Set<String> getGroups(ApplicationRealm realm) {
        Application application = realm.getApplication();
        Configuration conf = application.getPortofinoProperties();
        Set<String> result = new LinkedHashSet<String>();
        result.add(conf.getString(PortofinoProperties.GROUP_ALL));
        result.add(conf.getString(PortofinoProperties.GROUP_ANONYMOUS));
        result.add(conf.getString(PortofinoProperties.GROUP_REGISTERED));
        result.add(conf.getString(PortofinoProperties.GROUP_ADMINISTRATORS));
        return result;
    }
}
