<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/skins/${skin}/content-page.jsp" >
    <jsp:useBean id="actionBean" scope="request"
                 type="com.manydesigns.portofino.actions.PortletAction"/>
    <stripes:layout-component name="contentBody">
        <mde:sessionMessages/>
        <div id="default" class="portletContainer">
            <c:forEach var="portletInstance" items="${ actionBean.portlets['default'] }">
                <input type="hidden" class="updateLayout" name="portletWrapper_default" value="<c:out value='${portletInstance.id}' />" />
                <div class="portletWrapper" id="portletWrapper_<c:out value='${portletInstance.id}' />">
                    <jsp:include page="${portletInstance.jsp}" />
                </div>
            </c:forEach>
        </div>
        <div class="yui-gc">
            <div id="contentLayoutLeft" class="yui-u first portletContainer">
                <c:forEach var="portletInstance" items="${ actionBean.portlets['contentLayoutLeft'] }">
                    <input type="hidden" class="updateLayout" name="portletWrapper_contentLayoutLeft" value="<c:out value='${portletInstance.id}' />" />
                    <div class="portletWrapper" id="portletWrapper_<c:out value='${portletInstance.id}' />">
                        <jsp:include page="${portletInstance.jsp}" />
                    </div>
                </c:forEach>
            </div>
            <div id="contentLayoutRight" class="yui-u portletContainer">
                <c:forEach var="portletInstance" items="${ actionBean.portlets['contentLayoutRight'] }">
                    <input type="hidden" class="updateLayout" name="portletWrapper_contentLayoutRight" value="<c:out value='${portletInstance.id}' />" />
                    <div class="portletWrapper" id="portletWrapper_<c:out value='${portletInstance.id}' />">
                        <jsp:include page="${portletInstance.jsp}" />
                    </div>
                </c:forEach>
            </div>
        </div>
        <div id="contentLayoutBottom" class="portletContainer">
            <c:forEach var="portletInstance" items="${ actionBean.portlets['contentLayoutBottom'] }">
                <input type="hidden" class="updateLayout" name="portletWrapper_contentLayoutBottom" value="<c:out value='${portletInstance.id}' />" />
                <div class="portletWrapper" id="portletWrapper_<c:out value='${portletInstance.id}' />">
                    <jsp:include page="${portletInstance.jsp}" />
                </div>
            </c:forEach>
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
    </stripes:layout-component>
</stripes:layout-render>