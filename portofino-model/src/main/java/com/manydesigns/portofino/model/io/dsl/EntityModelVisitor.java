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

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.manydesigns.portofino.model.PortofinoPackage;
import com.manydesigns.portofino.model.database.annotations.Id;
import com.manydesigns.portofino.model.language.ModelBaseVisitor;
import com.manydesigns.portofino.model.language.ModelParser;
import org.eclipse.emf.ecore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EntityModelVisitor extends ModelBaseVisitor<EModelElement> {

    protected EPackage parentDomain;
    protected EClass entity;
    protected EModelElement annotated;
    protected final Map<String, String> typeAliases = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(EntityModelVisitor.class);

    public EntityModelVisitor() {
        this(null);
    }

    public EntityModelVisitor(EPackage parentDomain) {
        this.parentDomain = parentDomain;
    }

    @Override
    public EPackage visitStandaloneDomain(ModelParser.StandaloneDomainContext ctx) {
        List<ModelParser.ImportDeclarationContext> importDeclarations = ctx.importDeclaration();
        initTypeAliases(importDeclarations);
        return visitDomain(ctx.domain());
    }

    protected void initTypeAliases(List<ModelParser.ImportDeclarationContext> importDeclarations) {
        typeAliases.clear();
        typeAliases.putAll(getDefaultTypeAliases());
        importDeclarations.forEach(i -> {
            String alias;
            String typeName = i.name.getText();
            if(i.alias != null) {
                alias = i.alias.getText();
            } else {
                int beginIndex = typeName.lastIndexOf('.');
                if(beginIndex < 0) {
                    return;
                }
                alias = typeName.substring(beginIndex);
            }
            //TODO warning/error if already present
            typeAliases.put(alias, typeName);
        });
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

    public static BiMap<String, String> getDefaultTypeAliases() {
        ImmutableBiMap.Builder<String, String> defaults = ImmutableBiMap.builder();
        defaults.put("long", "ELong");
        defaults.put("long?", "ELongObject");
        defaults.put("int", "EInt");
        defaults.put("int?", "EIntObject");
        return defaults.build();
    }

    public String resolveType(String typeName, boolean nullable) {
        String resolved = null;
        if(nullable) {
            resolved = typeAliases.get(typeName + "?");
        }
        if(resolved == null) {
            resolved = typeAliases.get(typeName);
        }
        return resolved != null ? resolved : typeName;
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
    public EAnnotation visitAnnotation(ModelParser.AnnotationContext ctx) {
        String annotationType = ctx.type().getText();
        if(annotated == null) {
            throw new IllegalStateException("Annotation without an element: " + annotationType);
        }
        String source = resolveType(annotationType, false);
        EAnnotation annotation = annotated.getEAnnotation(source);
        if(annotation == null) {
            annotation = EcoreFactory.eINSTANCE.createEAnnotation();
            annotation.setSource(source);
            annotated.getEAnnotations().add(annotation);
        }
        ModelParser.AnnotationParamsContext params = ctx.annotationParams();
        if(params != null) {
            visitAnnotationParams(annotation, params);
        }
        return annotation;
    }

    public void visitAnnotationParams(EAnnotation annotation, ModelParser.AnnotationParamsContext params) {
        if (params.simpleIdentifier().isEmpty()) {
            annotation.getDetails().put("value", getText(params.literal(0)));
        } else {
            for (int i = 0; i < params.simpleIdentifier().size(); i++) {
                annotation.getDetails().put(params.simpleIdentifier(i).getText(), getText(params.literal(i)));
            }
        }
    }

    @Override
    public EStructuralFeature visitProperty(ModelParser.PropertyContext ctx) {
        String propertyName = ctx.name.getText();
        EModelElement previous = annotated;
        if(entity == null) {
            throw new IllegalStateException("Property without an entity: " + propertyName);
        }
        EDataType type;
        if(ctx.type() != null) {
            String typeName = ctx.type().name.getText();
            //TODO should we be using the CodeBase for this?
            type = PortofinoPackage.ensureType(resolveType(typeName, ctx.nullable != null));
        } else {
            type = EcorePackage.eINSTANCE.getEString();
        }

        EStructuralFeature property = entity.getEStructuralFeatures().stream().filter(a -> propertyName.equals(a.getName()))
                .findFirst().orElseGet(() -> {
                    EAttribute attr = EcoreFactory.eINSTANCE.createEAttribute();
                    attr.setName(propertyName);
                    entity.getEStructuralFeatures().add(attr);
                    return attr;
                });
        property.setEType(type);
        annotated = property;
        visitChildren(ctx);
        annotated = previous;
        return property;
    }

    private String getText(ModelParser.LiteralContext value) {
        String text = value.getText();
        if(value.STRING() != null) {
            text = text.substring(1, text.length() - 1);
        }
        return text;
    }
}
