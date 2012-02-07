import com.manydesigns.portofino.application.Application
import com.manydesigns.portofino.logic.SecurityLogic
import com.manydesigns.portofino.shiro.ApplicationRealm
import com.manydesigns.portofino.shiro.ApplicationRealmDelegate
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.SimpleAuthorizationInfo
import com.manydesigns.portofino.shiro.GroupPermission
import org.apache.shiro.authz.Permission

class Security implements ApplicationRealmDelegate {


    AuthorizationInfo getAuthorizationInfo(ApplicationRealm realm, String userName) {
        Application application = realm.getApplication();
        Set<String> roleNames = new HashSet<String>(SecurityLogic.getUserGroups(application, userName));
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roleNames);
        Permission permission = new GroupPermission(roleNames);
        info.setObjectPermissions(Collections.singleton(permission));
        return info;
    }

    AuthenticationInfo getAuthenticationInfo(ApplicationRealm realm, String userName, String password) {
        Application application = realm.application;
        String userId = SecurityLogic.defaultLogin(application, userName, password);
        if(userId != null) {
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(userId, password.toCharArray(), realm.name);
            return info;
        } else {
            throw new AuthenticationException("Login failed");
        }
    }

}