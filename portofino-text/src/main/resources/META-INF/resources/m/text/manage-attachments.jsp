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
/><stripes:layout-render name="/m/theme${actionBean.pageTemplate}/modal.jsp">
    <stripes:layout-component name="portletTitle">
        <fmt:message key="layouts.text.manage-attachments.manage_attachments_for_page">
            <fmt:param value="<%= StringEscapeUtils.escapeHtml(actionBean.getPage().getTitle()) %>" />
        </fmt:message>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <stripes:form action="${actionBean.dispatch.originalPath}" method="post" enctype="multipart/form-data"
                      class="form-inline">
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
            <c:if test="${not empty actionBean.textConfiguration.attachments}">
                <fmt:message key="commons.attachments"/>:
                <br/>
                <table>
                    <c:forEach var="attachment" items="${actionBean.textConfiguration.attachments}">
                        <tr>
                            <td>
                                <label class="checkbox">
                                    <stripes:checkbox name="selection" value="${attachment.id}"/>
                                    <a href="<c:out value="${actionBean.dispatch.absoluteOriginalPath}?downloadAttachment=&id=${attachment.id}"/>"
                                            ><c:out value="${attachment.filename}"/></a>
                                </label>
                            </td>
                            <td>
                                <label class="checkbox">
                                    <stripes:checkbox name="downloadable" value="${attachment.id}"
                                                      checked="${attachment.downloadable ? attachment.id : null}"/>
                                    <fmt:message key="layouts.text.manage-attachments.downloadable" />
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
                            return confirm('<fmt:message key="layouts.text.manage-attachments.confirm_delete"/>');
                        });
                    });
                </script>
            </c:if><c:if test="${empty actionBean.textConfiguration.attachments}">
                <fmt:message key="layouts.text.manage-attachments.there_are_no_attachments"/>
            </c:if>
            <div class="horizontalSeparator"></div>
            <fmt:message key="layouts.text.manage-attachments.upload_new_file"/>:
            <stripes:file name="upload"/>
            <label class="checkbox">
                <stripes:checkbox name="uploadDownloadable" checked="checked" />
                <fmt:message key="layouts.text.manage-attachments.downloadable" />
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