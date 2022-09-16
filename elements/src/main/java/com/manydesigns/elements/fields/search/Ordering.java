package com.manydesigns.elements.fields.search;

import com.manydesigns.elements.reflection.PropertyAccessor;

import java.io.Serializable;

public class Ordering implements Serializable {

    protected final PropertyAccessor propertyAccessor;
    protected final String direction;

    public static final String ASC = "asc";
    public static final String DESC = "desc";

    public Ordering(PropertyAccessor propertyAccessor, String direction) {
        this.propertyAccessor = propertyAccessor;
        this.direction = direction;
    }

    public PropertyAccessor getPropertyAccessor() {
        return propertyAccessor;
    }

    public String getDirection() {
        return direction;
    }

    public boolean isAsc() {
        return ASC.equals(direction);
    }

    public boolean isDesc() {
        return DESC.equals(direction);
    }
}
