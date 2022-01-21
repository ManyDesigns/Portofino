package com.manydesigns.portofino.persistence;

import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.text.TextFormat;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public abstract class IdStrategy {
    protected final ClassAccessor classAccessor;

    public IdStrategy(ClassAccessor classAccessor) {
        this.classAccessor = classAccessor;
    }

    public abstract Object getPrimaryKey(String... identifier);

    public String[] generatePkStringArray(Object object) {
        PropertyAccessor[] keyProperties = classAccessor.getKeyProperties();
        String[] array = new String[keyProperties.length];
        for(int i = 0; i < keyProperties.length; i++) {
            PropertyAccessor property = keyProperties[i];
            Object value = property.get(object);
            String stringValue = OgnlUtils.convertValue(value, String.class);
            array[i] = stringValue;
        }
        return array;
    }

    public TextFormat createPkGenerator() {
        String formatString = getFormatString();
        return OgnlTextFormat.create(formatString);
    }

    @NotNull
    public String getFormatString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (PropertyAccessor property : classAccessor.getKeyProperties()) {
            if (first) {
                first = false;
            } else {
                sb.append("/");
            }
            sb.append("%{");
            sb.append(property.getName());
            sb.append("}");
        }
        return sb.toString();
    }

    public String getPkStringForUrl(Object o, String encoding) throws UnsupportedEncodingException {
        return getPkStringForUrl(generatePkStringArray(o), encoding);
    }

    public String getPkStringForUrl(String[] pk, String encoding) throws UnsupportedEncodingException {
        String[] escapedPk = new String[pk.length];
        for(int i = 0; i < pk.length; i++) {
            escapedPk[i] = URLEncoder.encode(pk[i], encoding);
        }
        return getPkString(escapedPk);
    }

    @Nullable
    public String getPkString(String[] pkStringArray) {
        return StringUtils.join(pkStringArray, "/");
    }

    public String getPkString(Object object) {
        return getPkString(generatePkStringArray(object));
    }
}
