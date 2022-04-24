package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.language.ModelLexer;
import com.manydesigns.portofino.model.language.ModelParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.*;

public class DefaultModelIOTest {

    @Test
    public void testSimpleDomain() throws IOException {
        try(InputStream inputStream = getClass().getResourceAsStream("/test-model-1/testDomain1/testDomain1.domain")) {
            assertNotNull(inputStream);
            ModelLexer lexer = new ModelLexer(CharStreams.fromStream(inputStream));
            ModelParser parser = new ModelParser(new CommonTokenStream(lexer));
            ModelParser.StandaloneDomainContext parseTree = parser.standaloneDomain();
            assertEquals(parser.getNumberOfSyntaxErrors(), 0);
            EPackage domain = new EntityModelBuilderVisitor().visitStandaloneDomain(parseTree);
            assertEquals(domain.getEClassifiers().size(), 1);
        }
    }

    @Test
    public void testSimpleModel() throws IOException {
        ElementsThreadLocals.setupDefaultElementsContext();
        DefaultModelIO io = new DefaultModelIO(VFS.getManager().resolveFile("res:test-model-1"));
        Model model = io.load();
        assertEquals(model.getDomains().size(), 2);
        FileObject outDir = VFS.getManager().resolveFile("ram://portofino/test-model-1");
        io = new DefaultModelIO(outDir);
        io.save(model);
        FileObject domainFile = outDir.resolveFile("testDomain1/testDomain1.domain");
        assertFalse(domainFile.exists());
        FileObject entityFile = outDir.resolveFile("testDomain1/Person.entity");
        assertTrue(entityFile.exists());
        String contents = IOUtils.toString(entityFile.getContent().getInputStream(), StandardCharsets.UTF_8);
        assertEquals(contents.replace(System.lineSeparator(), "\n"),
                "entity Person {\n" +
                "\tid {\n" +
                "\t\tname: string\n" +
                "\t}\n" +
                "\t@Email\n" +
                "\temail: string\n" +
                "\tage: int!\n" +
                "\tregistrationDate: EDate\n" +
                "\tmother: string\n" +
                "\tfather: string\n" +
                "\tmother_rel --> Person(name=mother)\n" +
                "\tfather_rel --> Person(name=father)\n" +
                "}");
        Model model2 = io.load();
        assertEquals(model2.getDomains().size(), 2);
    }

    @Test
    public void testWithObject() throws IOException {
        ElementsThreadLocals.setupDefaultElementsContext();
        DefaultModelIO io = new DefaultModelIO(VFS.getManager().resolveFile("res:test-model-1"));
        Model model = io.load();
        assertEquals(model.getDomains().size(), 2);
        Domain testDomain1 = model.getDomain("testDomain1");
        EClass Person = (EClass) testDomain1.getEClassifier("Person");
        EObject person = EcoreUtil.create(Person);
        person.eSet(Person.getEStructuralFeature("name"), "Alessio");
        testDomain1.getObjects().put("alessio", person);
        FileObject outDir = VFS.getManager().resolveFile("ram://portofino/test-model-1");
        io = new DefaultModelIO(outDir);
        io.save(model);
        FileObject objectFile = outDir.resolveFile("testDomain1/alessio.object");
        assertTrue(objectFile.exists());
        String contents = IOUtils.toString(objectFile.getContent().getInputStream(), StandardCharsets.UTF_8);
        assertEquals(contents.replace(System.lineSeparator(), "\n"),
                "object alessio : Person {\n" +
                        "\tname = \"Alessio\"\n" +
                        "\tage = 0\n" +
                        "}");
        Model model2 = io.load();
        assertEquals(model2.getDomains().size(), 2);
        testDomain1 = model2.getDomain("testDomain1");
        person = testDomain1.getObjects().get("alessio");
        assertNotNull(person);
        Person = person.eClass();
        assertEquals("Person", Person.getName());
        assertEquals("Alessio", person.eGet(Person.getEStructuralFeature("name")));
        assertEquals(0, person.eGet(Person.getEStructuralFeature("age")));
    }

    @Test
    public void testModelWithErrors() throws IOException {
        ElementsThreadLocals.setupDefaultElementsContext();
        DefaultModelIO io = new DefaultModelIO(VFS.getManager().resolveFile("res:test-model-1"));
        Model model = io.load();
        assertEquals(model.getDomains().size(), 2);
        assertEquals(model.getIssues().size(), 4);
    }

}
