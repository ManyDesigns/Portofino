<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.TextAction"
/><html>
<body>
<script type="text/javascript">
    window.parent.CKEDITOR.tools.callFunction(
            <c:out value="${actionBean.CKEditorFuncNum}"/>,
            '<c:out value="${actionBean.downloadAttachmentUrl}" escapeXml="false"/>',
            '<c:out value="${actionBean.message}"/>'
    );
</script>
</body>
</html>