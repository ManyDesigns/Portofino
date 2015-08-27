<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/m/admin/admin-theme/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.database.TablesAction"/>
    <stripes:layout-component name="pageTitle"> Tables </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <link href="<stripes:url value="/theme/fancytree/skin-bootstrap/ui.fancytree.css"/>" rel="stylesheet" type="text/css" class="skinswitcher">

        <script src="<stripes:url value="/theme/fancytree/src/jquery.fancytree.js"/>"       type="text/javascript"></script>
        <script src="<stripes:url value="/theme/fancytree/src/jquery.fancytree.dnd.js"/>"   type="text/javascript"></script>
        <script src="<stripes:url value="/theme/fancytree/src/jquery.fancytree.edit.js"/>"  type="text/javascript"></script>
        <script src="<stripes:url value="/theme/fancytree/src/jquery.fancytree.glyph.js"/>" type="text/javascript"></script>
        <script src="<stripes:url value="/theme/fancytree/src/jquery.fancytree.table.js"/>" type="text/javascript"></script>
        <script src="<stripes:url value="/theme/fancytree/src/jquery.fancytree.wide.js"/>"  type="text/javascript"></script>

        <!-- (Irrelevant source removed.) -->

        <style type="text/css">
                /* Define custom width and alignment of table columns */
            #treetable {
                table-layout: fixed;
            }
            #treetable tr td:nth-of-type(1) {
                text-align: right;
            }
            #treetable tr td:nth-of-type(2) {
                text-align: center;
            }
            #treetable tr td:nth-of-type(3) {
                min-width: 100px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
        </style>

        <!-- Add code to initialize the tree when the document is loaded: -->
        <script type="text/javascript">
            glyph_opts = {
                map: {
                    doc: "glyphicon glyphicon-file",
                    docOpen: "glyphicon glyphicon-file",
                    checkbox: "glyphicon glyphicon-unchecked",
                    checkboxSelected: "glyphicon glyphicon-check",
                    checkboxUnknown: "glyphicon glyphicon-share",
                    dragHelper: "glyphicon glyphicon-play",
                    dropMarker: "glyphicon glyphicon-arrow-right",
                    error: "glyphicon glyphicon-warning-sign",
                    expanderClosed: "glyphicon glyphicon-plus-sign",
                    expanderLazy: "glyphicon glyphicon-plus-sign",  // glyphicon-expand
                    expanderOpen: "glyphicon glyphicon-minus-sign",  // glyphicon-collapse-down
                    folder: "glyphicon glyphicon-folder-close",
                    folderOpen: "glyphicon glyphicon-folder-open",
                    loading: "glyphicon glyphicon-refresh"
                }
            };
            $(function(){
                // Initialize Fancytree
                $("#tree").fancytree({
                    extensions: [ "edit", "glyph", "wide"],
                    checkbox: false,
                    glyph: glyph_opts,
                    selectMode: 2,
                    source: {url: "?getTables", debugDelay: 1000},
                    toggleEffect: { effect: "drop", options: {direction: "down"}, duration: 400 },
                    wide: {
                        iconWidth: "1em",     // Adjust this if @fancy-icon-width != "16px"
                        iconSpacing: "0.5em", // Adjust this if @fancy-icon-spacing != "3px"
                        levelOfs: "1.5em"     // Adjust this if ul padding != "16px"
                    },

                    iconClass: function(event, data){
                        // if( data.node.isFolder() ) {
                        //   return "glyphicon glyphicon-book";
                        // }
                    },
                    lazyLoad: function(event, data) {
                        data.result = {url: "ajax-sub2.json", debugDelay: 1000};
                    },
                    activate: function(event, data) {
                        var node = data.node;
                        console.log(node.data.href);
                        if( node.data.href ){
                            window.open(node.data.href, node.data.target);

                        }
                    }
                });
            });
        </script>

        <div class="panel panel-default">
            <div class="panel-heading">
                <strong><fmt:message key="database/schema" />/<fmt:message key="table.entity" /></strong>
            </div>
            <div id="tree" class="panel-body fancytree-colorize-hover"></div>
            <div class="panel-footer"></div>
        </div>

        <stripes:form beanclass="com.manydesigns.portofino.actions.admin.database.TablesAction" method="post">
            <div class="form-group">
                <portofino:buttons list="tables-list" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>