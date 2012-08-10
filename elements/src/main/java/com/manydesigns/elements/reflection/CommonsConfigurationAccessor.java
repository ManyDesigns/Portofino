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

package com.manydesigns.elements.reflection;

import org.apache.commons.configuration.Configuration;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class CommonsConfigurationAccessor implements ClassAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected final List<CommonsConfigurationEntryAccessor> accessors;

    public CommonsConfigurationAccessor(Configuration configuration) {
        accessors = new ArrayList<CommonsConfigurationEntryAccessor>();
        int i = 0;
        Iterator keys = configuration.getKeys();
        while (keys.hasNext()) {
            String name = (String)keys.next();
            accessors.add(new CommonsConfigurationEntryAccessor(name));
            i++;
        }

        // sort alphabetically
        Collections.sort(accessors,
                new Comparator<CommonsConfigurationEntryAccessor>() {
                    public int compare(CommonsConfigurationEntryAccessor o1,
                                       CommonsConfigurationEntryAccessor o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
    }

    public String getName() {
        return null;
    }

    public PropertyAccessor getProperty(String propertyName) throws NoSuchFieldException {
        for (CommonsConfigurationEntryAccessor current : accessors) {
            if (current.getName().equals(propertyName)) {
                return current;
            }
        }
        throw new NoSuchFieldException(propertyName);
    }

    public PropertyAccessor[] getProperties() {
        PropertyAccessor[] result = new PropertyAccessor[accessors.size()];
        return accessors.toArray(result);
    }


    public PropertyAccessor[] getKeyProperties() {
        return new PropertyAccessor[0];
    }

    public Object newInstance() {
        return null;
    }

    public boolean isAnnotationPresent(
            Class<? extends Annotation> annotationClass) {
        return false;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return null;
    }

    public Annotation[] getAnnotations() {
        return new Annotation[0];
    }

    public Annotation[] getDeclaredAnnotations() {
        return new Annotation[0];
    }
}
