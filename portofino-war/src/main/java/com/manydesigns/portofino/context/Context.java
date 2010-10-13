/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.context;

import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.portofino.database.ConnectionProvider;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.reflection.UseCaseAccessor;
import com.manydesigns.portofino.system.model.users.User;

import java.io.Serializable;
import java.util.List;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public interface Context {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Model loading
    //**************************************************************************

    void loadConnectionsAsResource(String resource);
    void loadXmlModelAsResource(String resource);


    //**************************************************************************
    // Database stuff
    //**************************************************************************

    List<ConnectionProvider> getConnectionProviders();
    ConnectionProvider getConnectionProvider(String databaseName);

    //**************************************************************************
    // Model access
    //**************************************************************************

    Model getModel();
    void syncDataModel();

    //**************************************************************************
    // Persistance
    //**************************************************************************

    Object getObjectByPk(String qualifiedTableName, Serializable pk);

    List<Object> getAllObjects(String qualifiedTableName);

    List<Object> getObjects(Criteria criteria);

    List<Object> getObjects(String queryString);

    List<Object> getObjects(String queryString, Criteria criteria);

    void saveOrUpdateObject(String qualifiedTableName, Object obj);

    void saveObject(String qualifiedTableName, Object obj);

    void updateObject(String qualifiedTableName, Object obj);

    void deleteObject(String qualifiedTableName, Object obj);

    List<Object[]> runSql(String databaseName, String sql);

    void openSession();

    void closeSession();

    void commit(String databaseName);

    void rollback(String databaseName);

    void commit();

    void rollback();

    List<Object> getRelatedObjects(String qualifiedTableName, 
            Object obj, String oneToManyRelationshipName);

    void resetDbTimer();

    long getDbTime();

    List<String> getDDLCreate();

    List<String> getDDLUpdate();

    //**************************************************************************
    // ClassAccessors management
    //**************************************************************************

    public TableAccessor getTableAccessor(String qualifiedTableName);
    public UseCaseAccessor getUseCaseAccessor(String useCaseName);

    //**************************************************************************
    // User
    //**************************************************************************

    User login(String email, String password);

    public User findUserByEmail(String email);

    public User findUserByToken(String token);

    void logout();

    User getCurrentUser();

    void setCurrentUser(User user);

}
