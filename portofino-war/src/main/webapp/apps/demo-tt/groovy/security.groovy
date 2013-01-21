import com.manydesigns.portofino.PortofinoProperties
import com.manydesigns.portofino.application.Application
import com.manydesigns.portofino.shiro.AbstractApplicationRealmDelegate
import com.manydesigns.portofino.shiro.ApplicationRealm
import java.security.MessageDigest
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.criterion.Restrictions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.manydesigns.portofino.application.AppProperties

class Security extends AbstractApplicationRealmDelegate {

    private static final Logger logger = LoggerFactory.getLogger(Security.class);

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

        List result = criteria.list();

        if (result.size() == 1) {
            def user = result.get(0);
            PrincipalCollection loginAndId = new SimplePrincipalCollection(userName, realm.name);
            loginAndId.add(user.id, realm.name);
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(loginAndId, password.toCharArray(), realm.name);
            return info;
        } else {
            throw new AuthenticationException("Login failed");
        }
    }

    Set<String> getUsers(ApplicationRealm realm) {
        Application application = realm.application;
        Session session = application.getSession("redmine");
        SQLQuery query = session.createSQLQuery("select \"login\" from \"users\"");
        return new LinkedHashSet<String>(query.list());
    }

    @Override
    protected Collection<String> loadAuthorizationInfo(
            ApplicationRealm realm, PrincipalCollection principalCollection) {
        Session session = realm.application.getSession("redmine");
        def userId = (Integer) principalCollection.asList().get(1);
        logger.debug("Loading user with id = {}", userId);
        def user = session.load("users", userId);
        if("admin".equals(user.login)) {
            return [realm.application.appConfiguration.getString(AppProperties.GROUP_ADMINISTRATORS)]
        } else {
            return []
        }
    }

    @Override
    protected Collection<String> loadAuthorizationInfo(ApplicationRealm realm, String principal) {
        Session session = realm.application.getSession("redmine");
        logger.debug("Loading user with login = {}", principal);
        org.hibernate.Criteria criteria = session.createCriteria("users");
        criteria.add(Restrictions.eq("login", principal));
        def user = criteria.uniqueResult();
        if(user != null && "admin".equals(user.login)) {
            return [realm.application.appConfiguration.getString(AppProperties.GROUP_ADMINISTRATORS)]
        } else {
            return []
        }
    }

}