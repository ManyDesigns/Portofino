package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.portofino.model.language.ModelLexer;
import com.manydesigns.portofino.model.language.ModelParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class EntityResource extends ResourceImpl {

    public EntityResource(URI uri) {
        super(uri);
    }

    @Override
    protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
        ModelLexer lexer = new ModelLexer(CharStreams.fromStream(inputStream));
        ModelParser parser = new ModelParser(new CommonTokenStream(lexer));
        ModelParser.StandaloneEntityContext parseTree = parser.standaloneEntity();
        if (parser.getNumberOfSyntaxErrors() == 0) {
            getContents().add(new EntityModelVisitor().visit(parseTree));
        } else {
            throw new IOException("Could not parse entity definition " + uri); //TODO properly report errors
        }
    }

    public static class Factory extends ResourceFactoryImpl {
        @Override
        public Resource createResource(URI uri) {
            return new EntityResource(uri);
        }
    }

}
