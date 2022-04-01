package com.manydesigns.portofino.model.service;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.io.ModelIO;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ModelResource extends ResourceImpl {

    protected Model model;
    protected final ModelIO io;

    public ModelResource(ModelIO io) {
        this.io = io;
    }

    public ModelResource(Model model, ModelIO io) {
        this(io);
        this.model = model;
        this.isLoaded = true;
    }

    @Override
    public void load(Map<?, ?> options) throws IOException {
        if (!isLoaded) {
            model = io.load();
            model.getDomains().forEach(d -> getContents().add(d));
            Map<?, ?> response = options == null ? null : (Map<?, ?>) options.get(URIConverter.OPTION_RESPONSE);
            if (response == null) {
                response = new HashMap<>();
                handleLoadResponse(response, options);
            }
        }
    }

    @Override
    public void save(Map<?, ?> options) throws IOException {
        Map<?, ?> response = options == null ? null : (Map<?, ?>)options.get(URIConverter.OPTION_RESPONSE);
        if (response == null) {
            response = new HashMap<>();
        }
        try {
            io.save(model);
        } finally {
            handleSaveResponse(response, options);
        }
    }

}
