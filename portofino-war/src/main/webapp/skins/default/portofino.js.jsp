<%@ page import="com.manydesigns.elements.servlet.ServletConstants"%><%@page contentType="text/javascript; UTF-8"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%
    // Avoid caching of dynamic pages
    response.setHeader(ServletConstants.HTTP_PRAGMA, ServletConstants.HTTP_PRAGMA_NO_CACHE);
    response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_MUST_REVALIDATE);
    response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_NO_CACHE);
    response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_NO_STORE);
    response.setDateHeader(ServletConstants.HTTP_EXPIRES, 0);
%>
function copyFormAsHiddenFields(source, form) {
    source.find("input, select").each(function(index, elem) {
        elem = $(elem);
        var hiddenField = document.createElement("input");
        hiddenField.setAttribute("type", "hidden");
        hiddenField.setAttribute("name", elem.attr('name'));
        hiddenField.setAttribute("value", elem.val());
        form.append(hiddenField);
    });
}

function confirmDeletePage(pagePath, contextPath) {
    var dialogDiv = $("<div></div>").appendTo($("body"));
    dialogDiv.load(contextPath + "/actions/admin/page?confirmDelete&originalPath=" + pagePath, function() {
        var dialog = dialogDiv.find("#dialog-confirm-delete-page");
        dialog.modal({ backdrop: 'static'});
    });
    return false;
}

function showMovePageDialog(pagePath, contextPath) {
    var dialogDiv = $("<div></div>").appendTo($("body"));
    dialogDiv.load(contextPath + "/actions/admin/page?chooseNewLocation&originalPath=" + pagePath, function() {
        var dialog = dialogDiv.find("#dialog-move-page");
        dialog.modal({ backdrop: 'static'});
    });
    return false;
}

function showCopyPageDialog(pagePath, contextPath) {
    var dialogDiv = $("<div></div>").appendTo($("body"));
    dialogDiv.load(contextPath + "/actions/admin/page?copyPageDialog&originalPath=" + pagePath, function() {
        var dialog = dialogDiv.find("#dialog-copy-page");
        dialog.modal({ backdrop: 'static'});
    });
    return false;
}

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
                    250; //250 ~= toolbar 3 righe + footer + margine tolleranza
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
    }
};

setupRichTextEditors = function() {/* Do nothing (remove default initialization by Elements) */};

function enablePortletDragAndDrop(button) {
    $("div.portletContainer").sortable({
        connectWith: "div.portletContainer",
        placeholder: "portletPlaceholder",
        cursor: "move", // cursor image
        revert: true, // moves the portlet to its new position with a smooth transition
        tolerance: "pointer" // mouse pointer overlaps the droppable
    }).disableSelection().addClass("portletBox");

    var container = $(button).parent();
    $(button).remove();
    container.prepend('<button name="cancel" type="submit" class="contentButton">Cancel</button> ');
    container.prepend('<button name="updateLayout" type="submit" class="contentButton">Save</button>');
    //container.children("button[name=cancel]").button();
    //container.children("button[name=updateLayout]").button();
    $("button[name=updateLayout]").click(function() {
        var theButton = $(this);
        $('div.portletContainer').each(function(index, element) {
            var wrapper = $(element);
            var templateHiddenField = wrapper.children("input[type=hidden]").first();
            var elements = wrapper.sortable('toArray');
            for(var e in elements) {
                var id = elements[e];
                var hiddenField = document.createElement("input");
                hiddenField.setAttribute("type", "hidden");
                hiddenField.setAttribute("name", templateHiddenField.val());
                hiddenField.setAttribute("value", id.substring("portletWrapper_".length));
                theButton.before(hiddenField);
            }
        });
        return true;
    });

}


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
        function makeLoaderFunction(elem) {
            return function loadLinkHref() {
                var href = $(this).attr("href");
                var eventName = elem.find("input[name=eventName]").val();
                if(eventName && !(eventName.length == 0)) {
                    console.log(eventName);
                    console.log(href);
                    href = href.replace(eventName + "=&", "");
                    href = href.replace(eventName + "=", "");
                    console.log(href);
                }
                $.ajax(href + "&getSearchResultsPage=", {
                    dataType: "html",
                    success: function(data, status, xhr) {
                        var targetId = "#" + elem.attr("id");
                        elem.replaceWith(data);
                        var target = $(targetId);
                        setupLinks(target);
                    },
                    error: function(xhr, status, errorThrown) {
                        alert("There was an error fetching the requested data")
                    }
                });
                return false;
            }
        }
        function setupLinks(elem) {
            elem.find("a.paginator-link").click(makeLoaderFunction(elem));
            elem.find("a.sort-link").click(makeLoaderFunction(elem));
        }

        setupLinks($(elem));
    });
});