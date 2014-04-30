<%@ page import="com.manydesigns.portofino.breadcrumbs.BreadcrumbItem" %>
<%@ page import="com.manydesigns.portofino.breadcrumbs.Breadcrumbs" %>
<%@ page import="com.manydesigns.portofino.dispatcher.PageInstance" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8" %><%@
    taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@
    taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%><%@
    taglib prefix="mde" uri="/manydesigns-elements"%><%@
    taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%@
    taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/modal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.page.PageAdminAction"/>
    <stripes:layout-component name="contentHeader">
        <mde:sessionMessages />
    </stripes:layout-component>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="add.new.page"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form action="/actions/admin/page" method="post" enctype="multipart/form-data"
                      class="form-horizontal">
            <input type="hidden" name="originalPath" value="${actionBean.originalPath}" />
            <mde:write name="actionBean" property="newPageForm"/>
            <fieldset>
                <legend><fmt:message key="preview"/></legend>
                <div class="form-group readonly">
                    <label class="control-label col-sm-2" for="url">Url</label>
                    <div class="col-sm-10">
                        <p class="form-control-static" id="url"></p>
                    </div>
                </div>
                <div class="form-group readonly">
                    <label class="control-label col-sm-2" for="breadcrumbs">Breadcrumbs</label>
                    <div class="col-sm-10">
                        <p class="form-control-static" id="breadcrumbs"></p>
                    </div>
                </div>
            </fieldset>
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <div class="form-group">
                <div class="col-sm-12">
                    <portofino:buttons list="page-create" />
                </div>
            </div>
        </stripes:form>
        <% PageInstance pageInstance = actionBean.dispatch.getLastPageInstance(); %>
        <script type="text/javascript">
            $(function() {
                var rootPath = "<%= request.getContextPath() %>";
                var rootBreadcrumbs = "";

                var currentPath = "<%= pageInstance.getPath() %>";
                var currentBreadcrumbs =
                        "<%= generateBreadcrumbs(new Breadcrumbs(actionBean.dispatch)) %>";

                var parentPath = "<%= pageInstance.getParent().getPath() %>";
                var parentBreadcrumbs =
                        "<%= generateBreadcrumbs(new Breadcrumbs(actionBean.dispatch,
                                                    actionBean.dispatch.getPageInstancePath().length - 1)) %>";

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

                    urlField.html(htmlEscape(basePath + "/" + fragmentField.val()));
                    breadcrumbsField.html(htmlEscape(baseBreadcrumbs + titleField.val()));
                }

                fragmentField.change(recalculateUrlAndBreadcrumbs);
                titleField.change(recalculateUrlAndBreadcrumbs);
                $("input[name=insertPositionName]").change(function() {
                    if($(this).prop('checked')) {
                        position = $(this).val();
                        recalculateUrlAndBreadcrumbs();
                    }
                });
                recalculateUrlAndBreadcrumbs();
            });
        </script>
    </stripes:layout-component>
</stripes:layout-render>
<%!
    private String generateBreadcrumbs(Breadcrumbs breadcrumbs) {
        List<BreadcrumbItem> items = breadcrumbs.getItems();
        StringBuilder sb = new StringBuilder();
        for (BreadcrumbItem current : items) {
            sb.append(current.getText());
            sb.append(" > ");
        }
        return StringEscapeUtils.escapeJavaScript(sb.toString());
    }
%>