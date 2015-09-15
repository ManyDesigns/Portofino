<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
        %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
        %><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
        %><%@ taglib prefix="mde" uri="/manydesigns-elements"
        %><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
        %><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><stripes:layout-render name="/m/admin/admin-theme/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.database.TablesAction"/>
    <stripes:layout-component name="pageTitle"> Tables </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <link href="<stripes:url value="/webjars/fancytree/2.11.0/dist/skin-bootstrap/ui.fancytree.css"/>" rel="stylesheet" type="text/css" class="skinswitcher">

        <script src="<stripes:url value="/webjars/fancytree/2.11.0/dist/src/jquery.fancytree.js"/>"       type="text/javascript"></script>
        <!--<script src="<stripes:url value="/webjars/fancytree/2.11.0/dist/src/jquery.fancytree.dnd.js"/>"   type="text/javascript"></script>
        <script src="<stripes:url value="/webjars/fancytree/2.11.0/dist/src/jquery.fancytree.edit.js"/>"  type="text/javascript"></script>  -->
        <script src="<stripes:url value="/webjars/fancytree/2.11.0/dist/src/jquery.fancytree.glyph.js"/>" type="text/javascript"></script>
        <script src="<stripes:url value="/webjars/fancytree/2.11.0/dist/src/jquery.fancytree.filter.js"/>" type="text/javascript"></script>
        <script src="<stripes:url value="/webjars/fancytree/2.11.0/dist/src/jquery.fancytree.wide.js"/>"  type="text/javascript"></script>

        <style type="text/css">
            ul.fancytree-ext-wide { border: none; }
            ul.fancytree-container { border: none; }
        </style>

        <!-- Add code to initialize the tree when the document is loaded: -->
        <script type="text/javascript">
            glyph_opts = {
                map: {
                    doc: "glyphicon glyphicon-list-alt",
                    docOpen: "glyphicon glyphicon-list-alt",
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
                    extensions: [ "filter", "glyph"],
                    quicksearch: true,
                    checkbox: false,
                    glyph: glyph_opts,
                    selectMode: 2,
                    source: {url: "?getTables", debugDelay: 1000},
                    toggleEffect: { effect: "drop", options: {direction: "down"}, duration: 400 },
                    filter: {
                        autoApply: true,  // Re-apply last filter if lazy data is loaded
                        counter: false,  // Show a badge with number of matching child nodes near parent icons
                        fuzzy: false,  // Match single characters in order, e.g. 'fb' will match 'FooBar'
                        hideExpandedCounter: true,  // Hide counter badge, when parent is expanded
                        highlight: true,  // Highlight matches by wrapping inside <mark> tags
                        mode: "hide" , // Grayout unmatched nodes (pass "hide" to remove unmatched node instead)
                        leavesOnly:true
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
                        if( node.data.href!=null ){
                            window.location.href="<stripes:url value='${actionBean.actionPath}/' />"+node.data.href;
                        }
                    }
                });

                var tree = $("#tree").fancytree("getTree");
                $("input[name=search]").keyup(function(e){
                    var n,
                            opts = { autoExpand: true , leavesOnly: true },
                            match = $(this).val();

                    if(e && e.which === $.ui.keyCode.ESCAPE || $.trim(match) === ""){
                        $("span#matches").text("");
                        tree.clearFilter();
                        return;
                    }
                        // Pass a string to perform case insensitive matching
                        n = tree.filterNodes(match, opts);

                    $("button#btnResetSearch").attr("disabled", false);
                    $("span#matches").text("(" + n + " matches)");
                }).focus();
            });
        </script>

        <div class="panel panel-default">
            <div class="panel-heading">
                <strong><fmt:message key="database/schema" />/<fmt:message key="table.entity" /></strong>

                    <label>Filter:</label>
                    <input class="form-control inpu-sm" name="search" placeholder="Filter..." autocomplete="off">

            </div>
            <div id="tree" class="panel-body fancytree-colorize-hover"></div>
            <div class="panel-footer">
                <span class="badge" id="matches"></span>
            </div>
        </div>

        <stripes:form beanclass="com.manydesigns.portofino.actions.admin.database.TablesAction" method="post">
            <div class="form-group">
                <portofino:buttons list="tables-list" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>