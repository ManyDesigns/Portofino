package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.JdbcConnectionProvider;
import com.manydesigns.portofino.model.database.JndiConnectionProvider;
import com.manydesigns.portofino.model.database.Schema;
import com.manydesigns.portofino.model.language.ModelBaseVisitor;
import com.manydesigns.portofino.model.language.ModelParser;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DatabasePersistenceVisitor extends ModelBaseVisitor<Void> {

    protected final Model model;
    protected Database currentDatabase;

    public DatabasePersistenceVisitor(Model model) {
        this.model = model;
    }

    @Override
    public Void visitDatabase(ModelParser.DatabaseContext ctx) {
        currentDatabase = new Database();
        currentDatabase.setDatabaseName(ctx.name.getText());
        boolean jdbc = true;
        Optional<String> connectionType = getConnectionProperty(ctx, "type");
        if(connectionType.isPresent()) {
            if(connectionType.get().equalsIgnoreCase("JDBC")) {
                jdbc = true;
            } else if(connectionType.get().equalsIgnoreCase("JNDI")) {
                jdbc = false;
            } else {
                throw new RuntimeException("Unrecognized connection type: " + connectionType);
            }
        }
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
        super.visitDatabase(ctx);
        model.getDatabases().add(currentDatabase);
        return null;
    }

    @Override
    public Void visitSchema(ModelParser.SchemaContext ctx) {
        Schema schema = new Schema(currentDatabase);
        schema.setSchemaName(ctx.name.getText());
        if(ctx.physicalName != null) {
            schema.setActualSchemaName(getText(ctx.physicalName));
        }
        return null;
    }

    private String getText(Token token) {
        if(token.getType() == ModelParser.STRING) {
            return token.getText().substring(1, token.getText().length() - 2);
        } else {
            return token.getText();
        }
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
}
