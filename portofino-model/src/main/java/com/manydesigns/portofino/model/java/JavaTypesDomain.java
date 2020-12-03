package com.manydesigns.portofino.model.java;

import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.Type;

public class JavaTypesDomain extends Domain {

    public static final Type STRING_TYPE = new Type("java.lang.String", "string");

    public JavaTypesDomain() {
        types.add(STRING_TYPE);
        types.add(new Type("java.lang.Boolean", "boolean"));
        types.add(new Type("java.lang.Integer", "integer"));
        types.add(new Type("java.lang.Long", "long"));
        types.add(new Type("java.lang.Float", "float"));
        types.add(new Type("java.lang.Double", "double"));
        types.add(new Type("java.util.Date", "datetime"));
        types.add(new Type("java.sql.Date"));
        types.add(new Type("java.sql.Timestamp"));
    }

    @Override
    public Type getDefaultType() {
        return STRING_TYPE;
    }
}
