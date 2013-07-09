import com.manydesigns.elements.messages.SessionMessages
import com.manydesigns.elements.util.RandomUtil
import com.manydesigns.portofino.shiro.AbstractPortofinoRealm
import com.manydesigns.portofino.shiro.PasswordResetToken
import java.security.MessageDigest
import org.apache.commons.lang.StringUtils
import org.apache.shiro.codec.Base64
import org.apache.shiro.codec.Hex
import org.hibernate.Query
import org.hibernate.Session
import org.hibernate.criterion.Order
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.apache.shiro.authc.*

public class Security extends AbstractPortofinoRealm {

    public static final Logger logger = LoggerFactory.getLogger(Security.class);

    protected String databaseName = "$databaseName";

    protected String userTableEntityName = "$userTableEntityName";
    protected String userIdProperty = "$userIdProperty";
    protected String userNameProperty = "$userNameProperty";
    protected String userEmailProperty = "$userEmailProperty";
    protected String userTokenProperty = "$userTokenProperty";
    protected String passwordProperty = "$passwordProperty";

    protected String groupTableEntityName = "$groupTableEntityName";
    protected String groupIdProperty = "$groupIdProperty";
    protected String groupNameProperty = "$groupNameProperty";

    protected String userGroupTableEntityName = "$userGroupTableEntityName";
    protected String groupLinkProperty = "$groupLinkProperty";
    protected String userLinkProperty = "$userLinkProperty";

    protected String adminGroupName = "$adminGroupName";

    @Override
    protected Collection<String> loadAuthorizationInfo(Serializable principal) {
        def groups = []
        if(StringUtils.isEmpty(userGroupTableEntityName) || StringUtils.isEmpty(groupTableEntityName)) {
            /////////////////////////////////////////////////////////////////
            //NB admin is hardcoded for the wizard to work - remove it in production!
            /////////////////////////////////////////////////////////////////
            if("admin".equals(principal) ||
               (principal instanceof Map && "admin".equals(principal[userNameProperty]))) {
                logger.warn("Generated Security.groovy is using the hardcoded 'admin' user; " +
                            "remember to disable it in production!")
                groups.add(getAdministratorsGroup());
            }
            /////////////////////////////////////////////////////////////////
        } else {
            Session session = application.getSession(databaseName)
            def queryString = """
                select distinct g.${groupNameProperty}
                from ${groupTableEntityName} g, ${userGroupTableEntityName} ug, ${userTableEntityName} u
                where g.${groupIdProperty} = ug.${groupLinkProperty}
                and ug.${userLinkProperty} = u.${userIdProperty}
                and u.${userIdProperty} = :userId
            """
            def query = session.createQuery(queryString)
            query.setParameter("userId", principal[userIdProperty])
            groups.addAll(query.list())

            if(!StringUtils.isEmpty(adminGroupName) && groups.contains(adminGroupName)) {
                groups.add(getAdministratorsGroup());
            }
        }
        return groups
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        return loadAuthenticationInfo(token);
    }

    AuthenticationInfo loadAuthenticationInfo(UsernamePasswordToken usernamePasswordToken) {
        String userName = usernamePasswordToken.username;
        String password = new String(usernamePasswordToken.password);

        if(StringUtils.isEmpty(userTableEntityName)) {
            /////////////////////////////////////////////////////////////////
            //NB admin is hardcoded for the wizard to work - remove it in production!
            /////////////////////////////////////////////////////////////////
            if("admin".equals(userName) && "admin".equals(password)) {
                logger.warn("Generated Security.groovy is using the hardcoded 'admin' user; " +
                            "remember to disable it in production!")
                SessionMessages.addWarningMessage("Generated Security.groovy is using the hardcoded 'admin' user; " +
                                                  "remember to disable it in production!")
                SimpleAuthenticationInfo info =
                        new SimpleAuthenticationInfo(userName, password.toCharArray(), getName());
                return info;
            }
            /////////////////////////////////////////////////////////////////
        }

        String hashedPassword = encryptPassword(password);

        Session session = application.getSession(databaseName);
        org.hibernate.Criteria criteria = session.createCriteria(userTableEntityName);
        criteria.add(Restrictions.eq(userNameProperty, userName));
        criteria.add(Restrictions.eq(passwordProperty, hashedPassword));

        List result = criteria.list();

        if (result.size() == 1) {
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(result.get(0), password.toCharArray(), getName());
            return info;
        } else {
            throw new IncorrectCredentialsException("Login failed");
        }
    }

