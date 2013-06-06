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
        data[current.attr('name')] = current.attr('value');
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
            selectField.empty()

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
    dateField.datepicker({ format: dateFormat });
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
            element.ckeditor(conf);
        } else if(console && console.error) {
            console.error("CKEditor not loaded! Make sure that /elements/ckeditor/ckeditor.js and /elements/ckeditor/adapters/jquery.js are included in your page.");
        }
    });
}

$(function() {
    setupRichTextEditors();
});

function configureBulkEditTextField(id, checkboxName) {
    $("#" + id).focusin(function() {
        $("input[name=" + checkboxName + "]").prop("checked", true);
    })
}

function configureBulkEditField(id, checkboxName) {
    $("#" + id).change(function() {
        if($(this).val()) {
            $("input[name=" + checkboxName + "]").prop("checked", true);
        }
    })
}

