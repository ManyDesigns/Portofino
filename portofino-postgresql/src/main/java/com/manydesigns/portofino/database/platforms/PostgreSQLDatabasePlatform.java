/*
 * Copyright (C) 2005-2024 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.database.platforms;

import com.manydesigns.portofino.model.database.Column;
import com.manydesigns.portofino.model.database.ConnectionProvider;
import com.manydesigns.portofino.model.database.platforms.AbstractDatabasePlatform;
import com.manydesigns.portofino.persistence.hibernate.ColumnParameterType;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.usertype.DynamicParameterizedType;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PostgreSQLDatabasePlatform extends AbstractDatabasePlatform {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    public final static String DESCRIPTION = "PostgreSQL";
    public final static String STANDARD_DRIVER_CLASS_NAME =
            "org.postgresql.Driver";

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public PostgreSQLDatabasePlatform() {
        super(new PostgreSQL82Dialect(), "jdbc:postgresql://<host>[:<port, default 5432>]/<database>");
    }

    //**************************************************************************
    // Implementation of DatabaseAbstraction
    //**************************************************************************

    public String getDescription() {
        return DESCRIPTION;
    }

    public String getStandardDriverClassName() {
        return STANDARD_DRIVER_CLASS_NAME;
    }

    public boolean isApplicable(ConnectionProvider connectionProvider) {
        return "PostgreSQL".equals(connectionProvider.getDatabaseProductName());
    }

    @Override
    public TypeDescriptor getDatabaseSpecificType(Column column) {
        if ("JSONB".equalsIgnoreCase(column.getColumnType())) {
            Properties typeParams = new Properties();
            if(column.getActualJavaType() == Map.class) {
                typeParams.put(DynamicParameterizedType.PARAMETER_TYPE, new ColumnParameterType(column, Map.class));
                return new TypeDescriptor("com.marvinformatics.hibernate.json.JsonUserType", typeParams);
            } else if(column.getActualJavaType() == List.class) {
                typeParams.put(DynamicParameterizedType.PARAMETER_TYPE, new ColumnParameterType(column, Object.class));
                return new TypeDescriptor("com.marvinformatics.hibernate.json.JsonListUserType", typeParams);
            } else {
                logger.warn("Unsupported column data type: " + column.getActualJavaType());
            }
        }
        return super.getDatabaseSpecificType(column);
    }

    @Override
    public List<String[]> getSchemaNames(DatabaseMetaData databaseMetaData) throws SQLException {
        List<String[]> schemaNames = super.getSchemaNames(databaseMetaData);
        Iterator<String[]> it = schemaNames.iterator();
        while (it.hasNext()) {
            String[] next = it.next();
            if("information_schema".equalsIgnoreCase(next[1]) || next[1].startsWith("pg_")) {
                it.remove();
            }
        }
        return schemaNames;
    }
}
