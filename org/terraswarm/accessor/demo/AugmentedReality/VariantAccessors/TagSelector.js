var httpClient = require('@accessors-modules/http-client');

var IdToResource = null;

exports.setup = function() {
    // Inputs
    this.input('tags');
    this.input('index', {
        'type':'int', 
        'value':0
    });
    
    // Outputs
    this.output('tag', {
        'type': 'int'
    });
    this.output('translation');
}

exports.initialize = function() {
    var thiz = this;
    var previousIndex = -1;
    
    
    //Assume both the tags and index inputs always arrive simultaneously
    this.addInputHandler('tags', function() {
        var tags = thiz.get('tags');
        var index = thiz.get('index');
        
        if (index != previousIndex) {
            // Index has changed. Look up a new tag.
            if (tags && tags.length > index) {
                thiz.send('tag', tags[index].id);
            }
            previousIndex = index;
        }
        thiz.send('translation', tags[index].center);
    });
}

