<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head></head>
<body>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"/>
<script type="text/javascript">
    window.opener.${actionBean.popupCloseCallback};
    window.self.close();
</script>
</body>
</html>