package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.portofino.model.*;
import com.manydesigns.portofino.model.database.annotations.Id;
import com.manydesigns.portofino.model.language.ModelBaseVisitor;
import com.manydesigns.portofino.model.language.ModelParser;
import org.antlr.v4.runtime.Token;
import org.eclipse.emf.ecore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class EntityModelVisitor extends ModelBaseVisitor<EModelElement> {

    protected final Model model;
    protected EPackage parentDomain;
    protected EClass entity;
    protected EModelElement annotated;

    public EntityModelVisitor(Model model) {
        this(model, null);
    }

    public EntityModelVisitor(Model model, EPackage parentDomain) {
        this.model = model;
        this.parentDomain = parentDomain;
    }

    @Override
    public EPackage visitStandaloneDomain(ModelParser.StandaloneDomainContext ctx) {
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
            domain = model.ensureDomain(name);
        }
        parentDomain = domain;
        annotated = domain;
        visitChildren(ctx);
        parentDomain = previous;
        annotated = previous;
        return domain;
    }

    @Override
    public EModelElement visitStandaloneEntity(ModelParser.StandaloneEntityContext ctx) {
        return super.visitStandaloneEntity(ctx);
    }

    @Override
    public EClass visitEntity(ModelParser.EntityContext ctx) {
        String entityName = ctx.name.getText();
        if(parentDomain == null) {
            throw new IllegalStateException("Entity definition without a domain: " + entityName);
        }
        EClass previous = entity;
        entity = (EClass) parentDomain.getEClassifier(entityName);
        if(entity == null) {
            entity = EcoreFactory.eINSTANCE.createEClass();
            entity.setName(entityName);
            parentDomain.getEClassifiers().add(entity);
        }
        annotated = entity;
        visitChildren(ctx);
        for (int i = 0; i < ctx.idProperties.size(); i++) {
            ModelParser.PropertyContext idp = ctx.idProperties.get(i);
            String propertyName = idp.name.getText();
            Optional<EAttribute> property = entity.getEAttributes().stream().filter(
                    p -> p.getName().equals(propertyName)
            ).findFirst();
            if (property.isPresent()) {
                EAnnotation idAnn = EcoreFactory.eINSTANCE.createEAnnotation();
                idAnn.setSource(Id.class.getName());
                idAnn.getDetails().put("order", i + "");
                property.get().getEAnnotations().add(idAnn);
            } else {
                throw new IllegalStateException("Id property not found: " + propertyName);
            }
        }
        entity = previous;
        annotated = previous;
        return entity;
    }

    @Override
    public EAnnotation visitAnnotation(ModelParser.AnnotationContext ctx) {
        String annotationType = ctx.name.getText();
        if(annotated == null) {
            throw new IllegalStateException("Annotation without an element: " + annotationType);
        }
        EAnnotation annotation = annotated.getEAnnotation(resolveAnnotationType(annotationType));
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
    public EAttribute visitProperty(ModelParser.PropertyContext ctx) {
        String propertyName = ctx.name.getText();
        EModelElement previous = annotated;
        if(entity == null) {
            throw new IllegalStateException("Property without an entity: " + propertyName);
        }
        EDataType type;
        if(ctx.type() != null) {
            String typeName = ctx.type().name.getText();
            type = (EDataType) EcorePackage.eINSTANCE.getEClassifier(typeName);
            if(type == null) {
                throw new RuntimeException("Unknown type: " + typeName); //TODO
            }
        } else {
            type = EcorePackage.eINSTANCE.getEString();
        }

        EAttribute property = entity.getEAttributes().stream().filter(a -> propertyName.equals(a.getName()))
                .findFirst().orElseGet(() -> {
                    EAttribute attr = EcoreFactory.eINSTANCE.createEAttribute();
                    attr.setName(propertyName);
                    entity.getEAttributes().add(attr);
                    return attr;
                });
        property.setEType(type);
        annotated = property;
        visitChildren(ctx);
        annotated = previous;
        return property;
    }

    protected String decodeType(String typeName) {
        return typeName;
    }

    protected String resolveAnnotationType(String annotationType) {
        return annotationType;
    }

    private String getText(ModelParser.LiteralContext value) {
        String text = value.getText();
        if(value.STRING() != null) {
            text = text.substring(1, text.length() - 1);
        }
        return text;
    }
}
