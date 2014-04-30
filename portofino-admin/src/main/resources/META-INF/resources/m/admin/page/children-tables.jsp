<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<p><fmt:message key="drag.the.rows.of.the.tables.to.reorder.the.children" /></p>
<input type="hidden" name="originalPath" value="${actionBean.originalPath}" />
<div id="directChildren" class="childrenTable">
    <mde:write name="actionBean" property="childPagesForm" />
</div>
<c:if test="${not empty actionBean.detailChildPagesForm}">
    <div id="detailChildren" class="childrenTable">
        <h2><fmt:message key="detail" /></h2>
        <mde:write name="actionBean" property="detailChildPagesForm" />
    </div>
</c:if>
<input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
<script type="text/javascript">
    $(function() {
        var originalPathInput = $("input[name=originalPath]");
        $("div.childrenTable tbody").each(function(i, table) {
            $(table).find('tr').each(function(i, tr) {
                tr = $(tr);
                tr.data('childName', tr.find("td p.form-control-static").first().html());
            });
        });
        var sortables = $("div.childrenTable tbody").sortable({
                cursor: "move", // cursor image
                revert: true // moves the pageAction to its new position with a smooth transition
            });
        function prepareChildrenTablesForm(sortables) {
            sortables.each(function(i, elem) {
                var items = $($(elem).sortable( "option", "items" ), elem)
                        .not('.ui-sortable-helper').not('.ui-sortable-placeholder');
                items.each(function(j, elem) {
                    var hiddenField = document.createElement("input");
                    hiddenField.setAttribute("type", "hidden");
                    hiddenField.setAttribute("name", $(elem).closest("div.childrenTable").attr("id"));
                    hiddenField.setAttribute("value", $(elem).data('childName'));
                    originalPathInput.before(hiddenField);
                });
            });
        }
        $("button[name=updatePageChildren]").click(
            function() {
                prepareChildrenTablesForm(sortables);
            });
        });
</script>