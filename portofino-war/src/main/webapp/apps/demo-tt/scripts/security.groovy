import com.manydesigns.portofino.PortofinoProperties
import com.manydesigns.portofino.application.Application
import com.manydesigns.portofino.shiro.ApplicationRealm
import com.manydesigns.portofino.shiro.ApplicationRealmDelegate
import com.manydesigns.portofino.shiro.GroupPermission
import com.manydesigns.portofino.system.model.users.User
import java.security.MessageDigest
import org.apache.commons.configuration.Configuration
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.hibernate.Session
import org.hibernate.criterion.Restrictions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.hibernate.SQLQuery

class Security implements ApplicationRealmDelegate {

    private static final Logger logger = LoggerFactory.getLogger(Security.class);

    AuthorizationInfo getAuthorizationInfo(ApplicationRealm realm, Object userName) {
        Application application = realm.getApplication();
        Set<String> groups = new HashSet<String>();
        Configuration conf = application.getPortofinoProperties();
        groups.add(conf.getString(PortofinoProperties.GROUP_ALL));
        if (userName == null) {
            groups.add(conf.getString(PortofinoProperties.GROUP_ANONYMOUS));
        } else {
            groups.add(conf.getString(PortofinoProperties.GROUP_REGISTERED));
            Session session = application.getSession("redmine");
            org.hibernate.Criteria criteria = session.createCriteria("users");
            criteria.add(Restrictions.eq("login", userName));
            def user = criteria.uniqueResult();
            //TODO
            if("admin".equals(user.login)) {
                groups.add(conf.getString(PortofinoProperties.GROUP_ADMINISTRATORS));
            }
        }
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(groups);
        Permission permission = new GroupPermission(groups);
        info.setObjectPermissions(Collections.singleton(permission));
        return info;
    }

    AuthenticationInfo getAuthenticationInfo(ApplicationRealm realm, String userName, String password) {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(password.getBytes("UTF-8"));
        byte[] raw = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : raw) {
            sb.append(String.format("%02X", b));
        }
        String hashedPassword = sb.toString().toLowerCase();

        Application application = realm.application;
        Session session = application.getSession("redmine");
        org.hibernate.Criteria criteria = session.createCriteria("users");
        criteria.add(Restrictions.eq("login", userName));
        criteria.add(Restrictions.eq("hashed_password", hashedPassword));

        List<Object> result = (List<Object>) criteria.list();

        if (result.size() == 1) {
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(userName, password.toCharArray(), realm.name);
            return info;
        } else {
            throw new AuthenticationException("Login failed");
        }
    }

    List<String> getUsers(ApplicationRealm realm) {
        Application application = realm.application;
        Session session = application.getSession("redmine");
        SQLQuery query = session.createSQLQuery("select \"login\" from \"users\"");
        return query.list();
    }

    List<String> getGroups(ApplicationRealm realm) {
        Application application = realm.application;
        def groups = new ArrayList<String>();
        Configuration conf = application.getPortofinoProperties();
        def group = conf.getString(PortofinoProperties.GROUP_ALL);
        groups.add(group);
        group = conf.getString(PortofinoProperties.GROUP_ANONYMOUS);
        groups.add(group);
        group = conf.getString(PortofinoProperties.GROUP_REGISTERED);
        groups.add(group);
        group = conf.getString(PortofinoProperties.GROUP_ADMINISTRATORS);
        groups.add(group);
        return groups;
    }

    private User findUserByUserName(Session session, String username) {
        org.hibernate.Criteria criteria = session.createCriteria("users");
        criteria.add(Restrictions.eq("login", username));
        return (User) criteria.uniqueResult();
    }

}