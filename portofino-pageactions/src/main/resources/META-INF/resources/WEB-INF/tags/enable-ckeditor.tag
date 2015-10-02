<%@ attribute name="version" required="false"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><c:if test="${empty version}">
    <c:set var="version" value="4.5.3" scope="page" />
</c:if>
<script type="text/javascript" src="<stripes:url value="/webjars/ckeditor/${version}/standard/ckeditor.js"/>"></script>
<script type="text/javascript" src="<stripes:url value="/webjars/ckeditor/${version}/standard/adapters/jquery.js"/>"></script>
<script type="text/javascript">
    $(function() {
        portofino.setupRichTextEditors({
            toolbarCanCollapse: true,
            height: null
        });
    });
</script>