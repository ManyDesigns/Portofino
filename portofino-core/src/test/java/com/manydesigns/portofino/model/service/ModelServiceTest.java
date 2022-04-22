package com.manydesigns.portofino.model.service;

import com.manydesigns.portofino.model.Domain;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.vfs2.VFS;
import org.eclipse.emf.ecore.EClass;
import org.testng.annotations.Test;

import java.beans.IntrospectionException;
import java.io.IOException;

import static org.testng.Assert.*;

public class ModelServiceTest {

    private String test1;
    private double test2;
    private ModelServiceTest test3;

    @Test
    public void testAddBuiltInClass() throws IOException, IntrospectionException {
        ModelService modelService = new ModelService(
                VFS.getManager().resolveFile("ram://test"), new PropertiesConfiguration(), null);
        EClass eClass = modelService.addBuiltInClass(ModelServiceTest.class);
        assertEquals(modelService.getModel().getDomains().size(), 1);
        assertEquals(eClass.getName(), ModelServiceTest.class.getSimpleName());
        assertEquals(eClass.getInstanceClass(), ModelServiceTest.class);
        assertEquals(eClass, modelService.getClassesDomain().findClass(ModelServiceTest.class));
    }

    @Test
    public void testPutObject() throws Exception {
        ModelService modelService = new ModelService(
                VFS.getManager().resolveFile("ram://test"), new PropertiesConfiguration(), null);
        Domain domain = modelService.getModel().ensureDomain("domain");
        try {
            domain.putObject( "object", this, modelService.getClassesDomain());
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            //OK
        }
        modelService.addBuiltInClass(ModelServiceTest.class);
        test1 = "foo";
        test2 = 3.14;
        test3 = new ModelServiceTest();
        test3.test1 = "bar";
        domain.putObject( "object", this, modelService.getClassesDomain());
        assertEquals(domain.getObjects().size(), 1);
        ModelServiceTest object = (ModelServiceTest) domain.getJavaObject("object");
        assertNotNull(object);
        assertNotEquals(this, object);
        assertEquals(test1, object.test1);
        assertEquals(test2, object.test2);
        assertNotNull(object.test3);
        assertEquals(test3.test1, object.test3.test1);
    }

    public String getTest1() {
        return test1;
    }

    public void setTest1(String test1) {
        this.test1 = test1;
    }

    public double getTest2() {
        return test2;
    }

    public void setTest2(double test2) {
        this.test2 = test2;
    }

    public ModelServiceTest getTest3() {
        return test3;
    }

    public void setTest3(ModelServiceTest test3) {
        this.test3 = test3;
    }
}
