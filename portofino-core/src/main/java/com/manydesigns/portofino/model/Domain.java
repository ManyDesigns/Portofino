package com.manydesigns.portofino.model;

import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.model.annotations.Transient;
import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;

public class Domain extends EPackageImpl {

    public static final EClass CLASS;
    public static final EAttribute OBJECTS_ATTRIBUTE;
    public static final String PATH_SEPARATOR = ".";

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

    public void putObject(String name, EObject object) {
        if(getObjects().containsKey(name)) {
            throw new RuntimeException("Object already present: " + name + " in domain " + getName());
        }
        getObjects().put(name, object);
    }

    public EObject removeObject(String name) {
        return getObjects().removeKey(name);
    }

    /**
     * Registers an object under a given name.
     * @param name the name of the object.
     * @param javaObject the object. To be stored in the model, it has to be translated to a model object.
     * @param domain the {@link Domain} containing the entity definitions necessary to model the object.
     * @return the object translated to a model object.
     */
    public EObject putObject(String name, Object javaObject, Domain domain)
            throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        EObject object = toEObject(javaObject, domain);
        getObjects().put(name, object);
        return object;
    }

    public static EObject toEObject(Object javaObject, Domain domain)
            throws IntrospectionException, IllegalAccessException, InvocationTargetException, IllegalArgumentException {
        if (javaObject == null) {
            return null;
        }
        Class<?> javaClass = javaObject.getClass();
        EClassifier eClassifier = domain.findClass(javaClass);
        if (eClassifier instanceof EClass) {
            return toEObject(javaObject, domain, javaClass, (EClass) eClassifier);
        } else if (eClassifier instanceof EEnum) {
            EEnum eEnum = (EEnum) eClassifier;
            return eEnum.getEEnumLiteralByLiteral(((Enum<?>) javaObject).name());
        }
        throw new IllegalArgumentException(
                "We don't know how to convert " + javaObject + " in the domain " + domain.getName());
    }

    protected static EObject toEObject(
            Object javaObject, Domain domain, Class<?> javaClass, EClass eClass
    ) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        EObject object = EcoreUtil.create(eClass);
        for (PropertyDescriptor prop : getPersistentProperties(javaClass)) {
            EStructuralFeature eStructuralFeature = eClass.getEStructuralFeature(prop.getName());
            Object value = prop.getReadMethod().invoke(javaObject);
            if(eStructuralFeature.isMany()) {
                if (value != null) {
                    EList list = (EList<?>) object.eGet(eStructuralFeature);
                    for (Object elem : (Iterable<?>) value) {
                        list.add(toEObject(elem, domain));
                    }
                }
            } else if(eStructuralFeature instanceof EAttribute || value == null) {
                object.eSet(eStructuralFeature, value);
            } else {
                object.eSet(eStructuralFeature, toEObject(value, domain));
            }
        }
        return object;
    }

    public EClassifier findClass(Class<?> aClass) {
        String className = aClass.getSimpleName();
        Domain pkg = resolveDomain(aClass.getPackageName());
        EClassifier eClassifier = pkg.getEClassifier(className);
        if (eClassifier instanceof EClass || eClassifier instanceof EEnum) {
            return eClassifier;
        } else {
            throw new IllegalArgumentException("Not a modeled class: " + aClass.getName());
        }
    }

    public EList<Domain> getSubdomains() {
        return (EList) getESubpackages(); // TODO check that only domains can be added
    }

    public Domain getParentDomain() {
        EObject eObject = eContainer();
        if (eObject instanceof Domain) {
            return (Domain) eObject;
        } else {
            return null;
        }
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
                if (ReflectionUtil.getAnnotation(prop, Transient.class) == null) {
                    result.add(prop);
                }
            }
        }
        return result;
    }

    public static Object toJavaObject(EObject eObject, Domain classesDomain, CodeBase codeBase) throws Exception {
        if (eObject == null) {
            return null;
        }
        if (eObject instanceof EEnumLiteral) {
            Class javaClass = getJavaClass(classesDomain, codeBase, ((EEnumLiteral) eObject).getEEnum());
            if (!Enum.class.isAssignableFrom(javaClass)) {
                throw new RuntimeException("Not an enum: " + javaClass);
            }
            return Enum.valueOf(javaClass, ((EEnumLiteral) eObject).getName());
        }
        EClass eClass = eObject.eClass();
        if (eClass == null) {
            return null;
        }
        Class<?> javaClass = getJavaClass(classesDomain, codeBase, eClass);
        Object object = javaClass.getConstructor().newInstance();
        PropertyDescriptor[] props = Introspector.getBeanInfo(javaClass).getPropertyDescriptors();
        for (EStructuralFeature feature : eClass.getEAllStructuralFeatures()) {
            Optional<PropertyDescriptor> pd =
                    Arrays.stream(props).filter(p -> p.getName().equals(feature.getName())).findFirst();
            PropertyDescriptor propertyDescriptor = pd.orElseThrow(() -> new NoSuchFieldException(feature.getName()));
            if (feature.isMany()) {
                setCollection(eObject, classesDomain, codeBase, object, feature, propertyDescriptor);
            } else {
                Object value = eObject.eGet(feature);
                if (value instanceof EObject) {
                    value = toJavaObject((EObject) value, classesDomain, codeBase);
                }
                propertyDescriptor.getWriteMethod().invoke(object, value);
            }
        }
        return object;
    }

    protected static void setCollection(
            EObject eObject, Domain classesDomain, CodeBase codeBase, Object object, EStructuralFeature feature,
            PropertyDescriptor propertyDescriptor
    ) throws Exception {
        Collection<Object> values = (Collection) eObject.eGet(feature);
        Collection<Object> coll;
        if (propertyDescriptor.getWriteMethod() != null) {
            Class<?> collType = propertyDescriptor.getReadMethod().getReturnType();
            if (Modifier.isAbstract(collType.getModifiers())) {
                if (List.class.isAssignableFrom(collType)) {
                    if (!collType.isAssignableFrom(ArrayList.class)) {
                        throw new RuntimeException("Unsupported list type: " + collType);
                    }
                    coll = new ArrayList<>();
                } else if (Set.class.isAssignableFrom(collType)) {
                    if (!collType.isAssignableFrom(HashSet.class)) {
                        throw new RuntimeException("Unsupported set type: " + collType);
                    }
                    coll = new HashSet<>();
                } else {
                    throw new RuntimeException("Unsupported collection type: " + collType);
                }
            } else {
                coll = (Collection<Object>) collType.getConstructor().newInstance();
            }
        } else {
            coll = (Collection) propertyDescriptor.getReadMethod().invoke(object);
            coll.clear();
        }
        for (Object value : values) {
            if (value instanceof EObject) {
                coll.add(toJavaObject((EObject) value, classesDomain, codeBase));
            } else {
                coll.add(value);
            }
        }
        if (propertyDescriptor.getWriteMethod() != null) {
            propertyDescriptor.getWriteMethod().invoke(object, coll);
        }
    }

    private static Class<?> getJavaClass(
            Domain classesDomain, CodeBase codeBase, EClassifier type
    ) throws IOException, ClassNotFoundException {
        String javaClassName = type.getName();
        EPackage pkg = type.getEPackage();
        while (!classesDomain.equals(pkg)) {
            javaClassName = pkg.getName() + "." + javaClassName;
            pkg = pkg.getESuperPackage();
        }
        return (Class<?>) codeBase.loadClass(javaClassName);
    }

    public Object getJavaObject(String name, Domain classesDomain, CodeBase codeBase) throws Exception {
        return toJavaObject(getObjects().get(name), classesDomain, codeBase);
    }

    public static String getQualifiedName(EPackage domain) {
        if (domain == null) {
            return null;
        }
        String superQName = getQualifiedName(domain.getESuperPackage());
        if (superQName != null) {
            return superQName + PATH_SEPARATOR + domain.getName();
        } else {
            return domain.getName();
        }
    }

    public String getQualifiedName() {
        return getQualifiedName(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Domain)) {
            return false;
        } else {
            return getQualifiedName().equals(((Domain) obj).getQualifiedName());
        }
    }

    @Override
    public int hashCode() {
        return getQualifiedName().hashCode();
    }
}
