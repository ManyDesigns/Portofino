import com.manydesigns.elements.messages.SessionMessages
import com.manydesigns.portofino.actions.admin.appwizard.User
import com.manydesigns.portofino.shiro.AbstractPortofinoRealm
import java.security.MessageDigest
import org.apache.commons.lang.StringUtils
import org.apache.shiro.codec.Base64
import org.apache.shiro.codec.Hex
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection
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
    protected String passwordProperty = "$passwordProperty";

    protected String groupTableEntityName = "$groupTableEntityName";
    protected String groupIdProperty = "$groupIdProperty";
    protected String groupNameProperty = "$groupNameProperty";

    protected String userGroupTableEntityName = "$userGroupTableEntityName";
    protected String groupLinkProperty = "$groupLinkProperty";
    protected String userLinkProperty = "$userLinkProperty";

    protected String adminGroupName = "$adminGroupName";

    @Override
    protected Collection<String> loadAuthorizationInfo(PrincipalCollection principalCollection) {
        def groups = []
        if(StringUtils.isEmpty(userGroupTableEntityName) || StringUtils.isEmpty(groupTableEntityName)) {
            /////////////////////////////////////////////////////////////////
            //NB admin is hardcoded for the wizard to work - remove it in production!
            /////////////////////////////////////////////////////////////////
            if("admin".equals(principalCollection.asList().get(0))) {
                logger.warn("Generated security.groovy is using the hardcoded 'admin' user; " +
                            "remember to disable it in production!")
                groups.add(getAdministratorsGroup())
            }
            /////////////////////////////////////////////////////////////////
        } else {
            Session session = application.getSession(databaseName)
            def queryString = """
                select distinct g.${groupNameProperty}
                from ${groupTableEntityName} g, ${userGroupTableEntityName} ug
                where g.${groupIdProperty} = ug.${groupLinkProperty}
                and ug.${userLinkProperty} = :userId
            """
            def query = session.createQuery(queryString)
            query.setParameter("userId", principalCollection.asList().get(1).getDatabaseId())
            groups.addAll(query.list())

            if(!StringUtils.isEmpty(adminGroupName) && groups.contains(adminGroupName)) {
                groups.add(getAdministratorsGroup())
            }
        }
        return groups
    }

    @Override
    protected Collection<String> loadAuthorizationInfo(String principal) {
        def groups = []
        if(StringUtils.isEmpty(userGroupTableEntityName) || StringUtils.isEmpty(groupTableEntityName)) {
            /////////////////////////////////////////////////////////////////
            //NB admin is hardcoded for the wizard to work - remove it in production!
            /////////////////////////////////////////////////////////////////
            if("admin".equals(principal)) {
                logger.warn("Generated security.groovy is using the hardcoded 'admin' user; " +
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
                and u.${userNameProperty} = :principal
            """
            def query = session.createQuery(queryString)
            query.setParameter("principal", principal)
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

        String hashedPassword = encryptPassword(password);

        Session session = application.getSession(databaseName);
        org.hibernate.Criteria criteria = session.createCriteria(userTableEntityName);
        criteria.add(Restrictions.eq(userNameProperty, userName));
        criteria.add(Restrictions.eq(passwordProperty, hashedPassword));

        List result = criteria.list();

        if (result.size() == 1) {
            def user = new User();
            user.username = userName;
            user.databaseId = result.get(0).get(userIdProperty);
            PrincipalCollection loginAndUser = new SimplePrincipalCollection(userName, getName());
            loginAndUser.add(user, getName());
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(loginAndUser, password.toCharArray(), getName());
            return info;
        } else {
            /////////////////////////////////////////////////////////////////
            //NB admin is hardcoded for the wizard to work - remove it in production!
            /////////////////////////////////////////////////////////////////
            if("admin".equals(userName) && "admin".equals(password)) {
                logger.warn("Generated security.groovy is using the hardcoded 'admin' user; " +
                            "remember to disable it in production!")
                SessionMessages.addWarningMessage("Generated security.groovy is using the hardcoded 'admin' user; " +
                                                  "remember to disable it in production!")
                SimpleAuthenticationInfo info =
                        new SimpleAuthenticationInfo(userName, password.toCharArray(), getName());
                return info;
            }
            /////////////////////////////////////////////////////////////////
            throw new IncorrectCredentialsException("Login failed");
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

