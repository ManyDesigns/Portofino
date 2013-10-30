<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.text.TextAction"
/><html>
<body>
<script type="text/javascript">
    window.parent.CKEDITOR.tools.callFunction(
            <c:out value="${actionBean.CKEditorFuncNum}"/>,
            '<c:out value="${actionBean.viewAttachmentUrl}" escapeXml="false"/>',
            '<c:out value="${actionBean.message}"/>'
    );
</script>
</body>
</html>