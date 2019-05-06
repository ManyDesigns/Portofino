package com.manydesigns.portofino.upstairs.actions.mail.support;

import com.manydesigns.elements.annotations.*;

public class MailSettings {

    @Required
    @FieldSet("General")
    public boolean mailEnabled;

    @FieldSet("General")
    @Label("Keep sent messages")
    public boolean keepSent;

    @Required
    @FieldSet("General")
    @Label("Queue location")
    @FieldSize(100)
    public String queueLocation;

    @FieldSet("SMTP")
    @Label("Host")
    public String smtpHost;

    @FieldSet("SMTP")
    @Label("Port")
    public Integer smtpPort;

    @Required
    @FieldSet("SMTP")
    @Label("SSL enabled")
    public boolean smtpSSL;

    @FieldSet("SMTP")
    @Label("TLS enabled")
    public boolean smtpTLS;

    @FieldSet("SMTP")
    @Label("Login")
    public String smtpLogin;

    @FieldSet("SMTP")
    @Password
    @Label("Password")
    public String smtpPassword;

}
