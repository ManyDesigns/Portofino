package com.manydesigns.portofino.code;

import org.testng.annotations.Test;

import static org.testng.Assert.fail;

public class AggregateCodeBaseTest {

    @Test
    public void testEmpty() throws Exception {
        AggregateCodeBase aggregateCodeBase = new AggregateCodeBase();
        try {
            aggregateCodeBase.loadClass("test");
            fail("Exception expected");
        } catch (ClassNotFoundException e) {}
        aggregateCodeBase.loadClass(AggregateCodeBaseTest.class.getName());
        aggregateCodeBase.clear(true);
        aggregateCodeBase.close();
    }

}
