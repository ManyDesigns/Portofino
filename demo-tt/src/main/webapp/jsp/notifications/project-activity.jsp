<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:useBean id="activityItem" scope="request" type="com.manydesigns.portofino.pageactions.activitystream.ActivityItem"/>
<body>
<div style="padding: 20px;background-color: #f5f5f5;font-family: Helvetica, Arial, sans-serif;font-size: 14px;line-height: 20px;">
    <div style="padding: 10px 20px 20px;margin: 0 auto;background-color: #ffffff;border: 1px solid #e5e5e5;-webkit-border-radius: 5px;-moz-border-radius: 5px;border-radius: 5px;-webkit-box-shadow: 0 1px 2px rgba(0, 0, 0, .05);-moz-box-shadow: 0 1px 2px rgba(0, 0, 0, .05);box-shadow: 0 1px 2px rgba(0, 0, 0, .05);">
        <%
            XhtmlBuffer xb = new XhtmlBuffer(out);
            activityItem.writeData(xb);
            activityItem.writeTimestamp(xb);
            activityItem.writeMessage(xb);
        %>
    </div>
</div>
</body>