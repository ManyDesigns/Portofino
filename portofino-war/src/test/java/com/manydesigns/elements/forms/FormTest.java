package com.manydesigns.elements.forms;

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.fields.SelectBean1;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class FormTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    private Form form;
    private Form formWithPrefix;
    private Form formWithFieldNames;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        FormBuilder builder =
                new FormBuilder(SelectBean1.class);

        form = builder.build();

        builder.configPrefix("prova.");
        formWithPrefix = builder.build();

        builder.configPrefix(null);
        builder.configFields("myUrl", "myValue");
        formWithFieldNames = builder.build();
    }

    public void testNullPrefix() {
        assertNull(form.getId());
        String text = elementToString(form);
        assertEquals("<fieldset class=\"nolegend\">" +
                "<table class=\"details\"><tr><th>" +
                "<label for=\"myValue\" class=\"field\">My value:</label>" +
                "</th><td><input type=\"text\" class=\"text\" id=\"myValue\" name=\"myValue\">" +
                "</input></td></tr><tr><th>" +
                "<label for=\"myLabel\" class=\"field\">My label:</label>" +
                "</th><td><input type=\"text\" class=\"text\" id=\"myLabel\" name=\"myLabel\">" +
                "</input></td></tr><tr><th>" +
                "<label for=\"myUrl\" class=\"field\">My url:</label></th><td>" +
                "<input type=\"text\" class=\"text\" id=\"myUrl\" name=\"myUrl\">" +
                "</input></td></tr></table></fieldset>", text);
    }

    public void testWithPrefix() {
        assertNull(formWithPrefix.getId());
        String text = elementToString(formWithPrefix);
        assertEquals("<fieldset class=\"nolegend\">" +
                "<table class=\"details\"><tr><th>" +
                "<label for=\"prova.myValue\" class=\"field\">My value:</label>" +
                "</th><td><input type=\"text\" class=\"text\" id=\"prova.myValue\" name=\"prova.myValue\">" +
                "</input></td></tr><tr><th>" +
                "<label for=\"prova.myLabel\" class=\"field\">My label:</label>" +
                "</th><td><input type=\"text\" class=\"text\" id=\"prova.myLabel\" name=\"prova.myLabel\">" +
                "</input></td></tr><tr><th>" +
                "<label for=\"prova.myUrl\" class=\"field\">My url:</label></th><td>" +
                "<input type=\"text\" class=\"text\" id=\"prova.myUrl\" name=\"prova.myUrl\">" +
                "</input></td></tr></table></fieldset>", text);
    }

    public void testWithFieldNames() {
        assertNull(formWithFieldNames.getId());
        String text = elementToString(formWithFieldNames);
        assertEquals("<fieldset class=\"nolegend\">" +
                "<table class=\"details\">" +
                "<tr><th>" +
                "<label for=\"myUrl\" class=\"field\">My url:</label></th><td>" +
                "<input type=\"text\" class=\"text\" id=\"myUrl\" name=\"myUrl\">" +
                "</input></td></tr>" +
                "<tr><th>" +
                "<label for=\"myValue\" class=\"field\">My value:</label>" +
                "</th><td><input type=\"text\" class=\"text\" id=\"myValue\" name=\"myValue\">" +
                "</input></td></tr>" +
                "</table></fieldset>", text);
    }
}
