package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.PortofinoPackage;
import com.manydesigns.portofino.model.io.ModelIO;
import org.apache.commons.vfs2.VFS;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class EMFTest {

    @Test
    public void testSimpleModel() throws IOException {
        ElementsThreadLocals.setupDefaultElementsContext();
        ModelIO io = new ModelIO(VFS.getManager().resolveFile("res:test-model-1"));
        Model model = io.load();
        assertEquals(model.getDomains().size(), 2);
        XMIResource resource = new XMIResourceImpl();
        resource.getContents().addAll(model.getDomains());
        StringWriter writer = new StringWriter();
        resource.save(writer, new HashMap<>());
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(PortofinoPackage.eINSTANCE.getNsURI(), PortofinoPackage.eINSTANCE);
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        Resource resource2 = resourceSet.createResource(URI.createFileURI("test-model-1.xmi"));
        resource2.load(new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8)), new HashMap<>());
        assertEquals(resource2.getContents().size(), 2);
        Optional<EObject> testDomain1 = resource2.getContents().stream()
                .filter(o -> o instanceof EPackage && ((EPackage) o).getName().equals("testDomain1"))
                .findFirst();
        assertTrue(testDomain1.isPresent());
    }

    @Test
    public void testSimpleModelWithObjects() throws IOException {
        ElementsThreadLocals.setupDefaultElementsContext();
        ModelIO io = new ModelIO(VFS.getManager().resolveFile("res:test-model-1"));
        Model model = io.load();
        assertEquals(model.getDomains().size(), 2);
        model.getDomain("testDomain1").putObject("someObject", EcoreFactory.eINSTANCE.createEObject());
        XMIResource resource = new XMIResourceImpl();
        resource.getContents().addAll(model.getDomains());
        StringWriter writer = new StringWriter();
        resource.save(writer, new HashMap<>());
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(PortofinoPackage.eINSTANCE.getNsURI(), PortofinoPackage.eINSTANCE);
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        Resource resource2 = resourceSet.createResource(URI.createFileURI("test-model-1.xmi"));
        resource2.load(new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8)), new HashMap<>());
        assertEquals(resource2.getContents().size(), 2);
        Optional<EObject> testDomain1 = resource2.getContents().stream()
                .filter(o -> o instanceof EPackage && ((EPackage) o).getName().equals("testDomain1"))
                .findFirst();
        assertTrue(testDomain1.isPresent());
    }

}
