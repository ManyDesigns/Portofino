/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */

package com.manydesigns.portofino.database.platforms;

import com.manydesigns.elements.annotations.Status;
import com.manydesigns.portofino.model.database.ConnectionProvider;
import org.hibernate.dialect.Dialect;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public interface DatabasePlatform {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    final static String STATUS_CREATED = "created";
    final static String STATUS_OK = "ok";
    final static String STATUS_DRIVER_NOT_FOUND = "driver not found";
    final static String STATUS_DRIVER_ERROR = "driver error";

    String getDescription();
    String getStandardDriverClassName();
    Dialect getHibernateDialect();
    String getConnectionStringTemplate();

    @Status(red={STATUS_DRIVER_ERROR}, amber={STATUS_CREATED, STATUS_DRIVER_NOT_FOUND}, green={STATUS_OK})
    String getStatus();

    void test();
    boolean isApplicable(ConnectionProvider connectionProvider);
    void shutdown(ConnectionProvider connectionProvider);

    List<String> getSchemaNames(DatabaseMetaData databaseMetaData) throws SQLException;

}
