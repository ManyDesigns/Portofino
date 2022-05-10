package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.PortofinoPackage;
import com.manydesigns.portofino.model.language.ModelLexer;
import com.manydesigns.portofino.model.language.ModelParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.testng.Assert.*;

public class EMFTest {

    @Test
    public void testSimpleModel() throws IOException {
        ElementsThreadLocals.setupDefaultElementsContext();
        DefaultModelIO io = new DefaultModelIO(VFS.getManager().resolveFile("res:test-model-1"));
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
        assertNotNull(resource2.getContents().stream().filter(
                o -> ((EPackage) o).getName().equals("testDomain1")).findFirst().orElse(null));
    }

    @Test
    public void testSimpleModelWithObjects() throws IOException {
        ElementsThreadLocals.setupDefaultElementsContext();
        DefaultModelIO io = new DefaultModelIO(VFS.getManager().resolveFile("res:test-model-1"));
        Model model = io.load();
        assertEquals(model.getDomains().size(), 2);
        model.addObject(model.getDomain("testDomain1"), "someObject", EcoreFactory.eINSTANCE.createEObject());
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
        assertNotNull(resource2.getContents().stream().filter(
                o -> ((EPackage) o).getName().equals("testDomain1")).findFirst().orElse(null));
    }

}
