package com.manydesigns.portofino.model.service;

import com.manydesigns.portofino.model.Domain;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.vfs2.VFS;
import org.eclipse.emf.ecore.EClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

public class ModelServiceTest {

    @Test
    public void testAddBuiltInClass() throws IOException {
        ModelService modelService = new ModelService(
                VFS.getManager().resolveFile("ram://test"), new PropertiesConfiguration(), null);
        EClass eClass = modelService.addBuiltInClass(ModelServiceTest.class);
        assertEquals(modelService.getModel().getDomains().size(), 1);
        assertEquals(eClass.getName(), ModelServiceTest.class.getSimpleName());
        assertEquals(eClass.getInstanceClass(), ModelServiceTest.class);
        assertEquals(eClass, modelService.getModel().findClass(ModelServiceTest.class));
    }

    @Test
    public void testPutObject() throws IOException {
        ModelService modelService = new ModelService(
                VFS.getManager().resolveFile("ram://test"), new PropertiesConfiguration(), null);
        Domain domain = modelService.getModel().ensureDomain("domain");
        try {
            modelService.getModel().putObject(domain, "object", this);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            //OK
        }
        modelService.addBuiltInClass(ModelServiceTest.class);
        modelService.getModel().putObject(domain, "object", this);
        assertEquals(domain.getObjects().size(), 1);
        assertNotNull(domain.getObjects().get("object"));
    }
}
