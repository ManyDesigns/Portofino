function getDetails (tableName) {
    getColumns(tableName);
    getPk(tableName);
    getFk(tableName);
    getAnnotations(tableName);
}
function getColumns (tableName) {
    $("div#tabs-1").html('<h2>'+tableName+'</h2><table id="colDetails"/>');
    $.getJSON("./model-definition.action?getJsonColumns&tableName="+tableName,{},
      function(data) {
        $('table#colDetails').append('<tr><th>Name</th><th>Type</th><th>Java Type</th></tr>');
        $.each(data.columns, function(i,item){
            $('table#colDetails').append('<tr><td>'+item.name+'</td><td>'+item.colType+'</td><td>'+item.javaType+'</td></tr>');
        });
      }
    );
}

function getPk (tableName) {

    $.getJSON("./model-definition.action?getJsonPk&tableName="+tableName,{},
      function(data) {
        $("div#tabs-2").html('<h2>'+data.pkName+'</h2><table id="pkDetails"/>');
        $('table#pkDetails').append('<tr><th>Columns</th></tr>');
        $.each(data.columns, function(i,item){
            $('table#pkDetails').append('<tr><td>'+item.name+'</td></tr>');
        });
      }
    );
}

function getFk (tableName) {
    $("div#tabs-3").html("");
    $.getJSON("./model-definition.action?getJsonFk&tableName="+tableName,{},
      function(data) {

        $.each(data.fks, function(i,item){
            $("div#tabs-3").append('<h2>'+item.fkName+'</h2><table id="fkDetails"/>');
            $('table#fkDetails').append('<tr><th>To Table</th><th>To Column</th><th>From Column</th></tr>');
            $.each(item.refs, function(j,ref){
                $('table#fkDetails').append('<tr><td>'+ref.toTable+'</td><td>'+ref.to+'</td><td>'+ref.from+'</td></tr>');

            });

        });
      }
    );
}

function getAnnotations (tableName) {
    $("div#tabs-4").html('<h2>'+tableName+'</h2><table id="annDetails"/>');
    $.getJSON("./model-definition.action?getJsonAnnotations&tableName="+tableName,{},
      function(data) {
        $('table#annDetails').append('<tr><th>Name</th><th>Values</th></tr>');
        var nodata = true;
        $.each(data.annotations, function(i,item){
            nodata = false;
            $('table#annDetails').append('<tr><td>'+item.name+'</td><td>'+item.values+'</td></tr>');
        });
        if(nodata){
            $('table#annDetails').append('<tr><td colspan="2">No data</td></tr>');
        }
      }
    );
}