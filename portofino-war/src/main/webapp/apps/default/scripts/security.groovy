import com.manydesigns.elements.util.RandomUtil
import com.manydesigns.portofino.application.Application
import com.manydesigns.portofino.logic.SecurityLogic
import com.manydesigns.portofino.model.database.DatabaseLogic
import com.manydesigns.portofino.shiro.ApplicationRealm
import com.manydesigns.portofino.shiro.ApplicationRealmDelegate
import com.manydesigns.portofino.shiro.GroupPermission
import com.manydesigns.portofino.system.model.users.User
import java.sql.Timestamp
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.hibernate.Session
import org.hibernate.Transaction
import org.hibernate.criterion.Restrictions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Security implements ApplicationRealmDelegate {

    private static final Logger logger = LoggerFactory.getLogger(Security.class);

    AuthorizationInfo getAuthorizationInfo(ApplicationRealm realm, String userName) {
        Application application = realm.getApplication();
        Set<String> roleNames = new HashSet<String>(SecurityLogic.getUserGroups(application, userName));
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roleNames);
        Permission permission = new GroupPermission(roleNames);
        info.setObjectPermissions(Collections.singleton(permission));
        return info;
    }

    AuthenticationInfo getAuthenticationInfo(ApplicationRealm realm, String userName, String password) {
        Application application = realm.application;
        Session session = application.getSystemSession();
        org.hibernate.Criteria criteria = session.createCriteria("users");
        criteria.add(Restrictions.eq("userName", userName));
        criteria.add(Restrictions.eq(DatabaseLogic.PASSWORD, password));

        List<Object> result = (List<Object>) criteria.list();

        User user;
        if (result.size() == 1) {
            user = (User) result.get(0);
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(user.userId, password.toCharArray(), realm.name);
            updateUser(application, user);
            return info;
        } else {
            updateFailedUser(application, userName);
            throw new AuthenticationException("Login failed");
        }
    }

    //From LoginAction
    private void updateFailedUser(Application application, String username) {
        User user;
        user = application.findUserByUserName(username);
        if (user == null) {
            return;
        }
        user.setLastFailedLoginDate(new Timestamp(new Date().getTime()));
        int failedAttempts = (null==user.getFailedLoginAttempts())?0:1;
        user.setFailedLoginAttempts(failedAttempts+1);
        Session session = application.getSystemSession();
        session.update(user);
        session.getTransaction().commit();
    }

    private void updateUser(Application application, User user) {
        user.setFailedLoginAttempts(0);
        user.setLastLoginDate(new Timestamp(new Date().getTime()));
        user.setToken(null);
        Session session = application.getSystemSession();
        Transaction tx = session.getTransaction();
        try {
            User existingUser = application.findUserByUserName(user.getUserName());
            if(existingUser != null) {
                logger.debug("Updating existing user {} (userId: {})",
                        existingUser.getUserName(), existingUser.getUserId());
                user.setUserId(existingUser.getUserId());
                session.merge(DatabaseLogic.USER_ENTITY_NAME, user);
            } else {
                user.setUserId(RandomUtil.createRandomId(20));
                logger.debug("Importing user {} (userId: {})",
                        user.getUserName(), user.getUserId());
                session.save(DatabaseLogic.USER_ENTITY_NAME, user);
            }
            session.flush();
            tx.commit();
        } catch (RuntimeException e) {
            //Session will be closed by the filter
            throw e;
        }
    }

}