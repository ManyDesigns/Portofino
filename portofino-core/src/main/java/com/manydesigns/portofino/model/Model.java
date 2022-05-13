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

package com.manydesigns.portofino.model;

import com.manydesigns.portofino.model.issues.Issue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.bind.annotation.*;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Model {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final List<Domain> domains = new ArrayList<>();
    protected final List<Issue> issues = new ArrayList<>();

    public static final Logger logger = LoggerFactory.getLogger(Model.class);

    public void init() {
        issues.clear();
    }

    public List<Domain> getDomains() {
        return domains;
    }

    public Domain getDomain(String name) {
        List<Domain> domains = this.domains;
        return Domain.resolveDomain(name, domains);
    }

    public Domain ensureDomain(String name) {
        List<Domain> domains = getDomains();
        return Domain.ensureDomain(name, domains);
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public EClassifier resolveType(String typeName) {
        int nameSep = typeName.lastIndexOf('.');
        if (nameSep > 0) {
            Domain domain = getDomain(typeName.substring(0, nameSep));
            if (domain != null) {
                return domain.getEClassifier(typeName.substring(nameSep + 1));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
