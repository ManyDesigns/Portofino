//import com.google.appengine.api.users.User


//import com.manydesigns.portofino.shiro.GAEPortofinoRealm


import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.reflection.ClassAccessor
import com.manydesigns.mail.stripes.SendMailAction
import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.logic.SecurityLogic
import com.manydesigns.portofino.model.database.Database
import com.manydesigns.portofino.model.database.DatabaseLogic
import com.manydesigns.portofino.model.database.Table
import com.manydesigns.portofino.modules.DatabaseModule
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.reflection.TableAccessor
import com.manydesigns.portofino.shiro.AbstractPortofinoRealm
import com.manydesigns.portofino.shiro.ServletContainerToken
import java.security.MessageDigest
import org.apache.shiro.crypto.hash.Md5Hash
import org.hibernate.Criteria
import org.hibernate.Session
import org.hibernate.criterion.Restrictions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.apache.shiro.authc.*

class Security extends AbstractPortofinoRealm {

    public static final String ADMIN_GROUP_NAME = "admin";

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

// 
//    protected AuthenticationInfo loadAuthenticationInfo(ServletContainerToken token) {
//        def info = super.doGetAuthenticationInfo(token);
//        User user = (User) info.principals.asList()[0];
//
//        Session session = persistence.getSession("tt");
//        Criteria criteria = session.createCriteria("users");
//        criteria.add(Restrictions.eq("email", user.email));
//
//        Serializable principal = (Serializable)criteria.uniqueResult();
//
//        if (principal == null) {
//            throw new UnknownAccountException("Unknown user");
//        } else if (!principal.validated) {
//            throw new DisabledAccountException("User not validated");
//        } else {
//            logger.debug("Aggiorno i campi accesso.");
//            updateAccess(principal, new Date());
//            session.update("users", (Object)principal);
//            session.getTransaction().commit();
//        }
//
//        /*def pc = new SimplePrincipalCollection([principal, user], getName());
//        SimpleAuthenticationInfo infoEx =
//                new SimpleAuthenticationInfo(pc, "", getName());
//        return infoEx;*/
//        return new SimpleAuthenticationInfo(principal, "", getName());
//    }

    public AuthenticationInfo loadAuthenticationInfo(UsernamePasswordToken usernamePasswordToken) {
        String login = usernamePasswordToken.username;
        String plainTextPassword;
        if (usernamePasswordToken.password == null) {
            plainTextPassword = "";
        } else {
            plainTextPassword = new String(usernamePasswordToken.password);
        }

        String encryptedPassword = encryptPassword(plainTextPassword);
        Session session = persistence.getSession("tt");

        Criteria criteria = session.createCriteria("users");
        criteria.add(Restrictions.eq("email", login));

        Serializable principal = (Serializable)criteria.uniqueResult();

        if (principal == null) {
            throw new UnknownAccountException("Unknown user");
        } else if (!encryptedPassword.equals(principal.password)) {
            throw new IncorrectCredentialsException("Wrong password");
        } else if (!principal.validated) {
            throw new DisabledAccountException("User not validated");
        } else {
            logger.debug("Aggiorno i campi accesso.");
            updateAccess(principal, new Date());
            session.update("users", (Object)principal);
            session.getTransaction().commit();
        }

        SimpleAuthenticationInfo info =
                new SimpleAuthenticationInfo(
                        principal, plainTextPassword.toCharArray(), getName());
        return info;
    }

    private void updateAccess(Object principal, Date now) {
        def request = ElementsThreadLocals.getHttpServletRequest();
        principal.last_access = now;
        principal.access_ip = request.getRemoteAddr();
    }


    public String encryptPassword(String plainText) {
        Md5Hash md5Hash = new Md5Hash(plainText);
        String encrypted = md5Hash.toBase64();
        return encrypted;
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

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof ServletContainerToken ||
               token instanceof UsernamePasswordToken;
    }

    //--------------------------------------------------------------------------
    // Authorization
    //--------------------------------------------------------------------------

    @Override
    protected Collection<String> loadAuthorizationInfo(Serializable principal) {
        List<String> result = new ArrayList<String>()
        if (principal.admin) {
            result.add(ADMIN_GROUP_NAME);
//            UserService userService = UserServiceFactory.getUserService();
//            if (userService.isUserAdmin()) {
            if (isLocalUser()) {
                result.add(SecurityLogic.getAdministratorsGroup(portofinoConfiguration));
            }
        }
        return result;
    }

    protected boolean isLocalUser() {
        String remoteIp =
            ElementsThreadLocals.getHttpServletRequest().getRemoteAddr();
        InetAddress clientAddr = InetAddress.getByName(remoteIp);
        return SendMailAction.isLocalIPAddress(clientAddr)
    }


    //--------------------------------------------------------------------------
    // Users crud
    //--------------------------------------------------------------------------

    @Override
    Map<Serializable, String> getUsers() {
        Session session = persistence.getSession("tt");
        Criteria criteria = session.createCriteria("users");
        List users = criteria.list();

        Map result = new HashMap();
        for (Serializable user : users) {
            long id = user.id;
            String prettyName = getUserPrettyName(user);
            result.put(id, prettyName);
        }

        return result;
    }

    @Override
    Serializable getUserById(String encodedUserId) {
        Session session = persistence.getSession("tt");
        return (Serializable) session.get("users", Long.parseLong(encodedUserId));
    }

    Serializable getUserByEmail(String email) {
        Session session = persistence.getSession("tt");
        def criteria = session.createCriteria("users");
        criteria.add(Restrictions.eq("email", email));
        return (Serializable) criteria.uniqueResult();
    }

    @Override
    String getUserPrettyName(Serializable user) {
        return "${user.first_name} ${user.last_name}";
    }

    Serializable getUserId(Serializable user) {
        return user.id
    }

    @Override
    Serializable saveUser(Serializable user) {
        def session = persistence.getSession("tt");
        session.save("users", (Object) user);
        session.transaction.commit();
        return user;
    }

    @Override
    Serializable updateUser(Serializable user) {
        def session = persistence.getSession("tt");
        session.update("users", (Object) user);
        session.transaction.commit();
        return user;
    }

    @Override
    ClassAccessor getUserClassAccessor() {
        Database database =
            DatabaseLogic.findDatabaseByName(persistence.model, "tt");
        Table table =
            DatabaseLogic.findTableByEntityName(database, "users");
        return new TableAccessor(table);
    }

}