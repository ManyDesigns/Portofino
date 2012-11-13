CKEDITOR.editorConfig = function(config) {
    var toolbar = [];
    for(var x in config.toolbar_Full) {
        var y = config.toolbar_Full[x];
        if('document' == y.name) {
            toolbar.push({name:'document',items:['Source','-','Templates']});
        } else {
            toolbar.push(y);
        }
    }

    config.toolbar_PortofinoDefault = toolbar;
    config.toolbar = 'PortofinoDefault';
};