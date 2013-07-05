import com.manydesigns.portofino.application.AppProperties
import com.manydesigns.portofino.shiro.AbstractPortofinoRealm
import java.security.MessageDigest
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.criterion.Restrictions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.apache.shiro.authc.*

class Security extends AbstractPortofinoRealm {

    private static final Logger logger = LoggerFactory.getLogger(Security.class);


    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        return loadAuthenticationInfo(token);
    }


    public AuthenticationInfo loadAuthenticationInfo(UsernamePasswordToken usernamePasswordToken) {
        String userName = usernamePasswordToken.username;
        String password = new String(usernamePasswordToken.password);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(password.getBytes("UTF-8"));
        byte[] raw = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : raw) {
            sb.append(String.format("%02X", b));
        }
        String hashedPassword = sb.toString().toLowerCase();

        Session session = application.getSession("redmine");
        org.hibernate.Criteria criteria = session.createCriteria("users");
        criteria.add(Restrictions.eq("login", userName));
        criteria.add(Restrictions.eq("hashed_password", hashedPassword));

        List result = criteria.list();

        if (result.size() == 1) {
            def user = result.get(0);
            PrincipalCollection loginAndId = new SimplePrincipalCollection(userName, getName());
            loginAndId.add(user.id, getName());
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(loginAndId, password.toCharArray(), getName());
            return info;
        } else {
            throw new AuthenticationException("Login failed");
        }
    }

    @Override
    Set<String> getUsers() {
        Session session = application.getSession("redmine");
        SQLQuery query = session.createSQLQuery("select \"login\" from \"users\"");
        return new LinkedHashSet<String>(query.list());
    }

    @Override
    protected Collection<String> loadAuthorizationInfo(
            PrincipalCollection principalCollection) {
        Session session = application.getSession("redmine");
        def userId = (Integer) principalCollection.asList().get(1);
        logger.debug("Loading user with id = {}", userId);
        def user = session.load("users", userId);
        if("admin".equals(user.login)) {
            return [portofinoConfiguration.getString(AppProperties.GROUP_ADMINISTRATORS)]
        } else {
            return []
        }
    }

    @Override
    protected Collection<String> loadAuthorizationInfo(String principal) {
        Session session = application.getSession("redmine");
        logger.debug("Loading user with login = {}", principal);
        org.hibernate.Criteria criteria = session.createCriteria("users");
        criteria.add(Restrictions.eq("login", principal));
        def user = criteria.uniqueResult();
        if(user != null && "admin".equals(user.login)) {
            return [portofinoConfiguration.getString(AppProperties.GROUP_ADMINISTRATORS)]
        } else {
            return []
        }
    }

}