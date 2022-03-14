package com.manydesigns.portofino.model.io.dsl;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.manydesigns.portofino.model.language.ModelBaseVisitor;
import com.manydesigns.portofino.model.language.ModelParser;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EcoreFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityModelBaseVisitor extends ModelBaseVisitor<EModelElement> {
    protected final Map<String, String> typeAliases = new HashMap<>();
    protected EModelElement annotated;

    public static BiMap<String, String> getDefaultTypeAliases() {
        ImmutableBiMap.Builder<String, String> defaults = ImmutableBiMap.builder();
        defaults.put("boolean!", "EBoolean");
        defaults.put("boolean", "EBooleanObject");
        defaults.put("long!", "ELong");
        defaults.put("long", "ELongObject");
        defaults.put("int!", "EInt");
        defaults.put("int", "EIntObject");
        defaults.put("string", "EString");
        return defaults.build();
    }

    protected void initTypeAliases(List<ModelParser.ImportDeclarationContext> importDeclarations) {
        typeAliases.clear();
        typeAliases.putAll(EntityModelBaseVisitor.getDefaultTypeAliases());
        importDeclarations.forEach(i -> {
            String alias;
            String typeName = i.name.getText();
            if (i.alias != null) {
                alias = i.alias.getText();
            } else {
                int beginIndex = typeName.lastIndexOf('.');
                if (beginIndex < 0) {
                    return;
                }
                alias = typeName.substring(beginIndex + 1);
            }
            //TODO warning/error if already present
            typeAliases.put(alias, typeName);
        });
    }

    public String resolveType(String typeName, boolean nullable) {
        String resolved = null;
        if (!nullable) {
            resolved = typeAliases.get(typeName + "!");
        }
        if (resolved == null) {
            resolved = typeAliases.get(typeName);
        }
        return resolved != null ? resolved : typeName;
    }

    @Override
    public EAnnotation visitAnnotation(ModelParser.AnnotationContext ctx) {
        String annotationType = ctx.type().getText();
        if (annotated == null) {
            throw new IllegalStateException("Annotation without an element: " + annotationType);
        }
        String source = resolveType(annotationType, false);
        EAnnotation annotation = annotated.getEAnnotation(source);
        if (annotation == null) {
            annotation = EcoreFactory.eINSTANCE.createEAnnotation();
            annotation.setSource(source);
            annotated.getEAnnotations().add(annotation);
        }
        ModelParser.AnnotationParamsContext params = ctx.annotationParams();
        if (params != null) {
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

    private String getText(ModelParser.LiteralContext value) {
        String text = value.getText();
        if (value.STRING() != null) {
            text = text.substring(1, text.length() - 1);
        }
        return text;
    }
}
