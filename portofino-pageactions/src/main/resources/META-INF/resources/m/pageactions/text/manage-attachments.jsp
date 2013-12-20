<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ page import="org.apache.commons.lang.StringEscapeUtils"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><jsp:useBean id="actionBean" scope="request"
               type="com.manydesigns.portofino.pageactions.text.TextAction"
/><stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/modal.jsp">
    <stripes:layout-component name="pageTitle">
        <fmt:message key="manage.attachments.for.page._">
            <fmt:param value="<%= StringEscapeUtils.escapeHtml(actionBean.getPage().getTitle()) %>" />
        </fmt:message>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form action="${actionBean.context.actionPath}" method="post" enctype="multipart/form-data"
                      class="form-inline">
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <c:if test="${not empty actionBean.textConfiguration.attachments}">
                <fmt:message key="attachments"/>:
                <br/>
                <table>
                    <c:forEach var="attachment" items="${actionBean.textConfiguration.attachments}">
                        <tr>
                            <td>
                                <label class="checkbox">
                                    <stripes:checkbox name="selection" value="${attachment.id}"/>
                                    <a href="<c:out value="${pageContext.request.contextPath}${actionBean.context.actionPath}?downloadAttachment=&id=${attachment.id}"/>"
                                            ><c:out value="${attachment.filename}"/></a>
                                </label>
                            </td>
                            <td>
                                <label class="checkbox">
                                    <stripes:checkbox name="downloadable" value="${attachment.id}"
                                                      checked="${attachment.downloadable ? attachment.id : null}"/>
                                    <fmt:message key="downloadable" />
                                </label>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
                <br/>
                <portofino:buttons list="manage-attachments-delete" cssClass="manage-attachments-delete" />
                <script type="text/javascript">
                    $(function() {
                        $("button.manage-attachments-delete").click(function() {
                            return confirm('<fmt:message key="delete.selected.attachments"/>');
                        });
                    });
                </script>
            </c:if><c:if test="${empty actionBean.textConfiguration.attachments}">
                <fmt:message key="there.are.no.attachments"/>
            </c:if>
            <div class="horizontalSeparator"></div>
            <fmt:message key="upload.a.new.file"/>:
            <stripes:file name="upload"/>
            <label class="checkbox">
                <stripes:checkbox name="uploadDownloadable" checked="checked" />
                <fmt:message key="downloadable" />
            </label>
            <br/>
            <br/>
            <portofino:buttons list="manage-attachments-upload" />
            <br/>
            <br/>
            <div class="form-actions">
                <portofino:buttons list="manage-attachments" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>