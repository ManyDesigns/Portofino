package com.manydesigns.portofino.persistence.hibernate;

import com.manydesigns.portofino.code.CodeBase;
import org.hibernate.SessionFactory;

public class SessionFactoryAndCodeBase {

    public final SessionFactory sessionFactory;
    public final CodeBase codeBase;

    public SessionFactoryAndCodeBase(SessionFactory sessionFactory, CodeBase codeBase) {
        this.sessionFactory = sessionFactory;
        this.codeBase = codeBase;
    }
}
