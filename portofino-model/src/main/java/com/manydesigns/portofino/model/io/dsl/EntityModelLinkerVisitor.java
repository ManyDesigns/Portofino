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
import com.manydesigns.portofino.model.database.annotations.KeyMappings;
import com.manydesigns.portofino.model.language.ModelBaseVisitor;
import com.manydesigns.portofino.model.language.ModelParser;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EntityModelLinkerVisitor extends EntityModelBaseVisitor {

    protected EPackage parentDomain;
    protected EPackage initialDomain;
    protected EClass entity;
    private static final Logger logger = LoggerFactory.getLogger(EntityModelLinkerVisitor.class);

    public EntityModelLinkerVisitor() {
        this(null);
    }

    public EntityModelLinkerVisitor(EPackage parentDomain) {
        this.parentDomain = parentDomain;
    }

    @Override
    public EPackage visitStandaloneDomain(ModelParser.StandaloneDomainContext ctx) {
        List<ModelParser.ImportDeclarationContext> importDeclarations = ctx.importDeclaration();
        initTypeAliases(importDeclarations);
        initialDomain = parentDomain;
        parentDomain = null;
        return visitDomain(ctx.domain());
    }

    @Override
    public EPackage visitDomain(ModelParser.DomainContext ctx) {
        EPackage previous = parentDomain;
        String name = ctx.name.getText();
        EPackage domain =
            parentDomain == null ?
            initialDomain :
            parentDomain.getESubpackages().stream()
                    .filter(p -> name.equals(p.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No subdomain called " + name + " found in " + parentDomain.getName()));
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
        EClass entity = (EClass) parentDomain.getEClassifier(entityName);;
        annotated = entity;
        this.entity = entity;
        visitChildren(ctx);
        annotated = previous;
        this.entity = previous;
        return entity;
    }

    @Override
    public EModelElement visitProperty(ModelParser.PropertyContext ctx) {
        return null;
    }

    @Override
    public EModelElement visitRelationshipProperty(ModelParser.RelationshipPropertyContext ctx) {
        String propertyName = ctx.name.getText();
        if(entity == null) {
            throw new IllegalStateException("Relationship without an entity: " + propertyName);
        }
        String typeName = ctx.type().name.getText();
        //TODO should we be using the CodeBase for this?
        EClassifier type = null;
        if(parentDomain != null) {
            type = parentDomain.getEClassifier(typeName);
        }
        if(type == null) {
            type = PortofinoPackage.ensureType(resolveType(typeName, false));
        }
        EReference reference = ensureReference(entity, propertyName, false);
        reference.setEType(type);
        if(type instanceof EClass) {
            EClass eClass = (EClass) type;
            ModelParser.RelationshipMappingsContext mappings = ctx.relationshipMappings();
            if(mappings != null) {
                EList<EAttribute> eKeys = reference.getEKeys();
                EAnnotation ann = EcoreFactory.eINSTANCE.createEAnnotation();
                ann.setSource(KeyMappings.class.getName());
                mappings.relationshipMapping().forEach(m -> {
                    String ownName = m.ownName.getText();
                    String otherName = m.otherName.getText();
                    if(!(entity.getEStructuralFeature(ownName) instanceof EAttribute)) {
                        throw new IllegalStateException("Invalid reference " + entity.getName() + "." + ownName);
                    }
                    if(!(eClass.getEStructuralFeature(otherName) instanceof EAttribute)) {
                        throw new IllegalStateException("Invalid reference " + eClass.getName() + "." + otherName);
                    }
                    eKeys.add((EAttribute) eClass.getEStructuralFeature(otherName));
                    ann.getDetails().put(ownName, otherName);
                });
                reference.getEAnnotations().add(ann);
            }

            if(eClass != entity) {
                EReference opposite = ensureReference(eClass, propertyName, true);
                opposite.setEOpposite(reference);
                reference.setEOpposite(opposite);
            }
        }
        TerminalNode range = ctx.RANGE();
        if(range != null) {
            int sepIndex = range.getText().indexOf("..");
            String upperBound = "";
            if(sepIndex > 0) {
                reference.setLowerBound(Integer.parseInt(range.getText().substring(0, sepIndex)));
                upperBound = range.getText().substring(sepIndex + 2).trim();
            }
            if(StringUtils.isNotEmpty(upperBound) && !"*".equals(upperBound)) {
                reference.setUpperBound(Integer.parseInt(upperBound));
            }
        }

        EModelElement previous = annotated;
        try {
            annotated = reference;
            visitChildren(ctx);
        } finally {
            annotated = previous;
        }

        return reference;
    }

    protected EReference ensureReference(EClass entity, String propertyName, boolean derived) {
        EStructuralFeature feature = entity.getEStructuralFeatures().stream()
                .filter(a -> propertyName.equals(a.getName()))
                .findFirst().orElseGet(() -> {
                    EReference reference = EcoreFactory.eINSTANCE.createEReference();
                    reference.setName(propertyName);
                    reference.setDerived(derived);
                    entity.getEStructuralFeatures().add(reference);
                    return reference;
                });
        if(feature instanceof EReference) {
            if(feature.isDerived() == derived) {
                return (EReference) feature;
            } else {
                throw new IllegalStateException("Cannot redefine reference " + propertyName + " in " + entity.getName());
            }
        } else {
            throw new IllegalStateException("Cannot define reference " + propertyName + ", an attribute with the same name already exists in " + entity.getName());
        }
    }
}
