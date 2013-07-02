package com.manydesigns.portofino.actions.user;

import com.manydesigns.elements.annotations.Password;
import com.manydesigns.elements.annotations.Required;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: predo
 * Date: 7/2/13
 * Time: 4:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class User {
    @Required
    public String email;

    @Required
    @Password
    public String password;

    @Required
    @Password
    public String confermaPassword;

    @Required
    public String nome;

    @Required
    public String cognome;

    @Required
    public String luogoDiNascita;

    @Required
    public Date dataDiNascita;

    public String codiceFiscale;
}
