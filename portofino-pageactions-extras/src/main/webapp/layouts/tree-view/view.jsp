<%@ page import="com.manydesigns.portofino.pageactions.treeview.model.Node" %>
<%@ page
        import="com.manydesigns.portofino.pageactions.treeview.model.TreeModel" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.io.IOException" %>
<%@ page
        import="com.manydesigns.portofino.pageactions.treeview.configuration.TreeViewConfiguration" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes"
           uri="http://stripes.sourceforge.net/stripes-dynattr.tld" %>
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.pageactions.treeview.TreeViewAction"/>
<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <style type="text/css">
            @import "<stripes:url value="/layouts/tree-view/jquery.treeTable.css"/>";
        </style>
        <script language="javascript"
                src="<stripes:url value="/layouts/tree-view/jquery.treeTable.min.js"/>"></script>
        <%
            final TreeViewConfiguration configuration = actionBean.getConfiguration();
        %>

        <table id="typeTree">
            <%
                actionBean.loadTreeModel();
                TreeModel tree = actionBean.getTreeModel();
            %>
            <thead>
                <tr>
                    <%
                        for(String header : tree.getHeaders()) {
                            out.println("<th>"+header+"</th>");
                        }
                    %>

                </tr>
            </thead>
            <tbody>
            <%
                for(Node obj : tree.getNodes()) {
                    out.print("<tr id=\"node-" + obj.getId() + "\" >");
                    for(Object ob : obj.getData()){
                        out.println("<td>"+ob+"</td>");
                    }
                    out.print("</tr>");

                    for (Node node : obj.getChildren()){
                        printNode(out, obj, node);
                    }
                }
                %>
            </tbody>
            </table>

            <script type="text/javascript" src="<%= request.getContextPath() %>/jquery-treetable-2.3.0/jquery.treeTable.min.js" >
            </script>

            <script type="text/javascript">
                $("#typeTree").treeTable({"clickableNodeNames": <%=configuration.isClickableNodeNames()?"true":"false" %>, "expandable":<%=configuration.isExpandable()?"true":"false" %>, "treeColumn": <%= configuration.getTreeColumn()%>, "indent":<%= configuration.getIndent()%> });
            </script>

    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
    </stripes:layout-component>
</stripes:layout-render>
<%!
    private void printNode(JspWriter out, Node father, Node node) throws IOException {
        out.print("<tr id=\"node-" + node.getId() + "\" class=\"child-of-node-"+father.getId()+"\" >");
        for(Object ob : node.getData()){
            out.println("<td>"+ob+"</td>");
        }
        out.print("</tr>");
        for (Node children : node.getChildren()){
            printNode(out, node, children);
        }
    }
%>