/*
 * Copyright (C) 2005-2021 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.portofino.model.PortofinoPackage;
import com.manydesigns.portofino.model.annotations.KeyMappings;
import com.manydesigns.portofino.model.annotations.Id;
import com.manydesigns.portofino.model.language.ModelParser;
import org.eclipse.emf.ecore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EntityModelBuilderVisitor extends EntityModelBaseVisitor {

    protected EPackage parentDomain;
    protected EClass entity;
    private static final Logger logger = LoggerFactory.getLogger(EntityModelBuilderVisitor.class);

    public EntityModelBuilderVisitor() {
        this(null);
    }

    public EntityModelBuilderVisitor(EPackage parentDomain) {
        this.parentDomain = parentDomain;
    }

    @Override
    public EPackage visitStandaloneDomain(ModelParser.StandaloneDomainContext ctx) {
        List<ModelParser.ImportDeclarationContext> importDeclarations = ctx.importDeclaration();
        initTypeAliases(importDeclarations);
        return visitDomain(ctx.domain());
    }

    @Override
    public EPackage visitDomain(ModelParser.DomainContext ctx) {
        EPackage previous = parentDomain;
        String name = ctx.name.getText();
        EPackage domain;
        if(parentDomain != null) {
            domain = parentDomain.getESubpackages().stream().filter(p -> name.equals(p.getName())).findFirst().orElseGet(() -> {
                EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
                ePackage.setName(name);
                parentDomain.getESubpackages().add(ePackage);
                return ePackage;
            });
        } else {
            domain = EcoreFactory.eINSTANCE.createEPackage();
            domain.setName(name);
        }
        parentDomain = domain;
        annotated = domain;
        visitChildren(ctx);
        parentDomain = previous;
        annotated = previous;
        return domain;
    }

    @Override
    public EClass visitStandaloneEntity(ModelParser.StandaloneEntityContext ctx) {
        List<ModelParser.ImportDeclarationContext> importDeclarations = ctx.importDeclaration();
        initTypeAliases(importDeclarations);
        return visitEntity(ctx.entity());
    }

    @Override
    public EClass visitEntity(ModelParser.EntityContext ctx) {
        String entityName = ctx.name.getText();
        EClass previous = entity;
        EClass entity = null;
        if(parentDomain != null) {
            entity = (EClass) parentDomain.getEClassifier(entityName);
        }
        if(entity == null) {
            entity = EcoreFactory.eINSTANCE.createEClass();
            entity.setName(entityName);
            if(parentDomain != null) {
                parentDomain.getEClassifiers().add(entity);
            }
        }
        annotated = entity;
        this.entity = entity;
        visitChildren(ctx);
        for (int i = 0; i < ctx.idProperties.size(); i++) {
            ModelParser.PropertyContext idp = ctx.idProperties.get(i);
            String propertyName = idp.name.getText();
            Optional<EStructuralFeature> property = entity.getEStructuralFeatures().stream().filter(
                    p -> p.getName().equals(propertyName)
            ).findFirst();
            if (property.isPresent()) {
                EAnnotation idAnn = property.get().getEAnnotations().stream()
                        .filter(a -> a.getSource().equals(Id.class.getName())).findFirst()
                        .orElseGet(() -> {
                            EAnnotation ann = EcoreFactory.eINSTANCE.createEAnnotation();
                            ann.setSource(Id.class.getName());
                            property.get().getEAnnotations().add(ann);
                            return ann;
                        });
                idAnn.getDetails().put("order", i + "");
            } else {
                throw new IllegalStateException("Id property not found: " + propertyName);
            }
        }
        annotated = previous;
        this.entity = previous;
        return entity;
    }

    @Override
    public EStructuralFeature visitProperty(ModelParser.PropertyContext ctx) {
        String propertyName = ctx.name.getText();
        EModelElement previous = annotated;
        if(entity == null) {
            throw new IllegalStateException("Property without an entity: " + propertyName);
        }
        EDataType type = null;
        boolean nullable = ctx.NOT_NULLABLE() == null;
        if(ctx.type() != null) {
            String typeName = ctx.type().name.getText();
            //TODO should we be using the CodeBase for this?
            type = PortofinoPackage.ensureType(resolveType(typeName, nullable));
        }

        EStructuralFeature property = entity.getEStructuralFeatures().stream().filter(a -> propertyName.equals(a.getName()))
                .findFirst().orElseGet(() -> {
                    EAttribute attr = EcoreFactory.eINSTANCE.createEAttribute();
                    attr.setName(propertyName);
                    attr.setLowerBound(nullable ? 0 : 1);
                    entity.getEStructuralFeatures().add(attr);
                    return attr;
                });
        if(!(property instanceof EAttribute)) {
            throw new IllegalStateException("Cannot define property " + propertyName + ", a reference with the same name already exists in " + entity.getName());
        }
        property.setEType(type);
        annotated = property;
        visitChildren(ctx);
        annotated = previous;
        return property;
    }

}
