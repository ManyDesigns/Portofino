package com.manydesigns.portofino.model;

import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.util.EObjectEList;
import org.eclipse.emf.ecore.util.EcoreEList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class Domain extends EPackageImpl {

    public static final EClass CLASS;
    public static final EAttribute OBJECTS_ATTRIBUTE;

    static {
        CLASS = EcoreFactory.eINSTANCE.createEClass();
        CLASS.getESuperTypes().add(EcorePackage.eINSTANCE.getEPackage());
        CLASS.setName("Domain");
        CLASS.setInstanceClass(Domain.class);

        OBJECTS_ATTRIBUTE = EcoreFactory.eINSTANCE.createEAttribute();
        OBJECTS_ATTRIBUTE.setName("objects");
        OBJECTS_ATTRIBUTE.setEType(EcorePackage.eINSTANCE.getEMap());
        CLASS.getEStructuralFeatures().add(OBJECTS_ATTRIBUTE);
    }

    private final EMap<String, EObject> objects = new BasicEMap<>();

    public Domain() {}

    @Override
    protected EClass eStaticClass() {
        return CLASS;
    }

    public EMap<String, EObject> getObjects() {
        return objects;
    }

    public EList<Domain> getSubdomains() {
        return (EList) getESubpackages(); // TODO check that only domains can be added
    }

    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        if (featureID == OBJECTS_ATTRIBUTE.getFeatureID()) {
            return objects;
        } else {
            return super.eGet(featureID, resolve, coreType);
        }
    }

    @Override
    public boolean eIsSet(int featureID) {
        if (featureID == OBJECTS_ATTRIBUTE.getFeatureID()) {
            return objects != null;
        } else {
            return super.eIsSet(featureID);
        }
    }
}
