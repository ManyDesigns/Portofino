var portofino = portofino || {};

portofino.dataTable = function(elem) {
    elem = $(elem);

    function makeLoaderFunction() {
        return function loadLinkHref() {
            var href = $(this).attr("href");
            load(href);
            return false;
        }
    }
    function onDelete() {
        return confirm (elem.find(".crud-confirm-bulk-delete").html());
    }

    function setupDataTable() {
        var linkLoaderFunction = makeLoaderFunction(elem);
        elem.find("a.paginator-link").click(linkLoaderFunction);
        elem.find("a.sort-link").click(linkLoaderFunction);

        $('button[name=bulkDelete]:not(.bound)').addClass('bound').on('click',  onDelete);
    }

    setupDataTable();

    function load(href) {
        var eventName = elem.find("input[name=eventName]").val();
        if(eventName && !(eventName.length == 0)) {
            href = portofino.util.removeQueryStringArgument(href, eventName);
        }
        var additionalParameters = (href.indexOf("?") > -1 ? "&" : "?") + "getSearchResultsPage=";
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
                elem = newElem;
                $(document).trigger("portofino/datatable/load/completed", [datatableDescription, elem, status, xhr]);
                setupDataTable();
            },
            error: function(xhr, status, errorThrown) {
                if(xhr.status == 401) {
                    portofino.redirectToLogin(xhr);
                } else {
                    $(document).trigger("portofino/datatable/load/error", [datatableDescription, xhr, status, errorThrown]);
                }
            }
        });
    }

    this.load = load;

    return this;
};

$(function() {
    //Enable AJAX paginators
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

        var dataTable = new portofino.dataTable(form.find(".portofino-datatable"));

        function replaceQueryParam(param, newval, search) {
            var regex = new RegExp("([?;&])" + param + "[^&;]*[;&]?");
            var query = search.replace(regex, "$1").replace(/&$/, '');

            return (query.length > 2 ? query + "&" : "?") + (newval ? param + "=" + newval : '');
        }

        function search() {
            var href = form.attr("action");
            form.find("input[name=firstResult]").val("");
            form.find("input[name=searchString]").val("");
            var searchString = form.serialize();
            dataTable.load(href + "?" + searchString);
        }

        form.find("button[name=search]").click(function() {
            search();
            return false;
        });
        form.find("button[name=resetSearch]").click(function() {
            form[0].reset();
            form.find("input[name=sortProperty]").val("");
            form.find("input[name=sortDirection]").val("");
            search();
            return false;
        });
    });
});