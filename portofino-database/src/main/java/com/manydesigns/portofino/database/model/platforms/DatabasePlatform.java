/*
 * Copyright (C) 2005-2022 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.database.model.platforms;

import com.manydesigns.elements.annotations.Status;
import com.manydesigns.portofino.database.model.Column;
import com.manydesigns.portofino.database.model.ConnectionProvider;
import com.manydesigns.portofino.database.model.Type;
import org.jetbrains.annotations.Nullable;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * Represents a database platform.
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public interface DatabasePlatform extends TypeProvider {
    String copyright = "Copyright (C) 2005-2020 ManyDesigns srl";

    String STATUS_CREATED = "created";
    String STATUS_OK = "ok";
    String STATUS_DRIVER_NOT_FOUND = "driver not found";
    String STATUS_DRIVER_ERROR = "driver error";

    String getDescription();
    String getStandardDriverClassName();
    String getHibernateDialect();
    /**
     * Is Hibernate able to automatically the dialect from a JDBC connection for this database platform?
     * @return false if and only if the hibernate.dialect property must be explicitly set in order to connect to
     * an instance of this database platform.
     */
    boolean isDialectAutodetected();
    String getConnectionStringTemplate();

    @Status(red={STATUS_DRIVER_ERROR}, amber={STATUS_CREATED, STATUS_DRIVER_NOT_FOUND}, green={STATUS_OK})
    String getStatus();

    TypeDescriptor getDatabaseSpecificType(Column column);

    void test();
    boolean isApplicable(ConnectionProvider connectionProvider);
    void shutdown(ConnectionProvider connectionProvider);

    List<String[]> getSchemaNames(DatabaseMetaData databaseMetaData) throws SQLException;

    //**************************************************************************
    // Types
    //**************************************************************************

    List<TypeProvider> getAdditionalTypeProviders();

    class TypeDescriptor {

        public final String name;
        public final Properties parameters;

        public TypeDescriptor(String name, Properties parameters) {
            this.name = name;
            this.parameters = parameters;
        }
    }

}
