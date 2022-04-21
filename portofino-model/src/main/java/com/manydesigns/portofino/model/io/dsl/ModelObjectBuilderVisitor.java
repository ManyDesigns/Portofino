package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.language.ModelBaseVisitor;
import com.manydesigns.portofino.model.language.ModelParser;
import org.apache.commons.lang3.StringUtils;
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
                if (propertyAss.literal() != null) {
                    eObject.eSet(feature, propertyAss.literal().getText()); //TODO type conversion
                } else {
                    eObject.eSet(feature, visitObjectBody(propertyAss.objectBody()));
                }
            } else {
                throw new RuntimeException("Property " + featureName + " not found in " + fullEntityName);
            }
        }
        return eObject;
    }
}
