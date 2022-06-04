package com.manydesigns.portofino.model.service;

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.code.JavaCodeBase;
import com.manydesigns.portofino.config.ConfigurationSource;
import com.manydesigns.portofino.model.Domain;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.testng.annotations.Test;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

public class ModelServiceTest {

    private String test1;
    private double test2;
    private ModelServiceTest test3;
    private PortofinoProperties otherClass;
    private List<ModelServiceTest> listProperty;
    private List<ModelServiceTest> nullListProperty;

    @Test
    public void testAddBuiltInClass() throws IOException, IntrospectionException {
        FileObject applicationDirectory = VFS.getManager().resolveFile("ram://test");
        ModelService modelService = new ModelService(
                applicationDirectory, new ConfigurationSource(new PropertiesConfiguration(), null),
                new JavaCodeBase(applicationDirectory));
        EClassifier eClass = modelService.addBuiltInClass(ModelServiceTest.class);
        assertTrue(eClass instanceof EClass);
        assertEquals(modelService.getModel().getDomains().size(), 1);
        assertEquals(eClass.getName(), ModelServiceTest.class.getSimpleName());
        assertEquals(eClass, modelService.getClassesDomain().findClass(ModelServiceTest.class));
    }

    @Test
    public void testAddBuiltInEnum() throws IOException, IntrospectionException {
        FileObject applicationDirectory = VFS.getManager().resolveFile("ram://test");
        ModelService modelService = new ModelService(
                applicationDirectory, new ConfigurationSource(new PropertiesConfiguration(), null),
                new JavaCodeBase(applicationDirectory));
        EClassifier eEnum = modelService.addBuiltInClass(ModelService.EventType.class);
        assertTrue(eEnum instanceof EEnum);
        assertEquals(modelService.getModel().getDomains().size(), 1);
        assertEquals(eEnum.getName(), ModelService.EventType.class.getSimpleName());
        assertEquals(eEnum, modelService.getClassesDomain().findClass(ModelService.EventType.class));
        assertEquals(2, ((EEnum) eEnum).getELiterals().size());
        assertEquals(ModelService.EventType.LOADED.name(), ((EEnum) eEnum).getELiterals().get(0).getLiteral());
        assertEquals(ModelService.EventType.SAVED.name(), ((EEnum) eEnum).getELiterals().get(1).getLiteral());
    }

    @Test
    public void testPutObject() throws Exception {
        FileObject applicationDirectory = VFS.getManager().resolveFile("ram://test");
        ModelService modelService = new ModelService(
                applicationDirectory, new ConfigurationSource(new PropertiesConfiguration(), null),
                new JavaCodeBase(applicationDirectory));
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
        listProperty = Collections.singletonList(test3);
        domain.putObject( "object", this, modelService.getClassesDomain());
        assertEquals(domain.getObjects().size(), 1);
        ModelServiceTest object = (ModelServiceTest) modelService.getJavaObject(domain, "object");
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

    public PortofinoProperties getOtherClass() {
        return otherClass;
    }

    public void setOtherClass(PortofinoProperties otherClass) {
        this.otherClass = otherClass;
    }

    public List<ModelServiceTest> getListProperty() {
        return listProperty;
    }

    public void setListProperty(List<ModelServiceTest> listProperty) {
        this.listProperty = listProperty;
    }
}
