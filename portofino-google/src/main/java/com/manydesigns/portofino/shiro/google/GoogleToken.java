package com.manydesigns.portofino.shiro.google;

import org.apache.shiro.authc.HostAuthenticationToken;
import java.util.Map;

public class GoogleToken implements HostAuthenticationToken {

    private String sub;
    private String name;
    private String given_name;
    private String family_name;
    private String picture;
    private String email;
    private Boolean email_verified;
    private String locale;
    private String hd;

    private String host;

    public static GoogleToken fromMap(Map<String, Object> m) {
        GoogleToken gt = new GoogleToken();
        gt.sub = (String) m.get("sub");
        gt.name = (String) m.get("name");
        gt.given_name = (String) m.get("given_name");
        gt.family_name = (String) m.get("family_name");
        gt.picture = (String) m.get("picture");
        gt.email = (String) m.get("email");
        gt.email_verified = (Boolean) m.get("email_verified");
        gt.locale = (String) m.get("locale");
        gt.hd = (String) m.get("hd");

        return gt;
    }

    public GoogleToken() {
    }

    public GoogleToken(final String name, final String email) {
        this.name = name;
        this.email = email;
    }

    public GoogleToken(final String sub, final String name, final String given_name,
                       final String family_name, final String picture,
                       final String email, final Boolean email_verified,
                       final String locale, final String hd, final String host) {
        this.sub = sub;
        this.name = name;
        this.given_name = given_name;
        this.family_name = family_name;
        this.picture = picture;
        this.email = email;
        this.email_verified = email_verified;
        this.locale = locale;
        this.hd = hd;
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) { this.host = host; }

    public Object getPrincipal() {
        return email;
    }

    public Object getCredentials() {
        return null;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGiven_name() {
        return given_name;
    }

    public void setGiven_name(String given_name) {
        this.given_name = given_name;
    }

    public String getFamily_name() {
        return family_name;
    }

    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEmail_verified() {
        return email_verified;
    }

    public void setEmail_verified(Boolean email_verified) {
        this.email_verified = email_verified;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getHd() {
        return hd;
    }

    public void setHd(String hd) {
        this.hd = hd;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append(" - ").append(name);
        sb.append(", email=").append(email);
        sb.append(", locale=").append(locale);
        sb.append(", hd=").append(hd);
        if (host != null) {
            sb.append(" (").append(host).append(")");
        }
        return sb.toString();
    }
}
