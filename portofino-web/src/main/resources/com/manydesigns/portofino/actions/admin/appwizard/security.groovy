import com.manydesigns.portofino.PortofinoProperties
import com.manydesigns.portofino.application.Application
import com.manydesigns.portofino.shiro.ApplicationRealm
import com.manydesigns.portofino.shiro.ApplicationRealmDelegate
import com.manydesigns.portofino.shiro.GroupPermission
import java.security.MessageDigest
import org.apache.commons.configuration.Configuration
import org.apache.commons.lang.StringUtils
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.IncorrectCredentialsException
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.apache.shiro.codec.Base64
import org.apache.shiro.codec.Hex
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection
import org.hibernate.Query
import org.hibernate.Session
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Security implements ApplicationRealmDelegate {

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

    AuthorizationInfo getAuthorizationInfo(ApplicationRealm realm, principal) {
        Application application = realm.getApplication();
        Set<String> groups = new HashSet<String>();
        Configuration conf = application.getPortofinoProperties();
        groups.add(conf.getString(PortofinoProperties.GROUP_ALL));
        if (principal == null) {
            groups.add(conf.getString(PortofinoProperties.GROUP_ANONYMOUS));
        } else if(principal instanceof PrincipalCollection) {
            groups.add(conf.getString(PortofinoProperties.GROUP_REGISTERED));
            if(StringUtils.isEmpty(userGroupTableEntityName) || StringUtils.isEmpty(groupTableEntityName)) {
                /////////////////////////////////////////////////////////////////
                //NB admin is hardcoded for the wizard to work - remove it in production!
                /////////////////////////////////////////////////////////////////
                if("admin".equals(principal.asList().get(0))) {
                    logger.warn("Generated security.groovy is using the hardcoded 'admin' user; " +
                                "remember to disable it in production!")
                    groups.add(conf.getString(PortofinoProperties.GROUP_ADMINISTRATORS));
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
                query.setParameter("userId", principal.asList().get(1))
                groups.addAll(query.list())

                if(!StringUtils.isEmpty(adminGroupName) && groups.contains(adminGroupName)) {
                    groups.add(conf.getString(PortofinoProperties.GROUP_ADMINISTRATORS))
                }
            }
        } else if(principal instanceof String) {
            groups.add(conf.getString(PortofinoProperties.GROUP_REGISTERED));

            if(StringUtils.isEmpty(userGroupTableEntityName) || StringUtils.isEmpty(groupTableEntityName)) {
                /////////////////////////////////////////////////////////////////
                //NB admin is hardcoded for the wizard to work - remove it in production!
                /////////////////////////////////////////////////////////////////
                if("admin".equals(principal)) {
                    logger.warn("Generated security.groovy is using the hardcoded 'admin' user; " +
                                "remember to disable it in production!")
                    groups.add(conf.getString(PortofinoProperties.GROUP_ADMINISTRATORS));
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
                    groups.add(conf.getString(PortofinoProperties.GROUP_ADMINISTRATORS))
                }
            }
        } else {
            throw new AuthorizationException("Invalid principal: " + principal);
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(groups);
        Permission permission = new GroupPermission(groups);
        info.setObjectPermissions((Set<Permission>) Collections.singleton(permission));
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
            /////////////////////////////////////////////////////////////////
            //NB admin is hardcoded for the wizard to work - remove it in production!
            /////////////////////////////////////////////////////////////////
            if("admin".equals(userName) && "admin".equals(password)) {
                logger.warn("Generated security.groovy is using the hardcoded 'admin' user; " +
                            "remember to disable it in production!")
                SimpleAuthenticationInfo info =
                        new SimpleAuthenticationInfo(userName, password.toCharArray(), realm.name);
                return info;
            }
            /////////////////////////////////////////////////////////////////
            throw new IncorrectCredentialsException("Login failed");
        }
    }

    String encryptPassword(String password) {
        return this.$encryptionAlgorithm(password)
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

        if(!StringUtils.isEmpty(groupTableEntityName)) {
            Session session = application.getSession(databaseName)
            def criteria = session.createCriteria(groupTableEntityName)
            criteria.setProjection(Projections.property(groupNameProperty))
            for(x in criteria.list()) {
                groups.addAll(String.valueOf(x))
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

    protected String toHex(byte[] raw) {
        return Hex.encodeToString(raw)
    }

    protected String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes)
    }

}
