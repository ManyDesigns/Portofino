package com.manydesigns.portofino.operations;

import org.testng.annotations.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class OperationsTest {

    @Test
    public void testOperations() {
        List<Operation> operations = Operations.getOperations(SomeResource.class);
        assertEquals(operations.size(), 2);
        for(Operation op : operations) {
            verifyOperation(op);
        }
    }

    protected void verifyOperation(Operation operation) {
        switch (operation.getName()) {
            case "get":
                assertEquals(operation.getSignature(), "GET");
                break;
            case "post":
                assertEquals(operation.getSignature(), "POST foo/:bar");
                break;
            default:
                fail("Unexpected operation: " + operation.getName());
        }
    }

}

class SomeResource {

    @GET
    public Object get() {
        return null;
    }

    @POST
    @Path("foo/:bar")
    public String post() {
        return null;
    }

}
