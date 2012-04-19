/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.elements.util;

import com.manydesigns.elements.fields.helpers.FieldsManager;
import org.slf4j.Logger;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class InstanceBuilder<T> {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

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
            logger.warn("Cannot load class: {}", managerClassName);
            managerClass = defaultImplClass;
        }

        if (!clazz.isAssignableFrom(managerClass)) {
            logger.warn("Cannot use as {}: {}",
                    clazz.getName(), managerClassName);
            managerClass = defaultImplClass;
        }
        logger.debug("Using class: {}", managerClass.getName());

        T instance = (T)ReflectionUtil.newInstance(managerClass);
        if (instance == null) {
            logger.warn("Cannot instanciate: {}. Fall back to default: {}.",
                    managerClass.getName(),
                    FieldsManager.class.getName());
            instance = (T)ReflectionUtil.newInstance(defaultImplClass);
            if (instance == null) {
                logger.error("Cannot instanciate: {}",
                        defaultImplClass.getName());
            }
        }

        logger.debug("Installed {0}: {1}", clazz.getName(), instance);
        return instance;
    }

}
