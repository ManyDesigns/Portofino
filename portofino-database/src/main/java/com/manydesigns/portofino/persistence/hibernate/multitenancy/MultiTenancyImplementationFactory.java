package com.manydesigns.portofino.persistence.hibernate.multitenancy;

public interface MultiTenancyImplementationFactory {

    MultiTenancyImplementation make(Class<? extends MultiTenancyImplementation> implClass) throws Exception;

    MultiTenancyImplementationFactory DEFAULT = implClass -> implClass.getConstructor().newInstance();

}
