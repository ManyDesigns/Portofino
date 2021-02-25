package com.manydesigns.portofino.model;

import org.eclipse.emf.ecore.*;

public class PortofinoPackage {

    public static final EPackage eINSTANCE;

    static {
        eINSTANCE = EcoreFactory.eINSTANCE.createEPackage();
        eINSTANCE.setName("portofino");
    }

    public static EClassifier ensureType(Class<?> javaType) {
        return EcorePackage.eINSTANCE.getEClassifiers().stream().filter(
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

}
