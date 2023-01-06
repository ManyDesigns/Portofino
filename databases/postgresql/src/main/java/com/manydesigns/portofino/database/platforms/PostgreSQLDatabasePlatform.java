/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.portofino.database.model.Column;
import com.manydesigns.portofino.database.model.ConnectionProvider;
import com.manydesigns.portofino.database.model.platforms.AbstractDatabasePlatform;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.persistence.hibernate.ColumnParameterType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.type.SqlTypes;
import org.hibernate.usertype.DynamicParameterizedType;

import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
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
            "Copyright (C) 2005-2020 ManyDesigns srl";

    public final static String DESCRIPTION = "PostgreSQL";
    public final static String STANDARD_DRIVER_CLASS_NAME =
            "org.postgresql.Driver";

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public PostgreSQLDatabasePlatform() {
        super(PostgreSQL82Dialect.class.getName(), "jdbc:postgresql://<host>[:<port, default 5432>]/<database>");
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
    public List<Annotation> getAdditionalAnnotations(Column column) {
        if ("JSONB".equalsIgnoreCase(column.getColumnType())) {
            Annotation annotation = new Annotation();
            annotation.setType(JdbcTypeCode.class.getName());
            annotation.setPropertyValue("value", SqlTypes.JSON + "");
            return Collections.singletonList(annotation);
        } else {
            return super.getAdditionalAnnotations(column);
        }
    }

    @Override
    public List<String[]> getSchemaNames(DatabaseMetaData databaseMetaData) throws SQLException {
        List<String[]> schemaNames = super.getSchemaNames(databaseMetaData);
        schemaNames.removeIf(schema -> "information_schema".equalsIgnoreCase(schema[1]) || schema[1].startsWith("pg_"));
        return schemaNames;
    }

    @Override
    protected Class<? extends Number> getDefaultNumericType(Integer precision) {
        if(precision != null && precision == 131089) {
            return BigDecimal.class; //Postgres bug - #925
        }
        return super.getDefaultNumericType(precision);
    }
}
