import com.manydesigns.portofino.application.AppProperties
import com.manydesigns.portofino.shiro.AbstractPortofinoRealm
import java.security.MessageDigest
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.criterion.Restrictions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.apache.shiro.authc.*

class Security extends AbstractPortofinoRealm {

    private static final Logger logger = LoggerFactory.getLogger(Security.class);

    //--------------------------------------------------------------------------
    // Authentication
    //--------------------------------------------------------------------------

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

        Serializable principal = criteria.uniqueResult();

        if (principal == null) {
            throw new AuthenticationException("Login failed");
        } else {
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(
                            principal, password.toCharArray(), getName());
            return info;
        }
    }

    //--------------------------------------------------------------------------
    // Authorization
    //--------------------------------------------------------------------------

    @Override
    protected Collection<String> loadAuthorizationInfo(Serializable principal) {
        if("admin".equals(principal.login)) {
            return [portofinoConfiguration.getString(AppProperties.GROUP_ADMINISTRATORS)]
        } else {
            return []
        }
    }

    //--------------------------------------------------------------------------
    // Users crud
    //--------------------------------------------------------------------------

    @Override
    Set<String> getUsers() {
        Session session = application.getSession("redmine");
        SQLQuery query = session.createSQLQuery("select \"login\" from \"users\"");
        return new LinkedHashSet<String>(query.list());
    }

}