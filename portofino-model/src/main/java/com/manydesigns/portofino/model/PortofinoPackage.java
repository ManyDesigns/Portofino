package com.manydesigns.portofino.model;

import org.eclipse.emf.ecore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortofinoPackage {

    public static final EPackage eINSTANCE;
    private static final Logger logger = LoggerFactory.getLogger(PortofinoPackage.class);

    static {
        eINSTANCE = EcoreFactory.eINSTANCE.createEPackage();
        eINSTANCE.setName("portofino");
    }

    public static EDataType ensureType(Class<?> javaType) {
        return (EDataType) EcorePackage.eINSTANCE.getEClassifiers().stream().filter(
                c -> c.getInstanceClass() == javaType).findFirst().orElseGet(() ->
                eINSTANCE.getEClassifiers().stream().filter(
                        c -> c.getInstanceClass() == javaType).findFirst().orElseGet(() -> {
                            EDataType type = EcoreFactory.eINSTANCE.createEDataType();
                            type.setName(javaType.getName());
                            type.setInstanceClass(javaType);
                            eINSTANCE.getEClassifiers().add(type);
                            return type;
                        }));
    }

    public static EDataType ensureType(String className) {
        return (EDataType) EcorePackage.eINSTANCE.getEClassifiers().stream().filter(
                c -> c.getName().equals(className) || className.equals(c.getInstanceClassName()))
                .findFirst().orElseGet(() ->
                eINSTANCE.getEClassifiers().stream().filter(
                        c -> c.getName().equals(className) || className.equals(c.getInstanceClassName())).findFirst().orElseGet(() -> {
                    EDataType type = EcoreFactory.eINSTANCE.createEDataType();
                    type.setName(className);
                    try {
                        type.setInstanceClass(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        logger.error("Could not load type class " + className, e);
                    }
                    eINSTANCE.getEClassifiers().add(type);
                    return type;
                }));
    }

}
