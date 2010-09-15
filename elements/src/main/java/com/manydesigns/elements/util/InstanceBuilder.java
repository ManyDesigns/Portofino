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

package com.manydesigns.elements.util;

import com.manydesigns.elements.fields.helpers.FieldsManager;
import com.manydesigns.elements.logging.LogUtil;

import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class InstanceBuilder<T> {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    private Class<T> clazz;
    private Class defaultImplClass;
    private Logger logger;

    public InstanceBuilder(Class<T> iface,
                           Class<? extends T> defaultImplClass,
                           Logger logger) {
        this.clazz = iface;
        this.defaultImplClass = defaultImplClass;
        this.logger = logger;
    }


    @SuppressWarnings({"unchecked"})
    public T createInstance(String managerClassName) {
        Class managerClass = ReflectionUtil.loadClass(managerClassName);
        if (managerClass == null) {
            LogUtil.warningMF(logger,
                    "Cannot load class: {0}", managerClassName);
            managerClass = defaultImplClass;
        }

        if (!clazz.isAssignableFrom(managerClass)) {
            LogUtil.warningMF(logger,
                    "Cannot use as {0}: {1}",
                    clazz.getName(),
                    managerClassName);
            managerClass = defaultImplClass;
        }
        LogUtil.finerMF(logger,
                "Using class: {0}", managerClass.getName());

        T instance = (T)ReflectionUtil.newInstance(managerClass);
        if (instance == null) {
            LogUtil.warningMF(logger,
                    "Cannot instanciate: {0}. Fall back to default: {1}.",
                    managerClass.getName(),
                    FieldsManager.class.getName());
            instance = (T)ReflectionUtil.newInstance(defaultImplClass);
            if (instance == null) {
                LogUtil.severeMF(logger,
                        "Cannot instanciate: {0}",
                        defaultImplClass.getName());
            }
        }

        LogUtil.finerMF(logger, "Installed {0}: {1}",
                clazz.getName(), instance);
        return instance;
    }

}
