/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.manydesigns.elements.servlet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>Simple class that encapsulates the process of building up a URL from a path fragment
 * and a zero or more parameters.  Parameters can be single valued, array valued or
 * collection valued.  In the case of arrays and collections, each value in the array or
 * collection will be added as a separate URL parameter (with the same name). The assembled
 * URL can then be retrieved by calling toString().</p>
 *
 * <p>While not immediately obvious, it is possible to add a single parameter with multiple
 * values by invoking the addParameter() method that uses varargs, and supplying a Collection as
 * the single parameter value to the method.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.1.2
 */
public class UrlBuilder {
    /**
     * Holds the name and value of a parameter to be appended to the URL.
     */
    private static class Parameter {
        String name;
        Object value;

        Parameter(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return "" + this.name + "=" + this.value;
        }
    }

    private String baseUrl;
    private String anchor;
    private String parameterSeparator;
    private List<Parameter> parameters = new ArrayList<Parameter>();
    private Charset charset;
    private String url;

    /**
     * Constructs a UrlBuilder with the path to a resource. Parameters can be added
     * later using addParameter().  If the link is to be used in a page then the ampersand
     * character usually used to separate parameters will be escaped using the XML entity
     * for ampersand.
     *
     * @param charset the {@link Charset} to use when formatting URLs
     * @param url the path part of the URL
     * @param isForPage true if the URL is to be embedded in a page (e.g. in an anchor of img
     *        tag), false if for some other purpose.
     */
    public UrlBuilder(Charset charset, String url, boolean isForPage) {
        this(charset, isForPage);
        if (url != null) {
            // Check to see if there is an embedded anchor, and strip it out for later
            int index = url.indexOf('#');
            if (index != -1) {
                if (index < url.length() - 1) {
                    this.anchor = url.substring(index + 1);
                }
                url = url.substring(0, index);
            }

            this.baseUrl = url;
        }
    }

    /**
     * Sets the locale and sets the parameter separator based on the value of <code>isForPage</code>.
     *
     * @param charset the {@link Charset} to use when formatting URLs
     * @param isForPage true if the URL is to be embedded in a page (e.g. in an anchor of img tag),
     *            false if for some other purpose.
     */
    protected UrlBuilder(Charset charset, boolean isForPage) {
        this.charset = charset;
        if (isForPage) {
            this.parameterSeparator = "&amp;";
        }
        else {
            this.parameterSeparator = "&";
        }
    }

    /**
     * Returns the string that will be used to separate parameters in the query string.
     * Will usually be either '&amp;' for query strings that will be embedded in HTML
     * pages and '&' otherwise.
     */
    public String getParameterSeparator() { return parameterSeparator; }

    /**
     * Sets the string that will be used to separate parameters. By default the values is a
     * single ampersand character. If the URL is to be embedded in a page the value should be
     * set to the XML ampersand entity.
     */
    public void setParameterSeparator(String parameterSeparator) {
        this.parameterSeparator = parameterSeparator;
    }

    /**
     * <p>Appends one or more values of a parameter to the URL. Checks to see if each value is
     * null, and if so generates a parameter with no value.  URL Encodes the parameter values
     * to make sure that it is safe to insert into the URL.</p>
     *
     * <p>If any parameter value passed is a Collection or an Array then this method is called
     * recursively with the contents of the collection or array. As a result you can pass
     * arbitrarily nested arrays and collections to this method and it will recurse through them
     * adding all scalar values as parameters to the URL.</p.
     *
     * @param name the name of the request parameter being added
     * @param values one or more values for the parameter supplied
     */
    public UrlBuilder addParameter(String name, Object... values) {
        // If values is null or empty, then simply sub in a single empty string
        if (values == null || values.length == 0) {
            values = new String[] { "" };
        }

        for (Object v : values) {
            // Special case: recurse for nested collections and arrays!
            if (v instanceof Collection<?>) {
                addParameter(name, ((Collection<?>) v).toArray());
            }
            else if (v != null && v.getClass().isArray()) {
                addParameter(name, (Object[]) v);
            }
            else {
                parameters.add(new Parameter(name, v));
                url = null;
            }
        }

        return this;
    }

    /**
     * Appends one or more parameters to the URL.  Various assumptions are made about the Map
     * parameter. Firstly, that the keys are all either Strings, or objects that can be safely
     * toString()'d to yield parameter names.  Secondly that the values either toString() to form
     * a single parameter value, or are arrays or collections that contain toString()'able
     * objects.
     *
     * @param parameters a non-null Map as described above
     */
    public UrlBuilder addParameters(Map<? extends Object,? extends Object> parameters) {
        for (Map.Entry<? extends Object,? extends Object> parameter : parameters.entrySet()) {
            String name = parameter.getKey().toString();
            Object valueOrValues = parameter.getValue();

            if (valueOrValues == null) {
                addParameter(name, (Object) null);
            }
            else if (valueOrValues.getClass().isArray()) {
                addParameter(name, (Object[]) valueOrValues);
            }
            else if (valueOrValues instanceof Collection<?>) {
                addParameter(name, (Collection<?>) valueOrValues);
            }
            else {
                addParameter(name, valueOrValues);
            }
        }

        return this;
    }

    /**
     * Gets the anchor, if any, that will be appended to the URL. E.g. if this method
     * returns 'input' then the URL will be terminated with '#input' in order to instruct
     * the browser to navigate to the HTML anchor called 'input' when accessing the URL.
     *
     * @return the anchor (if any) without the leading pound sign, or null
     */
    public String getAnchor() { return anchor; }

    /**
     * Sets the anchor, if any, that will be appended to the URL. E.g. if supplied with
     * 'input' then the URL will be terminated with '#input' in order to instruct
     * the browser to navigate to the HTML anchor called 'input' when accessing the URL.
     *
     * @param anchor the anchor with or without the leading pound sign, or null to disable
     */
    public UrlBuilder setAnchor(String anchor) {
        if (anchor != null && anchor.startsWith("#") && anchor.length() > 1) {
            this.anchor = anchor.substring(1);
        }
        else {
            this.anchor = anchor;
        }
        return this;
    }

    /**
     * Returns the URL composed thus far as a String.  All paramter values will have been
     * URL encoded and appended to the URL before returning it.
     */
    @Override
    public String toString() {
        if (url == null) {
            url = build();
        }
        if (this.anchor != null && this.anchor.length() > 0) {
            try {
                return url + "#" + URLEncoder.encode(this.anchor, charset.name());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            return url;
        }
    }

    /**
     * Attempts to format an object. A null <code>value</code> will
     * be formatted as an empty string.
     *
     * @param value
     *            the object to be formatted
     * @return the formatted value
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected String format(Object value) {
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }

    /**
     * Build and return the URL
     */
    protected String build() {
        // special handling for event parameter
        List<Parameter> parameters = new ArrayList<Parameter>(this.parameters.size() + 1);
        parameters.addAll(this.parameters);

        StringBuilder buffer = new StringBuilder(256);
        buffer.append(this.baseUrl);
        boolean seenQuestionMark = buffer.indexOf("?") != -1;
        for (Parameter param : parameters) {
            // Figure out whether we already have params or not
            if (!seenQuestionMark) {
                buffer.append('?');
                seenQuestionMark = true;
            }
            else {
                buffer.append(getParameterSeparator());
            }
            try {
                buffer.append(URLEncoder.encode(param.name, charset.name())).append('=');
                if (param.value != null) {
                    String formatted = format(param.value);
                    buffer.append(URLEncoder.encode(formatted, charset.name()));
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return buffer.toString();
    }

}
