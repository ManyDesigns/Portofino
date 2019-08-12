package com.manydesigns.elements.annotations;

import com.manydesigns.elements.options.DisplayMode;
import com.manydesigns.elements.options.SearchDisplayMode;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

@Test
public class AnnotationFactoryTest {

    public void testSelect() {
        Select select;

        //Defaults
        select = new AnnotationFactory().make(Select.class);
        assertEquals(select.displayMode(), DisplayMode.DROPDOWN);
        assertEquals(select.searchDisplayMode(), SearchDisplayMode.DROPDOWN);
        assertEquals(select.labels(), new String[0]);
        assertEquals(select.values(), new String[0]);
        assertTrue(select.nullOption());

        //User values
        Map<String, Object> values = new HashMap<>();
        values.put("displayMode", DisplayMode.AUTOCOMPLETE);
        values.put("labels", new String[] { "test", "labels" });
        values.put("nullOption", false);
        select = new AnnotationFactory().make(Select.class, values);
        assertEquals(select.displayMode(), DisplayMode.AUTOCOMPLETE);
        assertEquals(select.searchDisplayMode(), SearchDisplayMode.DROPDOWN);
        assertEquals(select.labels(), new String[] { "test", "labels" });
        assertEquals(select.values(), new String[0]);
        assertFalse(select.nullOption());
    }

    public void testMissingValue() {
        try {
            new AnnotationFactory().make(ShortName.class);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            //Ok
        }
    }

}
