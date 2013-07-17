import com.manydesigns.elements.reflection.ClassAccessor
import com.manydesigns.elements.reflection.JavaClassAccessor
import com.manydesigns.elements.util.RandomUtil
import com.manydesigns.portofino.AppProperties
import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.model.database.Database
import com.manydesigns.portofino.model.database.DatabaseLogic
import com.manydesigns.portofino.model.database.Table
import com.manydesigns.portofino.modules.DatabaseModule
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.reflection.TableAccessor
import com.manydesigns.portofino.shiro.AbstractPortofinoRealm
import com.manydesigns.portofino.shiro.PasswordResetToken
import com.manydesigns.portofino.shiro.SignUpToken
import com.manydesigns.portofino.shiro.openid.OpenIDToken
import java.security.MessageDigest
import org.hibernate.Criteria
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.criterion.Restrictions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.apache.shiro.authc.*

class Security extends AbstractPortofinoRealm {

    private static final Logger logger = LoggerFactory.getLogger(Security.class);

    @Inject(DatabaseModule.PERSISTENCE)
    Persistence persistence;

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

        Session session = persistence.getSession("redmine");
        org.hibernate.Criteria criteria = session.createCriteria("users");
        criteria.add(Restrictions.eq("login", userName));
        criteria.add(Restrictions.eq("hashed_password", hashedPassword));

        Serializable principal = (Serializable) criteria.uniqueResult();

        if (principal == null) {
            throw new AuthenticationException("Login failed");
        } else {
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(
                            principal, password.toCharArray(), getName());
            return info;
        }
    }

    public AuthenticationInfo loadAuthenticationInfo(OpenIDToken openIDToken) {
        Session session = persistence.getSession("redmine");
        org.hibernate.Criteria criteria = session.createCriteria("users");
        if(openIDToken.firstLoginToken != null) {
            criteria.add(Restrictions.eq("token", openIDToken.firstLoginToken));
        } else {
            criteria.add(Restrictions.eq("identity_url", openIDToken.principal.identifier));
        }

        Serializable principal = (Serializable) criteria.uniqueResult();

        if (principal == null) {
            throw new UnknownAccountException();
        } else {
            if(openIDToken.firstLoginToken != null) {
                session.beginTransaction();
                principal.token = null; //Consume token
                principal.identity_url = openIDToken.principal.identifier;
                session.update("users", (Object) principal);
                session.transaction.commit();
            }
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(
                            principal, openIDToken.credentials, getName());
            return info;
        }
    }

    AuthenticationInfo loadAuthenticationInfo(PasswordResetToken passwordResetToken) {
        return setNewPassword(passwordResetToken)
    }

    AuthenticationInfo loadAuthenticationInfo(SignUpToken signUpToken) {
        return setNewPassword(signUpToken)
    }

    protected SimpleAuthenticationInfo setNewPassword(token) {
        Session session = persistence.getSession("redmine");
        Criteria criteria = session.createCriteria("users");
        criteria.add(Restrictions.eq("token", token.principal));

        List result = criteria.list();

        if (result.size() == 1) {
            def user = result.get(0);
            user.token = null; //Consume token
            user.hashed_password = hashPassword(token.newPassword);
            session.update("users", (Object) user);
            session.transaction.commit();
            SimpleAuthenticationInfo info =
                new SimpleAuthenticationInfo(user, token.credentials, getName());
            return info;
        } else {
            throw new IncorrectCredentialsException("Invalid token");
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
    Map<Serializable, String> getUsers() {
        Session session = persistence.getSession("redmine");
        SQLQuery query = session.createSQLQuery("select \"id\", \"login\" from \"users\" order by \"login\"");
        def users = new LinkedHashMap();
        for(Object[] user : query.list()) {
            users.put(user[0], user[1]);
        }
        return users;
    }

    @Override
    Serializable getUserById(String encodedUserId) {
        Session session = persistence.getSession("redmine");
        return (Serializable) session.get("users", Integer.parseInt(encodedUserId));
    }

    Serializable getUserByEmail(String email) {
        Session session = persistence.getSession("redmine");
        def criteria = session.createCriteria("users");
        criteria.add(Restrictions.eq("mail", email));
        return (Serializable) criteria.uniqueResult();
    }

    @Override
    String getUserPrettyName(Serializable user) {
        return "${user.firstname} ${user.lastname}";
    }

    Serializable getUserId(Serializable user) {
        return user.id
    }

    @Override
    Serializable saveUser(Serializable user) {
        def session = persistence.getSession("redmine");
        session.save("users", (Object) user);
        session.transaction.commit();
        return user;
    }

    @Override
    Serializable updateUser(Serializable user) {
        def session = persistence.getSession("redmine");
        session.update("users", (Object) user);
        session.transaction.commit();
        return user;
    }

    @Override
    ClassAccessor getUserClassAccessor() {
        Database database =
            DatabaseLogic.findDatabaseByName(persistence.model, "redmine");
        Table table =
            DatabaseLogic.findTableByEntityName(database, "users");
        return new TableAccessor(table);
    }

    @Override
    void changePassword(Serializable user, String oldPassword, String newPassword) {
        def session = persistence.getSession("redmine")
        def q = session.createQuery(
                "update users set hashed_password = :newPwd where id = :id and hashed_password = :oldPwd");
        q.setParameter("newPwd", hashPassword(newPassword));
        q.setParameter("oldPwd", hashPassword(oldPassword));
        q.setParameter("id", user.id);
        int rows = q.executeUpdate();
        if(rows == 0) {
            //Probably the password did not match
            throw new IncorrectCredentialsException(
                    "The password update query modified 0 rows. " +
                    "This most probably means that the old password is wrong. " +
                    "It may also mean that the user has been deleted.");
        } else if(rows > 1) {
            throw new Error("Password update query modified more than 1 row! Rolling back.");
        } else {
            session.transaction.commit();
        }
    }

    @Override
    String generateOneTimeToken(Serializable user) {
        Session session = persistence.getSession("redmine");
        user = (Serializable) session.get("users", user.id);
        String token = RandomUtil.createRandomId(20);
        user.token = token;
        session.update("users", (Object) user);
        session.transaction.commit();
        return token;
    }

    String saveSelfRegisteredUser(Object user) {
        DemoUser theUser = (DemoUser) user;
        Session session = persistence.getSession("redmine");
        Map persistentUser = new HashMap();
        persistentUser.login = theUser.username;
        persistentUser.mail = theUser.email;
        persistentUser.hashed_password = RandomUtil.createRandomId(32);
        persistentUser.firstname = theUser.firstname;
        persistentUser.lastname = theUser.lastname;
        persistentUser.admin = false;
        persistentUser.status = 0;
        persistentUser.mail_notification = "";

        String token = RandomUtil.createRandomId(20);
        persistentUser.token = token;

        session.save("users", (Object) persistentUser);
        session.transaction.commit();
        return token;
    }

    @Override
    ClassAccessor getSelfRegisteredUserClassAccessor() {
        return JavaClassAccessor.getClassAccessor(DemoUser.class)
    }

    @Override
    boolean supports(AuthenticationToken token) {
        return (token instanceof PasswordResetToken) ||
               (token instanceof SignUpToken) ||
               (token instanceof OpenIDToken) ||
               super.supports(token);
    }

}