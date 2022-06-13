package com.manydesigns.portofino.model.service;

import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.config.ConfigurationSource;
import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.PortofinoPackage;
import com.manydesigns.portofino.model.io.ModelIO;
import com.manydesigns.portofino.model.io.dsl.DefaultModelIO;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.emf.ecore.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ModelService {

    protected Model model = new Model();
    public final BehaviorSubject<EventType> modelEvents = BehaviorSubject.create();
    public static final String APP_MODEL_DIRECTORY = "portofino-model";
    protected final FileObject applicationDirectory;
    protected final ConfigurationSource configuration;
    protected final List<Domain> transientDomains = new CopyOnWriteArrayList<>();
    protected final CodeBase codeBase;
    private static final Logger logger = LoggerFactory.getLogger(ModelService.class);

    public ModelService(
            FileObject applicationDirectory, ConfigurationSource configuration, CodeBase codeBase) {
        this.applicationDirectory = applicationDirectory;
        this.configuration = configuration;
        this.codeBase = codeBase;
    }

    public enum EventType {
        LOADED, SAVED
    }

    public Model getModel() {
        return model;
    }

    public synchronized Model loadModel(ModelIO modelIO) throws IOException {
        Model loaded = modelIO.load();
        if(loaded != null) {
            model = loaded;
            modelEvents.onNext(EventType.LOADED);
            return model;
        } else {
            return null;
        }
    }

    public void loadModel() throws IOException {
        loadModel(getDefaultModelIO());
    }

    public FileObject getModelDirectory() throws FileSystemException {
        return applicationDirectory.resolveFile(APP_MODEL_DIRECTORY);
    }

    public FileObject getApplicationDirectory() {
        return applicationDirectory;
    }

    public synchronized void saveModel() throws IOException, ConfigurationException {
        Model toSave = new Model();
        toSave.getDomains().addAll(model.getDomains());
        toSave.getDomains().removeAll(transientDomains);
        getDefaultModelIO().save(toSave);
        if (configuration.isWritable()) {
            configuration.save();
        }
        modelEvents.onNext(EventType.SAVED);
    }

    @NotNull
    public DefaultModelIO getDefaultModelIO() throws FileSystemException {
        return new DefaultModelIO(getModelDirectory(), transientDomains);
    }

    public synchronized void saveDomain(Domain domain) throws IOException {
        if (isTransient(domain)) {
            throw new UnsupportedOperationException(domain.getQualifiedName() + " is transient");
        }
        getDefaultModelIO().save(domain);
    }

    public synchronized void saveEntity(EClass entity) throws IOException {
        EObject pkg = entity.eContainer();
        if (!(pkg instanceof Domain)) {
            throw new UnsupportedOperationException(entity + " does not belong to a domain");
        }
        Domain domain = (Domain) pkg;
        if (isTransient(domain)) {
            throw new UnsupportedOperationException(domain.getQualifiedName() + " is transient");
        }
        getDefaultModelIO().save(entity);
    }

    public boolean isTransient(Domain domain) {
        return transientDomains.contains(domain) ||
                (domain.eContainer() instanceof Domain && isTransient((Domain) domain.eContainer()));
    }

    public Domain ensureTopLevelDomain(String name, boolean persist) {
        Optional<Domain> any = model.getDomains().stream()
                .filter(d -> d.getName().equals(name)).findAny();
        Domain domain;
        if (any.isPresent()) {
            logger.debug("Not adding domain " + name + " because it's already present");
            Domain existing = any.get();
            if (persist) {
                // TODO merge domains?
                transientDomains.remove(existing);
            }
            return existing;
        } else {
            domain = new Domain(name);
            model.getDomains().add(domain);
        }
        if (!persist) {
            transientDomains.add(domain);
        }
        return domain;
    }

    public EClassifier addBuiltInClass(Class<?> javaClass) throws IntrospectionException {
        Domain pkg = ensureDomain(javaClass);
        String className = javaClass.getSimpleName();
        EClassifier existing = pkg.getEClassifier(className);
        if (existing != null) {
            // TODO check they model the same class?
            return existing;
        }
        if (javaClass.isEnum()) {
            return addEnum(javaClass, pkg, className);
        } else {
            return addClass(javaClass, pkg, className);
        }
    }

    protected EEnum addEnum(Class<?> javaClass, Domain pkg, String className) {
        EEnum eEnum = EcoreFactory.eINSTANCE.createEEnum();
        eEnum.setName(className);
        for (Object constant : javaClass.getEnumConstants()) {
            EEnumLiteral literal = EcoreFactory.eINSTANCE.createEEnumLiteral();
            literal.setLiteral(((Enum<?>) constant).name());
            eEnum.getELiterals().add(literal);
        }
        pkg.getEClassifiers().add(eEnum);
        return eEnum;
    }

    @NotNull
    protected EClass addClass(Class<?> javaClass, Domain pkg, String className) throws IntrospectionException {
        EClass eClass = EcoreFactory.eINSTANCE.createEClass();
        eClass.setName(className);
        pkg.getEClassifiers().add(eClass); // We must add it early to break recursion when properties refer to itself
        for (PropertyDescriptor prop : Domain.getPersistentProperties(javaClass)) {
            Class<?> type = prop.getPropertyType();
            if (Iterable.class.isAssignableFrom(type)) {
                Type returnType = prop.getReadMethod().getGenericReturnType();
                EReference reference = EcoreFactory.eINSTANCE.createEReference();
                if (returnType instanceof ParameterizedType) {
                    Type typeArgument = ((ParameterizedType) returnType).getActualTypeArguments()[0];
                    if (typeArgument instanceof Class) {
                        reference.setEType(addBuiltInClass((Class<?>) typeArgument));
                    }
                }
                reference.setName(prop.getName());
                reference.setUpperBound(-1);
                eClass.getEStructuralFeatures().add(reference);
            } else {
                Optional<EClassifier> builtin =
                        EcorePackage.eINSTANCE.getEClassifiers().stream().filter(t -> type.equals(t.getInstanceClass())).findFirst();
                EStructuralFeature feature = builtin.map(t -> {
                    EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
                    attribute.setEType(t);
                    return (EStructuralFeature) attribute;
                }).orElseGet(() -> {
                    try {
                        EReference reference = EcoreFactory.eINSTANCE.createEReference();
                        reference.setEType(addBuiltInClass(type));
                        return reference;
                    } catch (IntrospectionException e) {
                        throw new RuntimeException(e);
                    }
                });
                feature.setName(prop.getName());
                eClass.getEStructuralFeatures().add(feature);
            }
        }
        return eClass;
    }

    public Object getJavaObject(Domain domain, String name)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException,
            IntrospectionException, NoSuchFieldException, IOException, ClassNotFoundException {
        EObject eObject = domain.getObjects().get(name);
        return toJavaObject(eObject);
    }

    @Nullable
    public Object toJavaObject(EObject eObject) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IntrospectionException, NoSuchFieldException {
        return Domain.toJavaObject(eObject, getClassesDomain(), codeBase);
    }

    public EClassifier ensureType(Class<?> type) {
        try {
            Domain pkg = getClassesDomain().resolveDomain(type.getPackageName());
            String className = type.getSimpleName();
            EClassifier eClassifier = pkg.getEClassifier(className);
            if (eClassifier != null) {
                return eClassifier;
            }
        } catch (IllegalArgumentException e) {
            logger.debug("Domain not found", e);
        }
        return PortofinoPackage.ensureType(type);
    }

    public Domain ensureDomain(Class<?> javaClass) {
        String[] packageName = javaClass.getPackageName().split("[.]");
        Domain pkg = getClassesDomain();
        for (String s : packageName) {
            pkg = Domain.ensureDomain(s, pkg.getSubdomains());
        }
        return pkg;
    }

    public Domain getClassesDomain() {
        return getPortofinoDomain().ensureDomain("classes");
    }

    public Domain getPortofinoDomain() {
        return ensureTopLevelDomain("portofino", false);
    }

}
