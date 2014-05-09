<%@ page import="com.manydesigns.portofino.model.database.Table" %>
<%@ page import="java.io.File" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/m/admin/admin-theme/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.database.TablesAction"/>
    <stripes:layout-component name="pageTitle">
        Tables
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:url var="treetablePath"
                     value="/theme/jquery-treetable" />

        <script type="text/javascript" src="${treetablePath}/jquery.treetable.js" >
        </script>

        <script type="text/javascript">
            $(function() {
                $("#tables").treetable({"clickableNodeNames": true, "expandable":true, "treeColumn":0, "indent":20 });
                $("button[name=bulkDelete]").click(function() {
                    return confirm('<fmt:message key="are.you.sure" />');
                });
            });
        </script>
        <table id="tables" style="width: auto;">
            <tr>
                <th width="20%"><fmt:message key="database/schema" /></th>
                <th width="80%"><fmt:message key="table.entity" /></th>
            </tr>

            <%
                String lastDatabase = null;
                String lastSchema = null;
                List<Table> tables = actionBean.getAllTables();
                for(Table table : tables) {
                    if(table.getPrimaryKey() == null) {
                        continue;
                    }
                    if(table.getDatabaseName().equals(lastDatabase)) {
                        if(!table.getSchemaName().equals(lastSchema)) {
                            lastSchema = table.getSchemaName(); %>
                            <tr data-tt-id="<%= lastDatabase + "---" + lastSchema %>"
                                data-tt-parent-id="<%= lastDatabase %>">
                                <td colspan="2"><%= table.getSchemaName() %></td>
                            </tr><%
                        }
                    } else {
                        String changelogFileNameTemplate = "{0}-changelog.xml";
                        String changelogFileName =
                            MessageFormat.format(
                                changelogFileNameTemplate, table.getDatabaseName() + "-" + table.getSchemaName());
                        File changelogFile = new File(actionBean.getPersistence().getAppDbsDir(), changelogFileName);
                        String schemaDescr = table.getSchemaName();
                        if(changelogFile.isFile()) {
                            schemaDescr += " <img src='" + request.getContextPath() + "/m/admin/tables/liquibase_logo_small.gif' /> Liquibase";
                        }

                        lastDatabase = table.getDatabaseName();
                        lastSchema = table.getSchemaName(); %>
                        <tr data-tt-id="<%= lastDatabase %>">
                            <td colspan="2"><%= table.getDatabaseName() %></td>
                        </tr>
                        <tr data-tt-id="<%= lastDatabase + "---" + lastSchema %>"
                            data-tt-parent-id="<%= lastDatabase %>">
                            <td colspan="2"><%= schemaDescr %></td>
                        </tr>
                        <%
                    }
                    String tableDescr = table.getTableName();
                    if(!table.getActualEntityName().equals(table.getTableName())) {
                        tableDescr += " (" + table.getActualEntityName() + ")";
                    }
                    %>
                    <tr data-tt-id="<%= lastDatabase + "---" + lastSchema + "---" + table.getTableName() %>"
                        data-tt-parent-id="<%= lastDatabase + "---" + lastSchema %>">
                        <td></td>
                        <td><a href="<%= lastDatabase %>/<%= lastSchema %>/<%= table.getTableName() %>"
                                ><%= tableDescr %></a></td>
                    </tr><%
                } %>
        </table>
        <stripes:form beanclass="com.manydesigns.portofino.actions.admin.database.TablesAction"
                      method="post">
            <div class="form-group">
                <portofino:buttons list="tables-list" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>