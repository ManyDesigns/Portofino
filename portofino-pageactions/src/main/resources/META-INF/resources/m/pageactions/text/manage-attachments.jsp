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
        /><stripes:layout-render name="/theme/templates/${actionBean.pageTemplate}/modal.jsp">
    <stripes:layout-component name="pageTitle">
        <fmt:message key="manage.attachments.for.page._">
            <fmt:param value="<%= StringEscapeUtils.escapeHtml(actionBean.getPage().getTitle()) %>" />
        </fmt:message>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form action="${actionBean.context.actionPath}" method="post" enctype="multipart/form-data">
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <c:if test="${not empty actionBean.textConfiguration.attachments}">
                <legend><fmt:message key="attachments"/>: </legend>

                <c:forEach var="attachment" items="${actionBean.textConfiguration.attachments}">
                    <div class="row form-inline">
                        <div class="form-group">
                            <div class=" form-control checkbox">
                                <stripes:checkbox name="selection" value="${attachment.id}" id="${attachment.id}" />
                                <label for="${attachment.id}" >
                                    <a href="<c:out value="${pageContext.request.contextPath}${actionBean.context.actionPath}?downloadAttachment=&id=${attachment.id}"/>">
                                        <span class="glyphicon glyphicon-download"></span> <c:out value="${attachment.filename}"/>
                                    </a>
                                </label>
                            </div>

                            <div class="form-control checkbox">
                                <c:if test="${attachment.downloadable eq true}">
                                <input type="checkbox" name="downloadable" value="${attachment.id}" id="${attachment.id}_down" checked />
                                </c:if>
                                <c:if test="${attachment.downloadable eq false}">
                                 <input type="checkbox" name="downloadable" value="${attachment.id}" id="${attachment.id}_down" />
                                </c:if>
                                <label for="${attachment.id}_down"> <fmt:message key="downloadable" /> </label>
                            </div>
                        </div>
                    </div>
                </c:forEach>
                <br/>
                <portofino:buttons list="manage-attachments-delete" cssClass="manage-attachments-delete" />

                <script type="text/javascript">
                    $(function() {
                        $("button.manage-attachments-delete").click(function() {
                            return confirm('<fmt:message key="delete.selected.attachments"/>');
                        });
                    });

                    $('#file').fileinput({'showUpload':false, 'previewFileType':'text' , 'browseLabel':'' , 'removeLabel':''});
                </script>
            </c:if><c:if test="${empty actionBean.textConfiguration.attachments}">
            <fmt:message key="there.are.no.attachments"/>
        </c:if>
            <div class="horizontalSeparator"></div>

            <legend><fmt:message key="upload.a.new.file"/>:</legend>

            <div class="form-group">
                <!--<stripes:file name="upload"/> -->
                <input id="upload" type="file" class="file" data-preview-file-type="text" data-show-upload="false"  name="upload" >
                <div class="form-control checkbox">
                    <stripes:checkbox name="uploadDownloadable" checked="checked" id="uploadDownloadable"/>
                    <label for="uploadDownloadable"><fmt:message key="downloadable" />  </label>
                </div>
            </div>

            <div class="form-group">
                <portofino:buttons list="manage-attachments-upload" />
            </div>
            <div class="form-group">
                <portofino:buttons list="manage-attachments" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>