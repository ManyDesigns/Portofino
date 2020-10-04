package com.manydesigns.portofino.model.java;

import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.Type;

public class JavaTypesDomain extends Domain {

    public static final Type STRING_TYPE = new Type("java.lang.String");

    public JavaTypesDomain() {
        types.add(STRING_TYPE);
        types.add(new Type("java.lang.Integer"));
        types.add(new Type("java.lang.Long"));
    }

    @Override
    public Type getDefaultType() {
        return STRING_TYPE;
    }
}
