<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.CrudAction"/>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.crudConfiguration.readTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <mde:write name="actionBean" property="form"/>
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
            return confirm ('<fmt:message key="commons.confirm" />');
        });
    </script>
</stripes:layout-render>