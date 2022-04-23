package com.manydesigns.portofino.model;

import com.manydesigns.portofino.model.annotations.Transient;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

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

    public void addObject(String name, EObject object) {
        if(getObjects().containsKey(name)) {
            throw new RuntimeException("Object already present: " + name + " in domain " + getName());
        }
        getObjects().put(name, object);
    }

    public EObject putObject(String name, Object javaObject, Domain domain)
            throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Class<?> javaClass = javaObject.getClass();
        EClass eClass = domain.findClass(javaClass);
        EObject object = eClass.getEPackage().getEFactoryInstance().create(eClass);
        for (PropertyDescriptor prop : getPersistentProperties(javaClass)) {
            object.eSet(eClass.getEStructuralFeature(prop.getName()), prop.getReadMethod().invoke(javaObject));
        }
        getObjects().put(name, object);
        return object;
    }

    public EClass findClass(Class<?> aClass) {
        String className = aClass.getSimpleName();
        Domain pkg = resolveDomain(aClass.getPackageName());
        EClassifier eClassifier = pkg.getEClassifier(className);
        if (eClassifier instanceof EClass) {
            return (EClass) eClassifier;
        } else {
            throw new IllegalArgumentException("Not a modeled class: " + aClass.getName());
        }
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

    public Domain resolveDomain(String name) {
        return resolveDomain(name, getSubdomains());
    }

    @Nullable
    public static Domain resolveDomain(String name, List<Domain> domains) {
        String[] components = name.split("[.]");
        Domain result = null;
        for(String s : components) {
            Optional<Domain> domain = domains.stream().filter(p -> s.equals(p.getName())).findFirst();
            if(domain.isPresent()) {
                result = domain.get();
                domains = result.getSubdomains();
            } else {
                throw new IllegalArgumentException("Domain " + name + " not known");
            }
        }
        return result;
    }

    public Domain ensureDomain(String name) {
        return ensureDomain(name, getSubdomains());
    }

    public static Domain ensureDomain(String name, List<Domain> domains) {
        return ensureDomain(name, domains, null);
    }

    @NotNull
    public static Domain ensureDomain(String name, List<Domain> domains, Consumer<Domain> initializer) {
        return domains.stream().filter(d -> d.getName().equals(name)).findFirst().orElseGet(() -> {
            Domain domain = new Domain();
            domain.setName(name);
            domains.add(domain);
            if(initializer != null) {
                initializer.accept(domain);
            }
            return domain;
        });
    }

    public static List<PropertyDescriptor> getPersistentProperties(Class<?> javaClass) throws IntrospectionException {
        PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(javaClass).getPropertyDescriptors();
        List<PropertyDescriptor> result = new ArrayList<>();
        for (PropertyDescriptor prop : propertyDescriptors) {
            if (prop.getWriteMethod() != null && prop.getReadMethod() != null) {
                if (getAnnotation(prop, Transient.class) == null) {
                    result.add(prop);
                }
            }
        }
        return result;
    }

    public static <T extends Annotation> T getAnnotation(PropertyDescriptor prop, Class<T> annClass) {
        Class<?> declaringClass = null;
        if (prop.getReadMethod() != null) {
            T annotation = prop.getReadMethod().getAnnotation(annClass);
            if (annotation != null) {
                return annotation;
            } else {
                declaringClass = prop.getReadMethod().getDeclaringClass();
            }
        }
        if (prop.getWriteMethod() != null) {
            T annotation = prop.getReadMethod().getAnnotation(annClass);
            if (annotation != null) {
                return annotation;
            } else {
                declaringClass = prop.getWriteMethod().getDeclaringClass();
            }
        }
        if (declaringClass != null) {
            try {
                Field field = declaringClass.getDeclaredField(prop.getName());
                return field.getAnnotation(annClass);
            } catch (NoSuchFieldException e) {
                return null;
            }
        } else {
            return null;
        }
    }
}
