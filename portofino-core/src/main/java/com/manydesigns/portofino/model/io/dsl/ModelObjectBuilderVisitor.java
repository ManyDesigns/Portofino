package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.language.ModelBaseVisitor;
import com.manydesigns.portofino.model.language.ModelParser;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class ModelObjectBuilderVisitor extends ModelBaseVisitor<EObject> {

    protected final Model model;
    protected final EPackage ownerPackage;

    public ModelObjectBuilderVisitor(Model model, EPackage ownerPackage) {
        this.model = model;
        this.ownerPackage = ownerPackage;
    }

    @Override
    public EObject visitObjectBody(ModelParser.ObjectBodyContext ctx) {
        String fullEntityName = ctx.className.getText();
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
                if (valueCtx.literal() != null) {
                    Object value = EcoreFactory.eINSTANCE.createFromString(
                            (EDataType) feature.getEType(), getLiteral(valueCtx.literal()));
                    eObject.eSet(feature, value);
                } else if (valueCtx.objectBody() != null) {
                    eObject.eSet(feature, visitObjectBody(valueCtx.objectBody()));
                } else {
                    ModelParser.PropertyListValueContext listCtx = valueCtx.propertyListValue();
                    EList list = (EList) eObject.eGet(feature);
                    listCtx.propertyValue().forEach(pv -> {
                        if (pv.literal() != null) {
                            //TODO type conversion
                            list.add(getLiteral(valueCtx.literal()));
                        } else if (pv.objectBody() != null) {
                            list.add(visitObjectBody(pv.objectBody()));
                        } else {
                            throw new RuntimeException("Nested lists not supported");
                        }
                    });
                }
            } else {
                throw new RuntimeException("Property " + featureName + " not found in " + fullEntityName);
            }
        }
        return eObject;
    }

    public static String getLiteral(ModelParser.LiteralContext lit) {
        if (lit.STRING() != null) {
            return lit.getText().substring(1, lit.getText().length() - 1);
        } else {
            return lit.getText();
        }
    }
}
