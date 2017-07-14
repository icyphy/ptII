var httpClient = require('@accessors-modules/http-client');

var IdToResource = null;

exports.setup = function() {
    // Inputs
    this.input('tags');
    this.input('IdToResource');
    this.input('index', {
        'type':'int', 
        'value':0
    });
    
    // Outputs
    this.output('accessor', {
        'type': 'string',
        'value': ''
    });
    this.output('translation');
}

exports.initialize = function() {
    var thiz = this;
    var previousIndex = -1;
    
    this.addInputHandler('IdToResource', function() {
        IdToResource = thiz.get('IdToResource');
    });
    
    this.addInputHandler('tags', function() {
        var tags = thiz.get('tags');
        var index = thiz.get('index');
        
        if (index != previousIndex) {
            // Index has changed. Look up an accessor and produce it.
            if (IdToResource && tags && tags.length > index) {
                var id = tags[index].id;
                // FIXME: Change getResource API.
                var accessor = getResource(IdToResource[id], 2000);
                if (accessor) {
                    thiz.send('accessor', accessor);
                }
            }
            previousIndex = index;
        }
        thiz.send('translation', tags[index].center);
    });
}

