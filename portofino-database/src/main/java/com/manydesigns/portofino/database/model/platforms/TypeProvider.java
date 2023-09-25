package com.manydesigns.portofino.database.model.platforms;

import com.manydesigns.portofino.database.model.Type;
import org.jetbrains.annotations.Nullable;

public interface TypeProvider {
    /**
     * Returns the default Java type to use to map a certain database type.
     *
     * @param jdbcType  the JDBC type number, as returned by the JDBC driver.
     * @param typeName  the name of the type in the database.
     * @param precision precision, if applicable, e.g. 1 in DECIMAL(1,2).
     * @param scale     scale, if applicable, e.g. 2 in DECIMAL(1,2).
     * @return the default Java type or <code>null</code> if this platform doesn't know or support the type.
     */
    @Nullable
    Class<?> getDefaultJavaType(int jdbcType, String typeName, Integer precision, Integer scale);

    Class[] getAvailableJavaTypes(Type type, Integer length);
}
