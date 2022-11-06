package com.manydesigns.portofino.model.io;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.language.ModelParser;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.util.List;
import java.util.stream.Collectors;

public class ModelObjectBuilderVisitor extends ModelObjectBaseVisitor {

    protected final Model model;
    protected final EPackage ownerPackage;

    public ModelObjectBuilderVisitor(Model model, EPackage ownerPackage) {
        this.model = model;
        this.ownerPackage = ownerPackage;
    }

    @Override
    public EObject visitStandaloneObject(ModelParser.StandaloneObjectContext ctx) {
        List<ModelParser.ImportDeclarationContext> importDeclarations = ctx.importDeclaration();
        initTypeAliases(importDeclarations);
        return visitObjectBody(ctx.object().objectBody());
    }

    @Override
    public EObject visitObjectBody(ModelParser.ObjectBodyContext ctx) {
        String fullEntityName = resolveType(ctx.className.getText(), true);
        EClass eClass;
        if(fullEntityName.indexOf('.') >= 0) {
            String entityName = StringUtils.substringAfterLast(fullEntityName, ".");
            String packageName = StringUtils.substringBeforeLast(fullEntityName, ".");
            eClass = (EClass) model.getDomain(packageName).getEClassifier(entityName);
        } else {
            eClass = (EClass) ownerPackage.getEClassifier(fullEntityName);
        }
        if (eClass == null) {
            throw new RuntimeException("Entity " + fullEntityName + " not found");
        }
        EObject eObject = EcoreUtil.create(eClass);
        for (ModelParser.PropertyAssignmentContext propertyAss : ctx.properties) {
            String featureName = propertyAss.name.getText();
            EStructuralFeature feature = eClass.getEStructuralFeature(featureName);
            if(feature != null) {
                ModelParser.PropertyValueContext valueCtx = propertyAss.propertyValue();
                Object value = translate(feature, valueCtx);
                if (feature.isMany()) {
                    EList list = (EList) eObject.eGet(feature);
                    list.addAll((List) value);
                } else {
                    eObject.eSet(feature, value);
                }
            } else {
                throw new RuntimeException("Property " + featureName + " not found in " + fullEntityName);
            }
        }
        return eObject;
    }

    protected Object translate(EStructuralFeature feature, ModelParser.PropertyValueContext valueCtx) {
        EClassifier type = feature.getEType();
        if (valueCtx.literal() != null) {
            if (!(type instanceof EDataType)) {
                throw new RuntimeException("Type expected for feature " + feature + ", got" + type);
            }
            return EcoreFactory.eINSTANCE.createFromString((EDataType) type, getLiteral(valueCtx.literal()));
        } else if (valueCtx.objectBody() != null) {
            return visitObjectBody(valueCtx.objectBody());
        } else if (valueCtx.enumValue != null) {
            if (type instanceof EEnum) {
                return ((EEnum) type).getEEnumLiteral(valueCtx.enumValue.getText());
            } else {
                throw new RuntimeException(feature + ": enum expected, but type is " + type);
            }
        } else if (valueCtx.propertyListValue() != null) {
            return valueCtx.propertyListValue().propertyValue().stream()
                    .map(pv -> translate(feature, pv))
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public static String getLiteral(ModelParser.LiteralContext lit) {
        if (lit.STRING() != null) {
            return lit.getText().substring(1, lit.getText().length() - 1);
        } else {
            return lit.getText();
        }
    }
}
