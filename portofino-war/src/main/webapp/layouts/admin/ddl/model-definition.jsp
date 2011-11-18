<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.DDLAction"/>
    <stripes:layout-component name="pageTitle">
        Model Definition
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <stripes:submit name="update" value="Update" class="contentButton"/>
        <stripes:submit name="returnToPages" value="Return to pages" class="contentButton"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        Model Definition
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">

    <style type="text/css">
        td {
            vertical-align: top;
            font-family: Arial,sans-serif;
            font-size: 1em;
        }
    </style>

    <script type="text/javascript" src="<%= request.getContextPath() %>/layouts/admin/ddl/ddl.js">
    </script>
    <table id="modelTable" width="100%" >
        <tr>
            <td rowspan="2" width="30%" >
                <table id="dbTree">
                    <tr width="100%" style="background-color: #ECEEF0">
                        <th width="30%">Database</th>
                        <th width="30%">Schema</th>
                        <th width="40%">Table</th>
                    </tr>
                    <c:forEach var="table" items="${actionBean._tbls}">
                    <tr id="node-${table[0]}"
                        <c:if test="${table[1]!=null}">class="child-of-node-${table[1]}" onclick="getDetails('${table[6]}');"</c:if>
                        onmouseover="style.backgroundColor='#EEEEFF';" onmouseout="style.backgroundColor='#fff'" >
                      <td><c:if test="${table[1]==null}">${table[2]}</c:if></td>
                      <td>${table[4]}</td>
                      <td>${table[5]}</td>
                    </tr>
                    </c:forEach>
                </table>

                <script src="<%= request.getContextPath() %>/jquery-treetable-2.3.0/jquery.treeTable.min.js" >
                </script>

                <script>
                    $("#dbTree").treeTable({"clickableNodeNames": true, "expandable":true, "treeColumn":0, "indent":20 });
                </script>
            </td>
            <td width="70%">
                <jsp:include page="componenti.jsp" flush="true" />
            </td>
        </tr>
        <tr>
            <td></td>
        </tr>

    </table>

    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <stripes:submit name="update" value="Update" class="contentButton"/>
        <stripes:submit name="returnToPages" value="Return to pages" class="contentButton"/>
    </stripes:layout-component>
</stripes:layout-render>