<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/skins/${skin}${actionBean.pageTemplate}/modal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.text.TextAction"/>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="manage-attachments" cssClass="contentButton" />
        <jsp:include page="/skins/${skin}/breadcrumbs.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <fmt:message key="layouts.text.manage-attachments.manage_attachments_for_page"/>
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
        <c:if test="${not empty actionBean.textConfiguration.attachments}">
            <fmt:message key="commons.attachments"/>:
            <br/>
            <table class="noBorder">
                <c:forEach var="attachment" items="${actionBean.textConfiguration.attachments}">
                    <tr>
                        <td>
                            <stripes:checkbox name="selection" value="${attachment.id}"/>
                            <a href="<c:out value="${actionBean.dispatch.absoluteOriginalPath}?downloadAttachment=&id=${attachment.id}"/>"
                                    ><c:out value="${attachment.filename}"/></a>
                        </td>
                        <td>
                            <stripes:checkbox name="downloadable" value="${attachment.id}"
                                              checked="${attachment.downloadable ? attachment.id : null}"/>
                            <fmt:message key="layouts.text.manage-attachments.downloadable" />
                        </td>
                    </tr>
                </c:forEach>
            </table>
            <br/>
            <portofino:buttons list="manage-attachments-delete" cssClass="portletButton manage-attachments-delete" />
            <script type="text/javascript">
                $(function() {
                    $("button.manage-attachments-delete").click(function() {
                        return confirm('<fmt:message key="layouts.text.manage-attachments.confirm_delete"/>');
                    });
                });
            </script>
        </c:if><c:if test="${empty actionBean.textConfiguration.attachments}">
            <fmt:message key="layouts.text.manage-attachments.manage_attachments_for_page"/>
        </c:if>
        <div class="horizontalSeparator"></div>
        <fmt:message key="layouts.text.manage-attachments.upload_new_file"/>:
        <stripes:file name="upload"/>
        <stripes:checkbox name="uploadDownloadable" checked="checked" />
        <fmt:message key="layouts.text.manage-attachments.downloadable" />
        <br/>
        <br/>
        <portofino:buttons list="manage-attachments-upload" cssClass="portletButton" />
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="manage-attachments" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>