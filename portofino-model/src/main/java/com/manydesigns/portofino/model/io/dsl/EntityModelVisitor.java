package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.portofino.model.*;
import com.manydesigns.portofino.model.language.ModelBaseVisitor;
import com.manydesigns.portofino.model.language.ModelParser;
import org.antlr.v4.runtime.Token;

public class EntityModelVisitor extends ModelBaseVisitor<Domain> {

    protected Domain baseDomain;
    protected Entity entity;
    protected Annotated annotated;

    public EntityModelVisitor() {}

    public EntityModelVisitor(Domain baseDomain) {
        this.baseDomain = baseDomain;
    }

    @Override
    public Domain visitDomain(ModelParser.DomainContext ctx) {
        Domain previous = baseDomain;
        Domain domain = new Domain();
        if(baseDomain != null) {
            domain.setParent(baseDomain);
        }
        domain.setName(ctx.name.getText());
        baseDomain = domain;
        visitChildren(ctx);
        baseDomain = previous;
        return domain;
    }

    @Override
    public Domain visitEntity(ModelParser.EntityContext ctx) {
        String entityName = ctx.name.getText();
        if(baseDomain == null) {
            throw new IllegalStateException("Entity definition without a domain: " + entityName);
        }
        Entity previous = entity;
        entity = new Entity();
        entity.setName(entityName);
        entity.setDomain(baseDomain);
        annotated = entity;
        visitChildren(ctx);
        entity = previous;
        annotated = previous;
        return baseDomain;
    }

    @Override
    public Domain visitAnnotation(ModelParser.AnnotationContext ctx) {
        String annotationType = ctx.name.getText();
        if(annotated == null) {
            throw new IllegalStateException("Annotation without an element: " + annotationType);
        }
        Annotation annotation = new Annotation(annotated, resolveAnnotationType(annotationType));
        ModelParser.AnnotationParamsContext params = ctx.annotationParams();
        if(params.IDENTIFIER().isEmpty()) {
            annotation.getProperties().add(new AnnotationProperty("value", getText(params.literal(0))));
        } else {
            for(int i = 0; i < params.IDENTIFIER().size(); i++) {
                AnnotationProperty prop =
                        new AnnotationProperty(params.IDENTIFIER(i).getText(), getText(params.literal(i)));
                annotation.getProperties().add(prop);
            }
        }
        annotated.getAnnotations().add(annotation);
        return baseDomain;
    }

    protected String resolveAnnotationType(String annotationType) {
        return annotationType;
    }

    private String getText(ModelParser.LiteralContext value) {
        String text = value.getText();
        if(value.STRING() != null) {
            text = text.substring(1, text.length() - 2);
        }
        return text;
    }

    private String getText(Token token) {
        if(token.getType() == ModelParser.STRING) {
            return token.getText().substring(1, token.getText().length() - 2);
        } else {
            return token.getText();
        }
    }
}
