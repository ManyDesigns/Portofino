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
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.bind.annotation.*;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;

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
        String[] components = name.split("[.]");
        List<Domain> domains = this.domains;
        Domain result = null;
        for(String s : components) {
            Optional<Domain> domain = domains.stream().filter(p -> s.equals(p.getName())).findFirst();
            if(domain.isPresent()) {
                result = domain.get();
                domains = result.getSubdomains();
            } else {
                throw new IllegalArgumentException("Domain " + name + " not known");
            }
        }
        return result;
    }

    public Domain ensureDomain(String name) {
        List<Domain> domains = getDomains();
        return ensureDomain(name, domains);
    }

    public static Domain ensureDomain(String name, List<Domain> domains) {
        return ensureDomain(name, domains, null);
    }

    @NotNull
    public static Domain ensureDomain(String name, List<Domain> domains, Consumer<Domain> initializer) {
        return domains.stream().filter(d -> d.getName().equals(name)).findFirst().orElseGet(() -> {
            Domain domain = new Domain();
            domain.setName(name);
            domains.add(domain);
            if(initializer != null) {
                initializer.accept(domain);
            }
            return domain;
        });
    }

    public void addObject(Domain domain, String name, EObject object) {
        if(domain.getObjects().containsKey(name)) {
            throw new RuntimeException("Object already present: " + name + " in domain " + domain);
        }
        domain.getObjects().put(name, object);
    }

    public EObject putObject(Domain domain, String name, Object javaObject)
            throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Class<?> javaClass = javaObject.getClass();
        EClass eClass = findClass(javaClass);
        EObject object = eClass.getEPackage().getEFactoryInstance().create(eClass);
        PropertyDescriptor[] props = Introspector.getBeanInfo(javaClass).getPropertyDescriptors();
        for (PropertyDescriptor prop : props) {
            if (prop.getReadMethod() != null && prop.getWriteMethod() != null) {
                //TODO type conversion
                object.eSet(eClass.getEStructuralFeature(prop.getName()), prop.getReadMethod().invoke(javaObject));
            }
        }
        domain.getObjects().put(name, object);
        return object;
    }

    public EClass findClass(Class<?> aClass) {
        String className = aClass.getSimpleName();
        String[] packageName = aClass.getPackageName().split("[.]");
        Domain pkg = getDomain(packageName[0]);
        int i;
        for (i = 1; pkg != null && i < packageName.length; i++) {
            pkg = pkg.getSubdomain(packageName[i]).orElse(null);
        }
        if (pkg == null) {
            throw new IllegalArgumentException(
                    "Package " + StringUtils.join(packageName, '.', 0, i) + " not found");
        }
        EClassifier eClassifier = pkg.getEClassifier(className);
        if (eClassifier instanceof EClass) {
            return (EClass) eClassifier;
        } else {
            throw new IllegalArgumentException("Not a modeled class: " + aClass.getName());
        }
    }

    public List<Issue> getIssues() {
        return issues;
    }

}
