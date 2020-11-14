package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.JdbcConnectionProvider;
import com.manydesigns.portofino.model.database.JndiConnectionProvider;
import com.manydesigns.portofino.model.database.Schema;
import com.manydesigns.portofino.model.language.ModelBaseVisitor;
import com.manydesigns.portofino.model.language.ModelParser;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DatabasePersistenceVisitor extends ModelBaseVisitor<ModelObject> {

    protected final Model model;
    protected Database currentDatabase;

    public DatabasePersistenceVisitor(Model model) {
        this.model = model;
    }

    @Override
    public Database visitDatabase(ModelParser.DatabaseContext ctx) {
        String name = ctx.name.getText();
        boolean jdbc = true;
        Optional<String> connectionType = getConnectionProperty(ctx, "type");
        if(connectionType.isPresent()) {
            if(connectionType.get().equalsIgnoreCase("JDBC")) {
                jdbc = true;
            } else if(connectionType.get().equalsIgnoreCase("JNDI")) {
                jdbc = false;
            } else {
                throw new RuntimeException("Unrecognized connection type: " + connectionType); //TODO
            }
        }
        Optional<Domain> domain = model.getDomains().stream().filter(d -> d.getName().equals(name)).findFirst();
        if(domain.isPresent()) {
            currentDatabase = new Database(domain.get());
        } else {
            currentDatabase = new Database();
            currentDatabase.setDatabaseName(name);
            model.getDomains().add(currentDatabase.getDomain());
        }
        model.getDatabases().add(currentDatabase);
        if(jdbc) {
            JdbcConnectionProvider cp = new JdbcConnectionProvider();
            Optional<String> property = getConnectionProperty(ctx, "url");
            property.ifPresent(cp::setUrl);
            property = getConnectionProperty(ctx, "driver");
            property.ifPresent(cp::setDriver);
            property = getConnectionProperty(ctx, "username");
            property.ifPresent(cp::setUsername);
            property = getConnectionProperty(ctx, "password");
            property.ifPresent(cp::setPassword);
            currentDatabase.setConnectionProvider(cp);
        } else {
            JndiConnectionProvider cp = new JndiConnectionProvider();
            Optional<String> property = getConnectionProperty(ctx, "jndiName");
            property.ifPresent(cp::setJndiResource);
            currentDatabase.setConnectionProvider(cp);
        }
        try {
            super.visitDatabase(ctx);
            return currentDatabase;
        } finally {
            currentDatabase = null;
        }
    }

    @Override
    public Schema visitSchema(ModelParser.SchemaContext ctx) {
        Schema schema = new Schema(currentDatabase);
        schema.setSchemaName(ctx.name.getText());
        if(ctx.physicalName != null) {
            schema.setActualSchemaName(getText(ctx.physicalName));
        }
        return schema;
    }

    @NotNull
    protected Optional<String> getConnectionProperty(ModelParser.DatabaseContext ctx, String name) {
        return ctx.connectionProperty().stream()
                .filter(p -> p.name.getText().equals(name))
                .map(p -> getText(p.value))
                .findFirst();
    }

    private String getText(ModelParser.LiteralContext value) {
        String text = value.getText();
        if(value.STRING() != null) {
            text = text.substring(1, text.length() - 2);
        }
        return text;
    }

    private String getText(Token token) {
        if(token.getType() == ModelParser.STRING) {
            return token.getText().substring(1, token.getText().length() - 2);
        } else {
            return token.getText();
        }
    }
}
