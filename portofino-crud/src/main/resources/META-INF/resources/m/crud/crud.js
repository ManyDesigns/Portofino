var portofino = portofino || {};

portofino.dataTable = function(elem) {
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
            var url = href + additionalParameters;
            var datatableDescription = {
                url: url
            };
            $(document).trigger("portofino/datatable/load/started", [datatableDescription]);
            $.ajax(url, {
                dataType: "text",
                success: function(data, status, xhr) {
                    var newElem = $(data);
                    elem.replaceWith(newElem);
                    $(document).trigger("portofino/datatable/load/completed", [datatableDescription, newElem, status, xhr]);
                    setupDataTable(newElem);
                },
                error: function(xhr, status, errorThrown) {
                    if(xhr.status == 403) {
                        //Redirect to login page (link included in the response)
                        var loginUrl = xhr.responseText;
                        loginUrl = removeQueryStringArgument(loginUrl, "returnUrl");
                        window.location.href =
                                loginUrl + (loginUrl.indexOf("?") > -1 ? "&" : "?") + "returnUrl=" +
                                encodeURIComponent(window.location.href);
                    } else {
                        $(document).trigger("portofino/datatable/load/error", [datatableDescription, xhr, status, errorThrown]);
                    }
                }
            });
            return false;
        }
    }
    function setupDataTable(elem) {
        var linkLoaderFunction = makeLoaderFunction(elem);
        elem.find("a.paginator-link").click(linkLoaderFunction);
        elem.find("a.sort-link").click(linkLoaderFunction);
        elem.find("button[name=bulkDelete]").click(function() {
            return confirm (elem.find(".crud-confirm-bulk-delete").html());
        });
    }

    setupDataTable($(elem));
};

$(function() {
    //Enable AJAX paginators
    $(".portofino-datatable").each(function(index, elem) {
        portofino.dataTable(elem);
    });

    $(".crud-search-form").each(function(i, form) {
        form = $(form);
        form.find(".search_form_toggle_link").click(function() {
            var target = $(this);
            var visible = target.data("search-visible");
            target.next().slideToggle(300);
            target.data("search-visible", !visible);
            target.find("span").toggle();
            return false;
        });
    });
});