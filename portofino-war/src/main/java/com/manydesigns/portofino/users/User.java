/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */
package com.manydesigns.portofino.users;

import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.Password;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class User implements Serializable{
    //Dati user_
    Integer uuid;
    String email;
    String pwd;
    Integer state;
    //Integer graceLoginCount;

    Timestamp delDate;
    Timestamp modifieDate;
    Timestamp pwdModdate;
    Timestamp loginDate;
    Timestamp lastLoginDate;
    Timestamp lastFailedLoginDate;
    Timestamp lockoutDate;
    Timestamp createDate;

    Boolean defaultUser;
    Boolean extAuth;
    Boolean pwdEncrypted;
    Boolean pwdReset;
    Boolean lockout;
    Boolean agreedToTerms;
    Boolean active;

    String digest;
    String remQuestion;
    String remans;
    String screenName;
    String greeting;
    String comments;
    String firstName;
    String middleName;
    String lastName;
    String jobTitle;
    String loginIp;
    String lastLoginIp;

    Integer failedLoginAttempts;



    //gruppi di appartenenza
    List<UsersGroups> groups = new ArrayList<UsersGroups>();

    public Integer getUuid() {
        return uuid;
    }

    public void setUuid(Integer uuid) {
        this.uuid = uuid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Password
    @Label(value = "password")
    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public List<UsersGroups> getGroups() {
        return groups;
    }

    public void setGroups(List<UsersGroups> groups) {
        this.groups = groups;
    }

    public Integer getState() {
        //TODO eliminare lo stato fisso
        state=1;
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Timestamp getDelDate() {
        return delDate;
    }

    public void setDelDate(Timestamp delDate) {
        this.delDate = delDate;
    }

    public Timestamp getModifieDate() {
        return modifieDate;
    }

    public void setModifieDate(Timestamp modifieDate) {
        this.modifieDate = modifieDate;
    }

    public Boolean isDefaultUser() {
        return defaultUser;
    }

    public void setDefaultUser(Boolean defaultUser) {
        this.defaultUser = defaultUser;
    }

    public Boolean isExtAuth() {
        return extAuth;
    }

    public void setExtAuth(Boolean extAuth) {
        this.extAuth = extAuth;
    }

    public Boolean isPwdEncrypted() {
        return pwdEncrypted;
    }

    public void setPwdEncrypted(Boolean pwdEncrypted) {
        this.pwdEncrypted = pwdEncrypted;
    }

    public Boolean isPwdReset() {
        return pwdReset;
    }

    public void setPwdReset(Boolean pwdReset) {
        this.pwdReset = pwdReset;
    }

    public Timestamp getPwdModdate() {
        return pwdModdate;
    }

    public void setPwdModdate(Timestamp pwdModdate) {
        this.pwdModdate = pwdModdate;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getRemQuestion() {
        return remQuestion;
    }

    public void setRemQuestion(String remQuestion) {
        this.remQuestion = remQuestion;
    }

    public String getRemans() {
        return remans;
    }

    public void setRemans(String remans) {
        this.remans = remans;
    }

    /*public Integer getGraceLoginCount() {
        return graceLoginCount;
    }

    public void setGraceLoginCount(Integer graceLoginCount) {
        this.graceLoginCount = graceLoginCount;
    }*/

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public Timestamp getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(Timestamp loginDate) {
        this.loginDate = loginDate;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public Timestamp getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Timestamp lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public Timestamp getLastFailedLoginDate() {
        return lastFailedLoginDate;
    }

    public void setLastFailedLoginDate(Timestamp lastFailedLoginDate) {
        this.lastFailedLoginDate = lastFailedLoginDate;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Boolean isLockout() {
        return lockout;
    }

    public void setLockout(Boolean lockout) {
        this.lockout = lockout;
    }

    public Timestamp getLockoutDate() {
        return lockoutDate;
    }

    public void setLockoutDate(Timestamp lockoutDate) {
        this.lockoutDate = lockoutDate;
    }

    public Boolean isAgreedToTerms() {
        return agreedToTerms;
    }

    public void setAgreedToTerms(Boolean agreedToTerms) {
        this.agreedToTerms = agreedToTerms;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (active != null ? !active.equals(user.active) : user.active != null)
            return false;
        if (agreedToTerms != null ? !agreedToTerms.equals(user.agreedToTerms) : user.agreedToTerms != null)
            return false;
        if (comments != null ? !comments.equals(user.comments) : user.comments != null)
            return false;
        if (createDate != null ? !createDate.equals(user.createDate) : user.createDate != null)
            return false;
        if (defaultUser != null ? !defaultUser.equals(user.defaultUser) : user.defaultUser != null)
            return false;
        if (delDate != null ? !delDate.equals(user.delDate) : user.delDate != null)
            return false;
        if (digest != null ? !digest.equals(user.digest) : user.digest != null)
            return false;
        if (!email.equals(user.email)) return false;
        if (extAuth != null ? !extAuth.equals(user.extAuth) : user.extAuth != null)
            return false;
        if (failedLoginAttempts != null ? !failedLoginAttempts.equals(user.failedLoginAttempts) : user.failedLoginAttempts != null)
            return false;
        if (firstName != null ? !firstName.equals(user.firstName) : user.firstName != null)
            return false;
        //if (graceLoginCount != null ? !graceLoginCount.equals(user.graceLoginCount) : user.graceLoginCount != null)
        //    return false;
        if (greeting != null ? !greeting.equals(user.greeting) : user.greeting != null)
            return false;
        if (groups != null ? !groups.equals(user.groups) : user.groups != null)
            return false;
        if (jobTitle != null ? !jobTitle.equals(user.jobTitle) : user.jobTitle != null)
            return false;
        if (lastFailedLoginDate != null ? !lastFailedLoginDate.equals(user.lastFailedLoginDate) : user.lastFailedLoginDate != null)
            return false;
        if (lastLoginDate != null ? !lastLoginDate.equals(user.lastLoginDate) : user.lastLoginDate != null)
            return false;
        if (lastLoginIp != null ? !lastLoginIp.equals(user.lastLoginIp) : user.lastLoginIp != null)
            return false;
        if (lastName != null ? !lastName.equals(user.lastName) : user.lastName != null)
            return false;
        if (lockout != null ? !lockout.equals(user.lockout) : user.lockout != null)
            return false;
        if (lockoutDate != null ? !lockoutDate.equals(user.lockoutDate) : user.lockoutDate != null)
            return false;
        if (loginDate != null ? !loginDate.equals(user.loginDate) : user.loginDate != null)
            return false;
        if (loginIp != null ? !loginIp.equals(user.loginIp) : user.loginIp != null)
            return false;
        if (middleName != null ? !middleName.equals(user.middleName) : user.middleName != null)
            return false;
        if (modifieDate != null ? !modifieDate.equals(user.modifieDate) : user.modifieDate != null)
            return false;
        if (!pwd.equals(user.pwd)) return false;
        if (pwdEncrypted != null ? !pwdEncrypted.equals(user.pwdEncrypted) : user.pwdEncrypted != null)
            return false;
        if (pwdModdate != null ? !pwdModdate.equals(user.pwdModdate) : user.pwdModdate != null)
            return false;
        if (pwdReset != null ? !pwdReset.equals(user.pwdReset) : user.pwdReset != null)
            return false;
        if (remQuestion != null ? !remQuestion.equals(user.remQuestion) : user.remQuestion != null)
            return false;
        if (remans != null ? !remans.equals(user.remans) : user.remans != null)
            return false;
        if (screenName != null ? !screenName.equals(user.screenName) : user.screenName != null)
            return false;
        if (!state.equals(user.state)) return false;
        if (!uuid.equals(user.uuid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (pwd != null ? pwd.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
//        result = 31 * result + (graceLoginCount != null ? graceLoginCount.hashCode() : 0);
        result = 31 * result + (delDate != null ? delDate.hashCode() : 0);
        result = 31 * result + (modifieDate != null ? modifieDate.hashCode() : 0);
        result = 31 * result + (pwdModdate != null ? pwdModdate.hashCode() : 0);
        result = 31 * result + (loginDate != null ? loginDate.hashCode() : 0);
        result = 31 * result + (lastLoginDate != null ? lastLoginDate.hashCode() : 0);
        result = 31 * result + (lastFailedLoginDate != null ? lastFailedLoginDate.hashCode() : 0);
        result = 31 * result + (lockoutDate != null ? lockoutDate.hashCode() : 0);
        result = 31 * result + (createDate != null ? createDate.hashCode() : 0);
        result = 31 * result + (defaultUser != null ? defaultUser.hashCode() : 0);
        result = 31 * result + (extAuth != null ? extAuth.hashCode() : 0);
        result = 31 * result + (pwdEncrypted != null ? pwdEncrypted.hashCode() : 0);
        result = 31 * result + (pwdReset != null ? pwdReset.hashCode() : 0);
        result = 31 * result + (lockout != null ? lockout.hashCode() : 0);
        result = 31 * result + (agreedToTerms != null ? agreedToTerms.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        result = 31 * result + (digest != null ? digest.hashCode() : 0);
        result = 31 * result + (remQuestion != null ? remQuestion.hashCode() : 0);
        result = 31 * result + (remans != null ? remans.hashCode() : 0);
        result = 31 * result + (screenName != null ? screenName.hashCode() : 0);
        result = 31 * result + (greeting != null ? greeting.hashCode() : 0);
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (middleName != null ? middleName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (jobTitle != null ? jobTitle.hashCode() : 0);
        result = 31 * result + (loginIp != null ? loginIp.hashCode() : 0);
        result = 31 * result + (lastLoginIp != null ? lastLoginIp.hashCode() : 0);
        result = 31 * result + (failedLoginAttempts != null ? failedLoginAttempts.hashCode() : 0);
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        return result;
    }
}
