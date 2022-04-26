package com.manydesigns.portofino.model.service;

import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.PortofinoPackage;
import com.manydesigns.portofino.model.io.ModelIO;
import com.manydesigns.portofino.model.io.dsl.DefaultModelIO;
import io.reactivex.subjects.PublishSubject;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.emf.ecore.*;
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
    public final PublishSubject<EventType> modelEvents = PublishSubject.create();
    public static final String APP_MODEL_DIRECTORY = "portofino-model";
    protected final FileObject applicationDirectory;
    protected final Configuration configuration;
    protected final FileBasedConfigurationBuilder<PropertiesConfiguration> configurationFile;
    protected final List<Domain> transientDomains = new CopyOnWriteArrayList<>();
    protected final CodeBase codeBase;
    private static final Logger logger = LoggerFactory.getLogger(ModelService.class);

    public ModelService(
            FileObject applicationDirectory, Configuration configuration,
            FileBasedConfigurationBuilder<PropertiesConfiguration> configurationFile, CodeBase codeBase) {
        this.applicationDirectory = applicationDirectory;
        this.configuration = configuration;
        this.configurationFile = configurationFile;
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
            loaded.getDomains().removeIf(d -> transientDomains.stream().anyMatch(t -> t.getName().equals(d.getName())));
            loaded.getDomains().addAll(transientDomains);
            loaded.init();
            model = loaded;
            modelEvents.onNext(EventType.LOADED);
            return model;
        } else {
            return null;
        }
    }

    public synchronized void loadModel() throws IOException {
        loadModel(new DefaultModelIO(getModelDirectory()));
    }

    public FileObject getModelDirectory() throws FileSystemException {
        return applicationDirectory.resolveFile(APP_MODEL_DIRECTORY);
    }

    public FileObject getApplicationDirectory() {
        return applicationDirectory;
    }

    public synchronized void saveModel() throws IOException, ConfigurationException {
        model.init();
        Model toSave = new Model();
        toSave.getDomains().addAll(model.getDomains());
        toSave.getDomains().removeAll(transientDomains);
        new DefaultModelIO(getModelDirectory()).save(toSave);
        if (configurationFile != null) {
            configurationFile.save();
            logger.info("Saved configuration file {}", configurationFile.getFileHandler().getFile().getAbsolutePath());
        }
        modelEvents.onNext(EventType.SAVED);
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

    public EClass addBuiltInClass(Class<?> javaClass) throws IntrospectionException {
        Domain pkg = ensureDomain(javaClass);
        String className = javaClass.getSimpleName();
        EClassifier existing = pkg.getEClassifier(className);
        if (existing != null) {
            // TODO check they model the same class?
            return (EClass) existing;
        }
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
