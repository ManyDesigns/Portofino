<%@ page import="com.manydesigns.portofino.model.database.Table" %>
<%@ page import="java.io.File" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.TablesAction"/>
    <stripes:layout-component name="customScripts">
        <script type="text/javascript" src="<%= request.getContextPath() %>/jquery-treetable-2.3.0/jquery.treeTable.min.js" >
        </script>

        <script type="text/javascript">
            $(function() {
                $("#tables").treeTable({"clickableNodeNames": true, "expandable":true, "treeColumn":0, "indent":20 });
                $("button[name=bulkDelete]").click(function() {
                    return confirm('<fmt:message key="commons.confirm" />');
                });
            });
        </script>
    </stripes:layout-component>
    <stripes:layout-component name="pageTitle">
        Tables
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="tables-list" cssClass="contentButton" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        Tables
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <table id="tables" style="width: auto;">
            <tr>
                <th width="20%"><fmt:message key="layouts.admin.tables.databaseSlashSchema" /></th>
                <th width="80%"><fmt:message key="layouts.admin.tables.table" /></th>
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
                            <tr id="<%= "node-" + lastDatabase + "---" + lastSchema %>"
                                class="child-of-node-<%= lastDatabase %>">
                                <td colspan="2"><%= table.getSchemaName() %></td>
                            </tr><%
                        }
                    } else {
                        String changelogFileNameTemplate = "{0}-changelog.xml";
                        String changelogFileName =
                            MessageFormat.format(
                                changelogFileNameTemplate, table.getDatabaseName() + "-" + table.getSchemaName());
                        File changelogFile = new File(actionBean.getApplication().getAppDbsDir(), changelogFileName);
                        String schemaDescr = table.getSchemaName();
                        if(changelogFile.isFile()) {
                            schemaDescr += " <img src='" + request.getContextPath() + "/layouts/admin/tables/liquibase_logo_small.gif' /> Liquibase";
                        }

                        lastDatabase = table.getDatabaseName();
                        lastSchema = table.getSchemaName(); %>
                        <tr id="<%= "node-" + lastDatabase %>">
                            <td colspan="2"><%= table.getDatabaseName() %></td>
                        </tr>
                        <tr id="<%= "node-" + lastDatabase + "---" + lastSchema %>"
                            class="child-of-node-<%= lastDatabase %>">
                            <td colspan="2"><%= schemaDescr %></td>
                        </tr>
                        <%
                    }
                    String tableDescr = table.getTableName();
                    if(!table.getActualEntityName().equals(table.getTableName())) {
                        tableDescr += " (" + table.getActualEntityName() + ")";
                    }
                    %>
                    <tr id="<%= "node-" + lastDatabase + "---" + lastSchema + "---" + table.getTableName() %>"
                        class="child-of-node-<%= lastDatabase + "---" + lastSchema %>">
                        <td></td>
                        <td><a href="tables/<%= lastDatabase %>/<%= lastSchema %>/<%= table.getTableName() %>"
                                ><%= tableDescr %></a></td>
                    </tr><%
                } %>
        </table>
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="tables-list" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>