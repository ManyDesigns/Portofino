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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;
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

    public Domain(String name) {
        setName(name);
    }

    @Override
    protected EClass eStaticClass() {
        return CLASS;
    }

    public EMap<String, EObject> getObjects() {
        return objects;
    }

    public Object getJavaObject(String name)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException,
            IntrospectionException, NoSuchFieldException {
        EObject eObject = objects.get(name);
        if (eObject == null) {
            return null;
        }
        EClass eClass = eObject.eClass();
        if (eClass == null || eClass.getInstanceClass() == null) {
            return null;
        }
        Class<?> javaClass = eClass.getInstanceClass();
        Object object = javaClass.getConstructor().newInstance();
        PropertyDescriptor[] props = Introspector.getBeanInfo(javaClass).getPropertyDescriptors();
        for (EStructuralFeature feature : eClass.getEAllStructuralFeatures()) {
            Optional<PropertyDescriptor> pd =
                    Arrays.stream(props).filter(p -> p.getName().equals(feature.getName())).findFirst();
            PropertyDescriptor propertyDescriptor = pd.orElseThrow(() -> new NoSuchFieldException(feature.getName()));
            propertyDescriptor.getWriteMethod().invoke(object, eObject.eGet(feature));
        }
        return object;
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

    public Optional<Domain> getSubdomain(String name) {
        return getSubdomains().stream().filter(d -> d.getName().equals(name)).findFirst();
    }
}
