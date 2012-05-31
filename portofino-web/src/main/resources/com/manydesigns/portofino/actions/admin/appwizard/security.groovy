import com.manydesigns.portofino.PortofinoProperties
import com.manydesigns.portofino.application.Application
import com.manydesigns.portofino.shiro.ApplicationRealm
import com.manydesigns.portofino.shiro.ApplicationRealmDelegate
import com.manydesigns.portofino.shiro.GroupPermission
import org.apache.commons.configuration.Configuration
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.IncorrectCredentialsException
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection
import org.hibernate.Query
import org.hibernate.Session
import org.hibernate.criterion.Restrictions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Security implements ApplicationRealmDelegate {

    public static final Logger logger = LoggerFactory.getLogger(Security.class);

    protected String databaseName = "<%= databaseName %>";
    protected String userTableEntityName = "<%= userTableEntityName %>";
    protected String userIdProperty = "<%= userIdProperty %>";
    protected String userNameProperty = "<%= userNameProperty %>";
    protected String passwordProperty = "<%= passwordProperty %>";

    AuthorizationInfo getAuthorizationInfo(ApplicationRealm realm, principal) {
        Application application = realm.getApplication();
        Set<String> groups = new HashSet<String>();
        Configuration conf = application.getPortofinoProperties();
        groups.add(conf.getString(PortofinoProperties.GROUP_ALL));
        if (principal == null) {
            groups.add(conf.getString(PortofinoProperties.GROUP_ANONYMOUS));
        } else if(principal instanceof PrincipalCollection) {
            groups.add(conf.getString(PortofinoProperties.GROUP_REGISTERED));
        } else if(principal instanceof String) {
            groups.add(conf.getString(PortofinoProperties.GROUP_REGISTERED));
        } else {
            throw new AuthorizationException("Invalid principal: " + principal);
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(groups);
        Permission permission = new GroupPermission(groups);
        info.setObjectPermissions(Collections.singleton(permission));
        return info;
    }

    AuthenticationInfo getAuthenticationInfo(ApplicationRealm realm, String userName, String password) {
        String hashedPassword = encryptPassword(password);

        Application application = realm.application;
        Session session = application.getSession(databaseName);
        org.hibernate.Criteria criteria = session.createCriteria(userTableEntityName);
        criteria.add(Restrictions.eq(userNameProperty, userName));
        criteria.add(Restrictions.eq(passwordProperty, hashedPassword));

        List result = criteria.list();

        if (result.size() == 1) {
            def user = result.get(0);
            PrincipalCollection loginAndId = new SimplePrincipalCollection(userName, realm.name);
            loginAndId.add(user.get(userIdProperty), realm.name);
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(loginAndId, password.toCharArray(), realm.name);
            return info;
        } else {
            throw new IncorrectCredentialsException("Login failed");
        }
    }

    String encryptPassword(String password) {
        return password
    }

    Set<String> getUsers(ApplicationRealm realm) {
        Application application = realm.application;
        def users = new HashSet<String>();
        Session session = application.getSession(databaseName);
        Query query = session.createQuery("select " + userNameProperty + " from " + userTableEntityName);
        users.addAll(query.list());
        return users;
    }

    Set<String> getGroups(ApplicationRealm realm) {
        Application application = realm.application;
        def groups = new HashSet<String>();
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

}