    AuthenticationInfo loadAuthenticationInfo(PasswordResetToken passwordResetToken) {
        if(StringUtils.isEmpty(userTokenProperty)) {
            throw new AuthenticationException("User token property is not configured; password reset is not supported by this application.");
        }

        Session session = application.getSession(databaseName);
        org.hibernate.Criteria criteria = session.createCriteria(userTableEntityName);
        criteria.add(Restrictions.eq(userTokenProperty, passwordResetToken.principal));

        List result = criteria.list();

        if (result.size() == 1) {
            def user = result.get(0);
            user[userTokenProperty] = null; //Consume token
            user[passwordProperty] = encryptPassword(passwordResetToken.newPassword);
            session.update(userTableEntityName, (Object) user);
            session.transaction.commit();
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(user, passwordResetToken.credentials, getName());
            return info;
        } else {
            throw new IncorrectCredentialsException("Invalid token");
        }
    }

    @Override
    boolean supports(AuthenticationToken token) {
        if(token instanceof PasswordResetToken) {
            return !StringUtils.isEmpty(userTokenProperty);
        }
        return super.supports(token)
    }

    @Override
    void changePassword(Serializable user, String oldPassword, String newPassword) {
        if(StringUtils.isEmpty(userTableEntityName)) {
            throw new UnsupportedOperationException("User table is not configured");
        }
        Session session = application.getSession(databaseName);
        def q = session.createQuery("""
                update $userTableEntityName set ${passwordProperty} = :newPwd
                where $userIdProperty = :id and ${passwordProperty} = :oldPwd""");
        q.setParameter("newPwd", encryptPassword(newPassword));
        q.setParameter("id", user[userIdProperty]);
        q.setParameter("oldPwd", encryptPassword(oldPassword));
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

    String encryptPassword(String password) {
        return this.$encryptionAlgorithm(password)
    }

    Set<String> getUsers() {
        def users = new HashSet<String>();
        Session session = application.getSession(databaseName);
        Query query = session.createQuery("select " + userNameProperty + " from " + userTableEntityName);
        users.addAll(query.list());
        return users;
    }

    Serializable getUserByEmail(String email) {
        if(StringUtils.isEmpty(userEmailProperty)) {
            throw new UnsupportedOperationException("Email property not configured.");
        }
        Session session = application.getSession(databaseName);
        def criteria = session.createCriteria(userTableEntityName);
        criteria.add(Restrictions.eq(userEmailProperty, email));
        return (Serializable) criteria.uniqueResult();
    }

    @Override
    String generateOneTimeToken(Serializable user) {
        if(StringUtils.isEmpty(userTokenProperty)) {
            throw new UnsupportedOperationException("Token property not configured.");
        }
        Session session = application.getSession(databaseName);
        user = (Serializable) session.get(userTableEntityName, user[userIdProperty]);
        String token = RandomUtil.createRandomId(20);
        user[userTokenProperty] = token;
        session.update(user);
        session.transaction.commit();
        return token;
    }

    Set<String> getGroups() {
        def groups = super.getGroups()

        if(!StringUtils.isEmpty(groupTableEntityName)) {
            Session session = application.getSession(databaseName)
            def criteria = session.createCriteria(groupTableEntityName)
            criteria.projection = Projections.property(groupNameProperty)
            criteria.addOrder(Order.asc(groupNameProperty))
            for(x in criteria.list()) {
                groups.add(String.valueOf(x))
            }
        }
        return groups;
    }

    def sha1Hex(String password) {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(password.getBytes("UTF-8"));
        byte[] raw = md.digest();
        return toHex(raw);
    }

    def sha1Base64(String password) {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(password.getBytes("UTF-8"));
        byte[] raw = md.digest();
        return toBase64(raw);
    }

    def md5Hex(String password) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes("UTF-8"));
        byte[] raw = md.digest();
        return toHex(raw);
    }

    def md5Base64(String password) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes("UTF-8"));
        byte[] raw = md.digest();
        return toBase64(raw);
    }

    def plaintext(String password) {
        return password
    }

    protected String toHex(byte[] raw) {
        return Hex.encodeToString(raw)
    }

    protected String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes)
    }

}

