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
        String hashedPassword = hashPassword(password);

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

    protected String hashPassword(String password) {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(password.getBytes("UTF-8"));
        byte[] raw = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b: raw) {
            sb.append(String.format("%02X", b));
        }
        String hashedPassword = sb.toString().toLowerCase()
        return hashedPassword
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

    @Override
    void changePassword(Serializable user, String oldPassword, String newPassword) {
        def session = application.getSession("redmine")
        def q = session.createQuery("update users set hashed_password = :newPwd where id = :id and hashed_password = :oldPwd");
        q.setParameter("newPwd", hashPassword(newPassword));
        q.setParameter("oldPwd", hashPassword(oldPassword));
        q.setParameter("id", user.id);
        int rows = q.executeUpdate();
        if(rows == 0) {
            //Probably the password did not match
            throw new IncorrectCredentialsException("The password update query modified 0 rows. This most probably means that the old password is wrong. It may also mean that the user has been deleted.");
        } else if(rows > 1) {
            throw new Error("Password update query modified more than 1 row! Rolling back.");
        } else {
            session.transaction.commit();
        }
    }

}