package com.manydesigns.portofino.model.service;

import com.manydesigns.portofino.model.Domain;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.vfs2.VFS;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.testng.annotations.Test;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.testng.Assert.*;

public class ModelServiceTest {

    private String test1;
    private ModelServiceTest test2;

    @Test
    public void testAddBuiltInClass() throws IOException, IntrospectionException {
        ModelService modelService = new ModelService(
                VFS.getManager().resolveFile("ram://test"), new PropertiesConfiguration(), null);
        EClass eClass = modelService.addBuiltInClass(ModelServiceTest.class);
        assertEquals(modelService.getModel().getDomains().size(), 1);
        assertEquals(eClass.getName(), ModelServiceTest.class.getSimpleName());
        assertEquals(eClass.getInstanceClass(), ModelServiceTest.class);
        assertEquals(eClass, modelService.getModel().findClass(ModelServiceTest.class));
    }

    @Test
    public void testPutObject() throws Exception {
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
        test1 = "foo";
        test2 = new ModelServiceTest();
        test2.test1 = "bar";
        modelService.getModel().putObject(domain, "object", this);
        assertEquals(domain.getObjects().size(), 1);
        ModelServiceTest object = (ModelServiceTest) domain.getJavaObject("object");
        assertNotNull(object);
        assertEquals(test1, object.test1);
        assertNotNull(object.test2);
        assertEquals(test2.test1, object.test2.test1);
    }

    public String getTest1() {
        return test1;
    }

    public void setTest1(String test1) {
        this.test1 = test1;
    }

    public ModelServiceTest getTest2() {
        return test2;
    }

    public void setTest2(ModelServiceTest test2) {
        this.test2 = test2;
    }
}
