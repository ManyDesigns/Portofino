/*
 * Copyright (C) 2005-2023 ManyDesigns srl.  All rights reserved.
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
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The Model is the principal holder of information about the application's data, and it's one of the core features of
 * Portofino, that sets it apart from other "frameworks" and low-code tools. This <i>data</i> includes both persistent
 * data, stored for example in a database, and configuration data for various parts of the application.
 * <p>
 * In model-driven parlance, this is really a <i>metamodel</i>, if we consider the data proper (as in, actual
 * database rows) to be the <i>model</i> of a physical or social system – such as a department in the company you're
 * working for.<br />
 * That is, this Model describes the structure of the data and its relationships, and doesn't contain the data directly.
 * </p><p>
 * This Model itself – or rather its contents, in the form of {@link Model#domains} – in turn are defined in terms of
 * a metamodel, that is, <a href="https://wiki.eclipse.org/Ecore">Ecore</a>.
 * </p><p>
 * The concern of persisting the model – i.e. saving and loading it – is left to other classes.
 *
 * </p>
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Model {
    public static final String copyright =
            "Copyright (C) 2005-2023 ManyDesigns srl";

    protected final List<Domain> domains = new CopyOnWriteArrayList<>();
    protected final List<Issue> issues = new CopyOnWriteArrayList<>();

    public static final Logger logger = LoggerFactory.getLogger(Model.class);

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
