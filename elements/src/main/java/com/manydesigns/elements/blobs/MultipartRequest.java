package com.manydesigns.elements.blobs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class MultipartRequest extends HttpServletRequestWrapper {
    protected final Map<String, String[]> parameterMap;
    protected final MultipartWrapper multipartWrapper;

    public MultipartRequest(HttpServletRequest request, MultipartWrapper multipartWrapper) {
        super(request);
        parameterMap = new HashMap<>(request.getParameterMap());
        Enumeration<String> parameterNames = multipartWrapper.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            parameterMap.put(name, multipartWrapper.getParameterValues(name));
        }
        this.multipartWrapper = multipartWrapper;
    }

    @Override
    public String getParameter(String name) {
        String[] strings = parameterMap.get(name);
        if(strings != null && strings.length > 0) {
            return strings[0];
        } else {
            return null;
        }
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return new IteratorEnumeration(parameterMap.keySet().iterator());
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameterMap.get(name);
    }

    FileBean getFileParameterValue(String name) {
        return multipartWrapper.getFileParameterValue(name);
    }
}
