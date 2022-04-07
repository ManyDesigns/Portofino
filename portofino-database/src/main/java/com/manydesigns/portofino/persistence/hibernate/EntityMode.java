package com.manydesigns.portofino.persistence.hibernate;

public enum EntityMode {

    POJO, MAP;

    static EntityMode parse(String value) {
        return EntityMode.valueOf(value.toUpperCase());
    }

}
