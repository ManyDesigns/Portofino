<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.CrudAction"/>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.crud.readTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <mde:write name="actionBean" property="form"/>
        <input type="hidden" name="pk" value="<c:out value="${actionBean.pk}"/>"/>
        <c:if test="${not empty actionBean.searchString}">
            <input type="hidden" name="searchString" value="<c:out value="${actionBean.searchString}"/>"/>
        </c:if>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
        <div class="crudReadButtons">
            <portofino:buttons list="crud-read" cssClass="portletButton" />
        </div>
        <!--<stripes:submit name="duplicate" value="Duplicate" disabled="true" class="portletButton"/>
        <stripes:submit name="print" value="Print" disabled="true" class="portletButton"/>-->
    </stripes:layout-component>
    <script type="text/javascript">
        $(".crudReadButtons button[name=delete]").click(function() {
            return confirm ('Are you sure?');
        });
    </script>
</stripes:layout-render>