<%@ page import="com.manydesigns.elements.servlet.ServletConstants"%><%@page contentType="text/javascript; UTF-8"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%
    // Avoid caching of dynamic pages
    response.setHeader(ServletConstants.HTTP_PRAGMA, ServletConstants.HTTP_PRAGMA_NO_CACHE);
    response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_MUST_REVALIDATE);
    response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_NO_CACHE);
    response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_NO_STORE);
    response.setDateHeader(ServletConstants.HTTP_EXPIRES, 0);
%>
var portofino = {
    _setupRichTextEditors: setupRichTextEditors,

    setupRichTextEditors: function(config) {
        config = config || {};
        var windowWidth, windowHeight;
        if (window.innerWidth && window.innerHeight) {
            windowWidth = window.innerWidth;
            windowHeight = window.innerHeight;
        } else if (document.compatMode=='CSS1Compat' &&
            document.documentElement &&
            document.documentElement.offsetWidth ) {
            windowWidth = document.documentElement.offsetWidth;
            windowHeight = document.documentElement.offsetHeight;
        } else if (document.body && document.body.offsetWidth) {
            windowWidth = document.body.offsetWidth;
            windowHeight = document.body.offsetHeight;
        }

        var baseConfig = {};
        if(windowHeight) {
            baseConfig.height =
                    windowHeight -
                    $("textarea.mde-form-rich-text").offset().top -
                    $("#ft").height() -
                    $(".contentFooter").height() -
                    350; //350 ~= toolbar 3 righe + footer + margine tolleranza
        }

        config = $.extend(baseConfig, {
            customConfig : '<c:out value="${pageContext.request.contextPath}"/>/ckeditor-custom/config.js',
            toolbar: 'PortofinoDefault',
            toolbarCanCollapse: false,
            filebrowserWindowWidth : windowWidth,
            filebrowserWindowHeight : windowHeight
        }, config);

        $('textarea.mde-form-rich-text').data('mdeRichTextConfig', config);
        portofino._setupRichTextEditors();
    },

    copyFormAsHiddenFields: function(source, form) {
        source.find("input, select").each(function(index, elem) {
            elem = $(elem);
            var hiddenField = document.createElement("input");
            hiddenField.setAttribute("type", "hidden");
            hiddenField.setAttribute("name", elem.attr('name'));
            hiddenField.setAttribute("value", elem.val());
            form.append(hiddenField);
        });
    },

    enablePortletDragAndDrop: function(button, originalPath) {
        $("div.pageActionContainer").sortable({
            connectWith: "div.pageActionContainer",
            placeholder: "pageActionPlaceholder",
            cursor: "move", // cursor image
            revert: true, // moves the portlet to its new position with a smooth transition
            tolerance: "pointer" // mouse pointer overlaps the droppable
        }).disableSelection().addClass("portletBox");

        var container = $("#content");
        container.prepend('\
            <form action="${pageContext.request.contextPath}/actions/admin/page" method="post">\
                Edit page layout: \
                <input type="hidden" name="originalPath" value="' + originalPath + '" />\
                <button name="updateLayout" type="submit" class="btn btn-primary">Save</button>\
                <button name="cancel" type="submit" class="btn btn-default">Cancel</button>\
            </form>');
        $("button[name=updateLayout]").click(function() {
            var theButton = $(this);
            $('div.pageActionContainer').each(function(index, element) {
                var wrapper = $(element);
                var templateHiddenField = wrapper.children("input[type=hidden]").first();
                var elements = wrapper.sortable('toArray');
                for(var e in elements) {
                    var id = elements[e];
                    var hiddenField = document.createElement("input");
                    hiddenField.setAttribute("type", "hidden");
                    hiddenField.setAttribute("name", templateHiddenField.val());
                    hiddenField.setAttribute("value", id.substring("embeddedPageActionWrapper_".length));
                    theButton.before(hiddenField);
                }
            });
            return true;
        });
        button.off("click");
    },

    confirmDeletePage: function(pagePath, contextPath) {
        var dialogDiv = $("<div></div>").appendTo($("body"));
        dialogDiv.load(contextPath + "/actions/admin/page?confirmDelete&originalPath=" + pagePath, function() {
            var dialog = dialogDiv.find("#dialog-confirm-delete-page");
            dialog.modal({ backdrop: 'static'});
        });
    },

    showMovePageDialog: function(pagePath, contextPath) {
        var dialogDiv = $("<div></div>").appendTo($("body"));
        dialogDiv.load(contextPath + "/actions/admin/page?chooseNewLocation&originalPath=" + pagePath, function() {
            var dialog = dialogDiv.find("#dialog-move-page");
            dialog.modal({ backdrop: 'static'});
        });
    },

    showCopyPageDialog: function(pagePath, contextPath) {
        var dialogDiv = $("<div></div>").appendTo($("body"));
        dialogDiv.load(contextPath + "/actions/admin/page?copyPageDialog&originalPath=" + pagePath, function() {
            var dialog = dialogDiv.find("#dialog-copy-page");
            dialog.modal({ backdrop: 'static'});
        });
    },

    dataTable: function(elem) {
        function removeQueryStringArgument(href, arg) {
            href = href.replace(new RegExp("[?]" + arg + "=[^&]*&", "g"), "?");
            href = href.replace(new RegExp("[?]" + arg + "=[^&]*", "g"), "");
            href = href.replace(new RegExp("[&]" + arg + "=[^&]*&", "g"), "&");
            href = href.replace(new RegExp("[&]" + arg + "=[^&]*", "g"), "");
            return href;
        }

        function makeLoaderFunction(elem) {
            return function loadLinkHref() {
                var href = $(this).attr("href");
                var eventName = elem.find("input[name=eventName]").val();
                if(eventName && !(eventName.length == 0)) {
                    href = removeQueryStringArgument(href, eventName);
                }
                href = removeQueryStringArgument(href, "ajax");
                var additionalParameters = (href.indexOf("?") > -1 ? "&" : "?") + "getSearchResultsPage=&ajax=true";
                $.ajax(href + additionalParameters, {
                    dataType: "text",
                    success: function(data, status, xhr) {
                        var targetId = "#" + elem.attr("id");
                        elem.replaceWith(data);
                        var target = $(targetId);
                        setupDataTable(target);
                    },
                    error: function(xhr, status, errorThrown) {
                        if(xhr.status == 403) {
                            //Redirect to login page (link included in the response)
                            var loginUrl = xhr.responseText;
                            loginUrl = removeQueryStringArgument(loginUrl, "returnUrl");
                            loginUrl = removeQueryStringArgument(loginUrl, "cancelReturnUrl");
                            window.location.href =
                                    loginUrl + (loginUrl.indexOf("?") > -1 ? "&" : "?") + "returnUrl=" +
                                    encodeURIComponent(window.location.href);
                        } else {
                            //TODO
                            alert("There was an error fetching the requested data");
                        }
                    }
                });
                return false;
            }
        }
        function setupDataTable(elem) {
            elem.find("a.paginator-link").click(makeLoaderFunction(elem));
            elem.find("a.sort-link").click(makeLoaderFunction(elem));
        }

        setupDataTable($(elem));
    }
};

setupRichTextEditors = function() {/* Do nothing (remove default initialization by Elements) */};

var HTML_CHARS = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#x27;',
    '/': '&#x2F;',
    '`': '&#x60;'
};

function htmlEscape (string) {
    if(string == null) {
        return string;
    }
    return (string + '').replace(/[&<>"'\/`]/g, function (match) {
        return HTML_CHARS[match];
    });
}

$(function() {
    //Enable AJAX paginators
    $(".portofino-datatable").each(function(index, elem) {
        portofino.dataTable(elem);
    });

    //Prevent double submit
    $('button.dont-prevent-double-submit').cli
    $('form').on('submit', function() {
        var form = $(this);
        var buttons = form.find(":submit");
        buttons.each(function(index, current) {
            var button = $(current);
            var clone = button.clone();
            clone.removeAttr("name");
            clone.attr("disabled", "disabled");
            button.css("display", "none");
            button.after(clone);
            button.appendTo(form);
        });
    });
});