<%@ page import="com.manydesigns.portofino.menu.*"
%><%@ page import="org.apache.commons.lang.StringUtils"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%
    MenuBuilder menuBuilder = (MenuBuilder) application.getAttribute(request.getAttribute("menu").toString());
    Menu menu = menuBuilder.build();
    for(MenuItem item : menu.items) {
        if(item instanceof MenuGroup) { %>
            <li class="dropdown">
                <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                    <% if(item.icon != null) { %>
                        <i class="<%= item.icon %>"></i>
                    <% } %>
                    <%= item.label %> <b class="caret"></b>
                </a>
                <ul class="dropdown-menu">
                <% for(MenuLink link : ((MenuGroup) item).menuLinks) { %>
                    <li>
                        <stripes:link href='<%= StringUtils.defaultString(link.link, "#") %>'>
                            <% if(link.icon != null) { %>
                                <i class="<%= link.icon %>"></i>
                            <% } %>
                            <c:out value="<%= link.label %>"/>
                        </stripes:link>
                    </li>
                <% } %>
                </ul>
        <% } else {
            MenuLink link = (MenuLink) item; %>
            <li>
                <stripes:link href='<%= StringUtils.defaultString(link.link, "#") %>'>
                    <% if(link.icon != null) { %>
                        <i class="<%= link.icon %>"></i>
                    <% } %>
                    <c:out value="<%= link.label %>"/>
                </stripes:link>
            </li>
        <% }
    }
%>