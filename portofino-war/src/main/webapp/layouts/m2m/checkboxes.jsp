<%@ page import="java.util.Map" %>
<%@ page import="com.manydesigns.elements.reflection.ClassAccessor" %>
<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.util.ShortNameUtils" %>
<%@ page import="com.manydesigns.portofino.util.PkHelper" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.m2m.ManyToManyAction"/>
<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <c:if test="${not empty actionBean.oneSelectField}">
            <mde:write name="actionBean" property="oneSelectField" />
            <br />
            <script type="text/javascript">
                $(function() {
                    $("#__onePk").change(function() {
                        $(this).attr("form").submit();
                    });
                });
            </script>
        </c:if>
        <%
            ClassAccessor ca = actionBean.getManyTableAccessor();
            XhtmlBuffer buffer = new XhtmlBuffer(out);
            PkHelper pkHelper = new PkHelper(ca);
            for(Map.Entry<Object, Boolean> entry : actionBean.getBooleanRelation().entrySet()) {
                buffer.openElement("label");
                Object obj = entry.getKey();
                buffer.write(ShortNameUtils.getName(ca, obj));
                buffer.closeElement("label");
                buffer.openElement("input");
                buffer.addAttribute("type", "checkbox");
                buffer.addAttribute("name", "selectedPrimaryKeys");
                buffer.addAttribute("value", StringUtils.join(pkHelper.generatePkStringArray(obj), "/"));
                if(entry.getValue()) {
                    buffer.addAttribute("checked", "t");
                }
                buffer.closeElement("input");
                buffer.write(" ");
            }
        %>
        <br /><br />
        <portofino:buttons list="m2m-checkboxes-edit" cssClass="portletButton" />
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
    </stripes:layout-component>
</stripes:layout-render>