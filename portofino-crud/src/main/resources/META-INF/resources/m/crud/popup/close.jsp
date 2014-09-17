<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ page import="com.manydesigns.elements.util.RandomUtil"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"
/><c:set var="randomId" value='<%= "_dialog" + RandomUtil.createRandomId() %>' />
<script type="text/javascript">
    (function() {
        ${actionBean.popupCloseCallback};
        var modal = $("#${randomId}");
        modal.on('hidden.bs.modal', function (e) {
            modal.remove();
        });
        modal.modal("hide");
    })();
</script>