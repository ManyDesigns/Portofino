package com.manydesigns.portofino.database.platforms;

import com.manydesigns.portofino.database.model.Type;
import com.manydesigns.portofino.database.model.platforms.TypeProvider;
import org.jetbrains.annotations.Nullable;

import java.sql.Types;
import java.util.List;
import java.util.Map;

public class PostgreSQLTypeProvider implements TypeProvider {
    @Nullable
    @Override
    public Class<?> getDefaultJavaType(int jdbcType, String typeName, Integer precision, Integer scale) {
        if (jdbcType == Types.OTHER && "JSONB".equalsIgnoreCase(typeName)) {
            return String.class;
        } else {
            return null;
        }
    }

    @Override
    public Class[] getAvailableJavaTypes(Type type, Integer length) {
        if("JSONB".equalsIgnoreCase(type.getTypeName())) {
            return new Class[] { String.class, Map.class, List.class };
        } else {
            return null;
        }
    }
}
