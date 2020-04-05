package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.java.JavaTypesDomain;
import com.manydesigns.portofino.model.language.ModelLexer;
import com.manydesigns.portofino.model.language.ModelParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertEquals;

public class DefaultModelIOTest {

    @Test
    public void testSimpleDomain() throws IOException {
        try(InputStream inputStream = getClass().getResourceAsStream("/testDomain1.domain")) {
            ModelLexer lexer = new ModelLexer(CharStreams.fromStream(inputStream));
            ModelParser parser = new ModelParser(new CommonTokenStream(lexer));
            ModelParser.StandaloneDomainContext parseTree = parser.standaloneDomain();
            assertEquals(parser.getNumberOfSyntaxErrors(), 0);
            Domain domain = new EntityModelVisitor(new JavaTypesDomain()).visit(parseTree);
            assertEquals(domain.getEntities().size(), 1);
        }
    }

}
