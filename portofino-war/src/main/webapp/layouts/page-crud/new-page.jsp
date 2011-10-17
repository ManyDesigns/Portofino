<%@ page import="com.manydesigns.portofino.breadcrumbs.Breadcrumbs" %>
<%@ page import="com.manydesigns.elements.util.Util" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/skins/${skin}/modal-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.PortletAction"/>
    <stripes:layout-component name="contentHeader">
        <stripes:submit name="createPage" value="Create" class="contentButton"/>
        <stripes:submit name="cancel" value="Cancel" class="contentButton"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <fmt:message key="layouts.page-crud.new-page.add_new_page"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <mde:write name="actionBean" property="newPageForm"/>
        <br /><br />
        <fieldset class="mde-form-fieldset">
            <legend><fmt:message key="layouts.page-crud.new-page.preview"/></legend>
            <table class="mde-form-table">
                <tbody>
                <tr>
                    <th><label class="mde-field-label" for="url">Url:</label></th>
                    <td><span id="url"></span></td>
                </tr>
                <tr>
                    <th><label class="mde-field-label" for="breadcrumbs">Breadcrumbs:</label></th>
                    <td><span id="breadcrumbs"></span></td>
                </tr>
                </tbody>
            </table>
        </fieldset>
        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter"/>
    <stripes:layout-component name="contentFooter">
        <stripes:submit name="createPage" value="Create" class="contentButton"/>
        <stripes:submit name="cancel" value="Cancel" class="contentButton"/>
        <script type="text/javascript">
            var rootPath = "<%= request.getContextPath() %>";
            var rootBreadcrumbs = ""; //"<span class='breadcrumb-item'>${actionBean.application.model.rootPage.title}</span>";

            var currentPath = "<%= actionBean.dispatch.getPathUrl() %>";
            var currentBreadcrumbs =
                    "<%= StringEscapeUtils.escapeJavaScript(
                            Util.elementToString(new Breadcrumbs(actionBean.dispatch, false))) %>" +
                    "<span class='breadcrumb-separator'> &gt; </span>";

            var parentPath = "<%= actionBean.dispatch.getParentPathUrl() %>";
            var parentBreadcrumbs =
                    "<%= StringEscapeUtils.escapeJavaScript(
                            Util.elementToString(
                                new Breadcrumbs(actionBean.dispatch,
                                                actionBean.dispatch.getPageInstancePath().length - 1,
                                                false))) %>" +
                    "<span class='breadcrumb-separator'> &gt; </span>";

            var urlField = $("#url");
            var breadcrumbsField = $("#breadcrumbs");
            var fragmentField = $("#fragment");
            var titleField = $("#title");

            var position = $("input[name=insertPositionName][checked=checked]").val();
            function recalculateUrlAndBreadcrumbs() {
                var basePath;
                var baseBreadcrumbs;
                if("TOP" == position) {
                    basePath = rootPath;
                    baseBreadcrumbs = rootBreadcrumbs;
                } else if("CHILD" == position) {
                    basePath = rootPath + currentPath;
                    baseBreadcrumbs = currentBreadcrumbs;
                } else if("SIBLING" == position) {
                    basePath = rootPath + parentPath;
                    baseBreadcrumbs = parentBreadcrumbs;
                }

                urlField.html(basePath + "/" + fragmentField.val());
                breadcrumbsField.html(baseBreadcrumbs + "<span class='breadcrumb-item'>" + titleField.val() + "</span>");
            }

            fragmentField.change(recalculateUrlAndBreadcrumbs);
            titleField.change(recalculateUrlAndBreadcrumbs);
            $("input[name=insertPositionName]").change(function() {
                if($(this).attr('checked')) {
                    position = $(this).val();
                    recalculateUrlAndBreadcrumbs();
                }
            });
            recalculateUrlAndBreadcrumbs();
        </script>
    </stripes:layout-component>
</stripes:layout-render>