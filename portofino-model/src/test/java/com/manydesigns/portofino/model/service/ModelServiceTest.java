package com.manydesigns.portofino.model.service;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.VFS;
import org.eclipse.emf.ecore.EClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

public class ModelServiceTest {

    @Test
    public void testAddBuiltInClass() throws IOException {
        ModelService modelService = new ModelService(
                VFS.getManager().resolveFile("ram://test"), new PropertiesConfiguration(), null);
        EClass eClass = modelService.addBuiltInClass(ModelServiceTest.class);
        assertEquals(modelService.getModel().getDomains().size(), 1);
        assertEquals(eClass.getName(), ModelServiceTest.class.getSimpleName());
        assertEquals(eClass.getInstanceClass(), ModelServiceTest.class);
    }
}
