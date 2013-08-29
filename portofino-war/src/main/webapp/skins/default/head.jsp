<%@ page import="com.manydesigns.portofino.head.HtmlHead"
%><%@ page import="com.manydesigns.portofino.head.HtmlHeadBuilder"
%><%@ page import="com.manydesigns.portofino.modules.BaseModule"
%><%@ page import="com.manydesigns.elements.xml.XhtmlFragment"
%><%@ page import="com.manydesigns.elements.xml.XhtmlBuffer"
%><%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%
    HtmlHeadBuilder htmlHeadBuilder = (HtmlHeadBuilder) application.getAttribute(BaseModule.HTML_HEAD_BUILDER);
    HtmlHead head = htmlHeadBuilder.build();

    XhtmlBuffer xhtmlBuffer = new XhtmlBuffer(out);
    for(XhtmlFragment fragment : head.fragments) {
        fragment.toXhtml(xhtmlBuffer);
    }

%>