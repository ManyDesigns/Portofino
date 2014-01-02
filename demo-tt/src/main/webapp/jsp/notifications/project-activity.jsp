<%@ page import="com.manydesigns.elements.util.Util" %>
<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.PortofinoProperties" %>
<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:useBean id="activityItem" scope="request" type="com.manydesigns.portofino.pageactions.activitystream.ActivityItem"/>
<jsp:useBean id="portofinoConfiguration" scope="application" type="org.apache.commons.configuration.Configuration"/>
<body>
<div style="padding: 20px;background-color: #f5f5f5;font-family: Helvetica, Arial, sans-serif;font-size: 14px;line-height: 20px;">
    <div style="padding: 10px 20px 20px;margin: 0 auto;background-color: #ffffff;border: 1px solid #e5e5e5;-webkit-border-radius: 5px;-moz-border-radius: 5px;border-radius: 5px;-webkit-box-shadow: 0 1px 2px rgba(0, 0, 0, .05);-moz-box-shadow: 0 1px 2px rgba(0, 0, 0, .05);box-shadow: 0 1px 2px rgba(0, 0, 0, .05);">
        <%
            XhtmlBuffer xb = new XhtmlBuffer(out);
            activityItem.setFullUrls(true);
            activityItem.writeData(xb);
            activityItem.writeTimestamp(xb);
            activityItem.writeMessage(xb);

            String appUrl = Util.getAbsoluteUrl("/", true);
            pageContext.setAttribute("appUrl", appUrl);

            String profileUrl = Util.getAbsoluteUrl("/profile", true);
            pageContext.setAttribute("profileUrl", profileUrl);

            String reviewNotificationsUrl = Util.getAbsoluteUrl("/profile/notifications", true);
            pageContext.setAttribute("reviewNotificationsUrl", reviewNotificationsUrl);
        %>
        <div style="border-top: 1px solid #ddd; padding-top: 10px; margin-top: 10px;">
            <small>
                You received this email as a user of
                <stripes:link href="${appUrl}">
                    <c:out value="<%= portofinoConfiguration.getString(PortofinoProperties.APP_NAME) %>"/>
                </stripes:link>.
                You can
                <stripes:link href="${profileUrl}">view your profile</stripes:link>
                or
                <stripes:link href="${reviewNotificationsUrl}">change your notifications</stripes:link>
                at any time.
            </small>
        </div>
    </div>
</div>
</body>