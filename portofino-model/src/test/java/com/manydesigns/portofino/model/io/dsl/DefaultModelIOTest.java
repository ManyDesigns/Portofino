package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.language.ModelLexer;
import com.manydesigns.portofino.model.language.ModelParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.eclipse.emf.ecore.EPackage;
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
        assertEquals(model.getDomains().size(), 1);
        FileObject outDir = VFS.getManager().resolveFile("ram://portofino/test-model-1");
        io = new DefaultModelIO(outDir);
        io.save(model);
        FileObject domainFile = outDir.resolveFile("testDomain1/testDomain1.domain");
        assertFalse(domainFile.exists());
        FileObject entityFile = outDir.resolveFile("testDomain1/Person.entity");
        assertTrue(entityFile.exists());
        String contents = IOUtils.toString(entityFile.getContent().getInputStream(), StandardCharsets.UTF_8);
        assertEquals(contents, "entity Person {\n" +
                "\tid {\n" +
                "\t\tname\n" +
                "\t}\n" +
                "\t@Email\n" +
                "\temail\n" +
                "\tage: int!\n" +
                "\tregistrationDate: EDate\n" +
                "\tmother\n" +
                "\tfather\n" +
                "\tmother_rel --> Person(name=mother)\n" +
                "\tfather_rel --> Person(name=father)\n" +
                "}");
        Model model2 = io.load();
        assertEquals(model2.getDomains().size(), 1);
    }

}
