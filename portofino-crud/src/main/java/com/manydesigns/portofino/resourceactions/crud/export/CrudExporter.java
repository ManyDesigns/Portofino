package com.manydesigns.portofino.resourceactions.crud.export;

import com.manydesigns.portofino.resourceactions.crud.AbstractCrudAction;
import org.springframework.http.MediaType;

import jakarta.ws.rs.core.Response;

public interface CrudExporter {

    boolean supports(MediaType mediaType);
    Response.ResponseBuilder exportObject(AbstractCrudAction<?> action);
    Response.ResponseBuilder exportSearchResults(AbstractCrudAction<?> action);

}
