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
package com.manydesigns.portofino.system.model.users;

import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.Password;
import com.manydesigns.portofino.system.model.messages.Message;
import org.apache.commons.lang.RandomStringUtils;
import sun.misc.BASE64Encoder;

import java.io.Serializable;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class User implements Serializable{
    public static final String copyright
            = "Copyright (c) 2005-2010, ManyDesigns srl";
    //Dati
    Long userId;
    Long state;


    Timestamp deletionDate;
    Timestamp modifiedDate;
    Timestamp pwdModDate;
    Timestamp lastLoginDate;
    Timestamp lastFailedLoginDate;
    Timestamp creationDate;

    Boolean defaultUser;
    Boolean agreedToTerms;
    Boolean extAuth;

    String email;
    String pwd;
    String token;
    String remQuestion;
    String remans;
    String userName;
    String firstName;
    String middleName;
    String lastName;
    String jobTitle;

    Integer failedLoginAttempts;
    Integer bounced;
    Integer graceLoginCount;

    List<Group> groupsCreated;
    List<Message> messageIn;
    List<Message> messageOut;
    List<OldPassword> oldPwds;


    //gruppi di appartenenza
    List<UsersGroups> groups = new ArrayList<UsersGroups>();

    public User(){

    }
    
    public User(Long userId){
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Long getState() {
        return state;
    }

    public void setState(Long state) {
        this.state = state;
    }

    public Timestamp getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(Timestamp deletionDate) {
        this.deletionDate = deletionDate;
    }

    public Timestamp getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Timestamp modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Boolean getDefaultUser() {
        return defaultUser;
    }

    public void setDefaultUser(Boolean defaultUser) {
            this.defaultUser = defaultUser;
    }

    public Boolean getExtAuth() {
        return extAuth;
    }

    public void setExtAuth(Boolean extAuth) {
        this.extAuth = extAuth;
    }

    public Timestamp getPwdModDate() {
        return pwdModDate;
    }

    public void setPwdModDate(Timestamp pwdModDate) {
        this.pwdModDate = pwdModDate;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public Timestamp getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Timestamp lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
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

    public Boolean getAgreedToTerms() {
        return agreedToTerms;
    }

    public void setAgreedToTerms(Boolean agreedToTerms) {
        this.agreedToTerms = agreedToTerms;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public Integer getGraceLoginCount() {
        return graceLoginCount;
    }

    public void setGraceLoginCount(Integer graceLoginCount) {
        this.graceLoginCount = graceLoginCount;
    }

    public Integer getBounced() {
        return bounced;
    }

    public void setBounced(Integer bounced) {
        this.bounced = bounced;
    }

    public List<Group> getGroupsCreated() {
        return groupsCreated;
    }

    public void setGroupsCreated(List<Group> groupsCreated) {
        this.groupsCreated = groupsCreated;
    }

    public List<Message> getMessageIn() {
        return messageIn;
    }

    public void setMessageIn(List<Message> messageIn) {
        this.messageIn = messageIn;
    }

    public List<Message> getMessageOut() {
        return messageOut;
    }

    public void setMessageOut(List<Message> messageOut) {
        this.messageOut = messageOut;
    }

    public List<OldPassword> getOldPwds() {
        return oldPwds;
    }

    public void setOldPwds(List<OldPassword> oldPwds) {
        this.oldPwds = oldPwds;
    }

    public void encryptPwd() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(pwd.getBytes("UTF-8"));
            byte raw[] = md.digest();
            setPwd((new BASE64Encoder()).encode(raw));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public synchronized  void tokenGenerator() {
        setToken(RandomStringUtils.random(30, true, true));
    }

    public synchronized void passwordGenerator(int len) {
        setPwd(RandomStringUtils.random(len, true, true));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return !(userId != null ? !userId.equals(user.userId) : user.userId != null);

    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    
}
