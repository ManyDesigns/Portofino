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
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.bind.annotation.*;
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

    protected final List<EPackage> domains = new ArrayList<>();
    protected final Map<EPackage, Map<String, EObject>> objects = new HashMap<>();
    protected final List<Issue> issues = new ArrayList<>();

    public static final Logger logger = LoggerFactory.getLogger(Model.class);

    public void init() {
        issues.clear();
    }

    public List<EPackage> getDomains() {
        return domains;
    }

    public EPackage getDomain(String name) {
        String[] components = name.split("[.]");
        List<EPackage> domains = this.domains;
        EPackage result = null;
        for(String s : components) {
            Optional<EPackage> ePackage = domains.stream().filter(p -> s.equals(p.getName())).findFirst();
            if(ePackage.isPresent()) {
                result = ePackage.get();
                domains = result.getESubpackages();
            } else {
                throw new RuntimeException("Domain " + name + " not known");
            }
        }
        return result;
    }

    public EPackage ensureDomain(String name) {
        return getDomains().stream().filter(d -> d.getName().equals(name)).findFirst().orElseGet(() -> {
            EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
            ePackage.setName(name);
            getDomains().add(ePackage);
            return ePackage;
        });
    }

    public void addObject(EPackage domain, String name, EObject object) {
        Map<String, EObject> objects = this.objects.getOrDefault(domain, new HashMap<>());
        if(objects.putIfAbsent(name, object) != null) {
            throw new RuntimeException("Object already present: " + name + " in domain " + domain);
        }
        this.objects.put(domain, objects);
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public Map<EPackage, Map<String, EObject>> getObjects() {
        return objects;
    }
}
