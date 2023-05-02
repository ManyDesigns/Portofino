package com.manydesigns.portofino.resourceactions.crud.export;

import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.util.FormUtil;
import com.manydesigns.portofino.resourceactions.crud.AbstractCrudAction;
import org.json.JSONStringer;
import org.springframework.http.MediaType;

import javax.ws.rs.core.Response;

public class JSONExporter implements CrudExporter {

    @Override
    public boolean supports(MediaType mediaType) {
        return mediaType.includes(MediaType.APPLICATION_JSON);
    }

    @Override
    public Response.ResponseBuilder exportObject(AbstractCrudAction<?> action) {
        String jsonText = FormUtil.writeToJson(action.getForm());
        return Response.ok(jsonText).type(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).encoding("UTF-8");
    }

    @Override
    public Response.ResponseBuilder exportSearchResults(AbstractCrudAction<?> action) {
        final long totalRecords = action.getTotalSearchRecords();

        JSONStringer js = new JSONStringer();
        js.object();
        if (totalRecords >= 0) {
            js.key("totalRecords").value(totalRecords);
        }
        js.key("startIndex").value(action.getFirstResult() == null ? 0 : action.getFirstResult());
        js.key("records").array();
        for (TableForm.Row row : action.getTableForm().getRows()) {
            js.object()
                    .key("__rowKey")
                    .value(row.getKey());
            FormUtil.fieldsToJson(js, row);
            js.endObject();
        }
        js.endArray();
        js.endObject();
        String jsonText = js.toString();
        Response.ResponseBuilder builder =
                Response.ok(jsonText).type(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).encoding("UTF-8");
        Integer rowsPerPage = action.getCrudConfiguration().getRowsPerPage();
        if(rowsPerPage != null && totalRecords > rowsPerPage) {
            int firstResult = action.getFirstResult() != null ? action.getFirstResult() : 1;
            int currentPage = firstResult / rowsPerPage;
            int lastPage = (int) (totalRecords / rowsPerPage);
            if(totalRecords % rowsPerPage == 0) {
                lastPage--;
            }
            StringBuilder sb = new StringBuilder();
            if(currentPage > 0) {
                sb.append("<").append(action.getLinkToPage(0)).append(">; rel=\"first\", ");
                sb.append("<").append(action.getLinkToPage(currentPage - 1)).append(">; rel=\"prev\"");
            }
            if(currentPage != lastPage) {
                if(currentPage > 0) {
                    sb.append(", ");
                }
                sb.append("<").append(action.getLinkToPage(currentPage + 1)).append(">; rel=\"next\", ");
                sb.append("<").append(action.getLinkToPage(lastPage)).append(">; rel=\"last\"");
            }
            builder.header("Link", sb.toString());
        }
        return builder;
    }
}
