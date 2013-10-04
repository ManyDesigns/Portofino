//Ex elements.js
function updateSelectOptions(relName, selectionProviderIndex, methodName) {
    var selectFieldId = arguments[3 + selectionProviderIndex];

    var data = {
        relName : relName,
        selectionProviderIndex : selectionProviderIndex
    };
    data[methodName] = '';
    for (var i = 3; i < arguments.length; i++ ) {
        var currentId = arguments[i];
        var current = $(currentId);
        data[current.attr('name')] = current.val();
    }

    var postUrl = stripQueryString(location.href);
    jQuery.ajax({
        type: 'POST',
        url: postUrl,
        data: data,
        success: function(responseData) {
            var options = responseData;
            if('string' === typeof(options)) {
                options = jQuery.parseJSON(options);
            }

            var selectField = $(selectFieldId);
            selectField.empty();

            for (var i = 0; i < options.length; i++) {
                var option = options[i];
                var y = document.createElement('option');
                y.value = option['v'];
                y.text = option['l'];
                y.selected = option['s'];

                selectField.append(y)
            }
            selectField.change();
        }
    });
}

function setupAutocomplete(autocompleteId, relName, selectionProviderIndex, methodName) {
    var setupArguments = arguments;
    var selectFieldId = setupArguments[4 + selectionProviderIndex];
    var autocompleteObj = $(autocompleteId);
    var selectField = $(selectFieldId);
    autocompleteObj.change(function() {
        selectField.val(""); //Reset selected object when user types
    });
    autocompleteObj.typeahead({
        source: function( request, response ) {
            var data = {
                relName : relName,
                selectionProviderIndex : selectionProviderIndex,
                labelSearch : request
            };
            data[methodName] = '';
            for (var i = 4; i < setupArguments.length; i++ ) {
                var currentId = setupArguments[i];
                var current = $(currentId);
                data[current.attr('name')] = current.attr('value');
            }

            var postUrl = stripQueryString(location.href);
            $.ajax({
                type: 'POST',
                dataType: 'json',
                url: postUrl,
                data: data,
                success: function( responseData ) {
                    response( $.map( responseData, function( item ) {
                        var obj = {
                            label: item.l,
                            value: item.l,
                            optionValue: item.v
                        };
                        obj.toString = function() {
                            return JSON.stringify(obj);
                        };
                        return obj;
                    }));
                },
                error: function(request, textStatus) {
                    alert(textStatus);
                }
            });
        },
        minLength: 1,
        matcher: function(item) {
            return true;
        },
        sorter: function(items) {
            return items;
        },
        highlighter: function (item) {
            var query = this.query.replace(/[\-\[\]{}()*+?.,\\\^$|#\s]/g, '\\$&');
            return item.label.replace(new RegExp('(' + query + ')', 'ig'), function ($1, match) {
                return '<strong>' + match + '</strong>'
            });
        },
        updater: function(item) {
            if(item) {
                item = JSON.parse(item);
                selectField.val(item.optionValue);
                return item.label;
            } else {
                selectField.val("");
                return "";
            }
        }
    });
}

function setupDatePicker(dateFieldId, dateFormat) {
    var dateField = $(dateFieldId);
    dateField.datepicker({ format: dateFormat, autoclose: true });
}

function stripQueryString(url) {
    var regexp = new RegExp("\\?.*");
    return url.replace(regexp, "");
}

function setupRichTextEditors() {
    $('textarea.mde-form-rich-text').each(function(index, element) {
        element = $(element);
        var conf = element.data('mdeRichTextConfig') || {
            toolbar: 'Full',
            toolbarCanCollapse: false
        };
        if(element.ckeditor) {
            element.ckeditor(function() {
                var elementId = element.attr("id");
                var editor = this;
                if(elementId) {
                    $("label[for=" + elementId + "]").click(function() {
                        editor.focus();
                    })
                }
            }, conf);
        } else if(console && console.error) {
            console.error("CKEditor not loaded! Make sure that /theme/ckeditor/ckeditor.js and /theme/ckeditor/adapters/jquery.js are included in your page.");
        }
    });
}

$(function() {
    setupRichTextEditors();
});

function configureBulkEditTextField(id, checkboxName) {
    $("#" + id).keypress(function(event) {
        var keyCode = event.keyCode || event.which;
        if(keyCode != 9) { //9 = Tab
            $("input[name=" + checkboxName + "]").prop("checked", true);
        }
    });
}

function configureBulkEditDateField(id, checkboxName) {
    configureBulkEditTextField(id, checkboxName);
    configureBulkEditField(id, checkboxName);
}

function configureBulkEditField(id, checkboxName) {
    $("#" + id).change(function() {
        if($(this).val()) {
            $("input[name=" + checkboxName + "]").prop("checked", true);
        }
    });
}

var portofino = {
    _setupRichTextEditors: setupRichTextEditors,

    contextPath: '',

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
            customConfig : portofino.contextPath + '/m/pageactions/ckeditor-custom/config.js',
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

    enablePageActionDragAndDrop: function(button, originalPath) {
        $("div.pageActionContainer").sortable({
            connectWith: "div.pageActionContainer",
            placeholder: "pageActionPlaceholder",
            cursor: "move", // cursor image
            revert: true, // moves the portlet to its new position with a smooth transition
            tolerance: "pointer" // mouse pointer overlaps the droppable
        }).disableSelection().addClass("portletBox");

        var container = $("#content");
        container.prepend('\
            <form action="' + portofino.contextPath + '/actions/admin/page" method="post">\
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

    $('form').on('submit', function() {
        //Prevent double submit
        var form = $(this);
        var buttons = form.find(":submit");
        var undo = new Array();
        buttons.each(function(index, current) {
            var button = $(current);
            var clone = button.clone();
            var display = button.css("display");
            clone.removeAttr("name");
            clone.attr("disabled", "disabled");
            button.css("display", "none");
            button.after(clone);
            button.appendTo(form);
            undo.push({ button: button, clone: clone, display: display });
        });
        //Restore the buttons after 10s...
        var timeout = setTimeout(function() {
            undo.forEach(function(obj) {
                obj.clone.before(obj.button);
                obj.clone.remove();
                obj.button.css("display", obj.display);
            });
        }, 10000);
        //...unless we have requested another page (note: beforeunload is triggered even if the page doesn't change,
        //e.g. when downloading a pdf export)
        $(window).unload(function() {
            clearTimeout(timeout);
        });

        //Page abandon
        $(this).data("dirty", false);
    });

    //Page abandon
    $(':input').on("change", function() {
        $(this).closest("form").data("dirty", true);
    });

    $(window).on("beforeunload", function() {
        var dirty = false;
        $("form:not(.dont-prompt-on-page-abandon)").each(function(index, form) {
            if($(form).data("dirty")) {
                dirty = true;
            }
        });
        return dirty ? "Are you sure you want to leave the page? Unsaved data will be lost." : null; //TODO I18n
    });
});