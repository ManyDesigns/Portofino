import com.manydesigns.elements.util.RandomUtil
import com.manydesigns.portofino.PortofinoProperties
import com.manydesigns.portofino.application.Application
import com.manydesigns.portofino.application.QueryUtils
import com.manydesigns.portofino.shiro.ApplicationRealm
import com.manydesigns.portofino.shiro.ApplicationRealmDelegate
import com.manydesigns.portofino.shiro.GroupPermission
import com.manydesigns.portofino.system.model.users.User
import com.manydesigns.portofino.system.model.users.UserConstants
import com.manydesigns.portofino.system.model.users.UsersGroups
import java.sql.Timestamp
import org.apache.commons.configuration.Configuration
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.DisabledAccountException
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.Transaction
import org.hibernate.criterion.Restrictions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Security implements ApplicationRealmDelegate {

    private static final Logger logger = LoggerFactory.getLogger(Security.class);

    AuthorizationInfo getAuthorizationInfo(ApplicationRealm realm, Object userName) {
        Application application = realm.getApplication();
        Set<String> groups = new HashSet<String>();
        Configuration conf = application.getPortofinoProperties();
        groups.add(conf.getString(PortofinoProperties.GROUP_ALL));
        if (userName == null) {
            groups.add(conf.getString(PortofinoProperties.GROUP_ANONYMOUS));
        } else {
            Session session = application.getSession("portofino");
            org.hibernate.Criteria criteria = session.createCriteria("users");
            criteria.add(Restrictions.eq("userName", userName));
            User u = criteria.uniqueResult();

            groups.add(conf.getString(PortofinoProperties.GROUP_REGISTERED));

            for (UsersGroups ug : u.getGroups()) {
                if (ug.getDeletionDate() == null) {
                    groups.add(ug.getGroup().getGroupId());
                }
            }
        }
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(groups);
        Permission permission = new GroupPermission(groups);
        info.setObjectPermissions(Collections.singleton(permission));
        return info;
    }

    AuthenticationInfo getAuthenticationInfo(ApplicationRealm realm, String userName, String password) {
        Application application = realm.application;
        Session session = application.getSession("portofino");
        org.hibernate.Criteria criteria = session.createCriteria("users");
        criteria.add(Restrictions.eq("userName", userName));
        criteria.add(Restrictions.eq(UserConstants.PASSWORD, password));

        List<Object> result = (List<Object>) criteria.list();

        User user;
        if (result.size() == 1) {
            user = (User) result.get(0);
            if(!user.getState().equals(UserConstants.ACTIVE)) {
                throw new DisabledAccountException("User " + user.userId + " is not active");
            }
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(user.userName, password.toCharArray(), realm.name);
            updateUser(application, user);
            return info;
        } else {
            updateFailedUser(application, userName);
            throw new AuthenticationException("Login failed");
        }
    }

    Set<String> getUsers(ApplicationRealm realm) {
        Application application = realm.application;
        Session session = application.getSession("portofino");
        SQLQuery query = session.createSQLQuery("select userName from \"USERS\"");
        return new LinkedHashSet<String>(query.list());
    }

    Set<String> getGroups(ApplicationRealm realm) {
        Application application = realm.application;
        Session session = application.getSession("portofino");
        SQLQuery query = session.createSQLQuery("select name from \"GROUPS\"");
        return new LinkedHashSet<String>(query.list()); //TODO verificare
    }

    //From LoginAction
    private void updateFailedUser(Application application, String username) {
        User user;
        user = findUserByUserName(username);
        if (user == null) {
            return;
        }
        user.setLastFailedLoginDate(new Timestamp(new Date().getTime()));
        int failedAttempts = (null==user.getFailedLoginAttempts())?0:1;
        user.setFailedLoginAttempts(failedAttempts+1);
        Session session = application.getSession("portofino");
        session.update(user);
        session.getTransaction().commit();
    }

    private void updateUser(Application application, User user) {
        user.setFailedLoginAttempts(0);
        user.setLastLoginDate(new Timestamp(new Date().getTime()));
        user.setToken(null);
        Session session = application.getSession("portofino");
        Transaction tx = session.getTransaction();
        try {
            User existingUser = findUserByUserName(session, user.getUserName());
            if(existingUser != null) {
                logger.debug("Updating existing user {} (userId: {})",
                        existingUser.getUserName(), existingUser.getUserId());
                user.setUserId(existingUser.getUserId());
                session.merge(UserConstants.USER_ENTITY_NAME, user);
            } else {
                user.setUserId(RandomUtil.createRandomId(20));
                logger.debug("Importing user {} (userId: {})",
                        user.getUserName(), user.getUserId());
                session.save(UserConstants.USER_ENTITY_NAME, user);
            }
            session.flush();
            tx.commit();
        } catch (RuntimeException e) {
            //Session will be closed by the filter
            throw e;
        }
    }

    private User findUserByUserName(Session session, String username) {
        org.hibernate.Criteria criteria = session.createCriteria(UserConstants.USER_ENTITY_NAME);
        criteria.add(Restrictions.eq("userName", username));
        return (User) criteria.uniqueResult();
    }

}