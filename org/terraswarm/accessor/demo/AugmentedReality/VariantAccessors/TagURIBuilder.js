//One-off javascript accessor for constructing a tag URI. Once a location
//has been given as input it will begin constructing tagURIs. Before then
//this accessor will not react to tag inputs.

var location = null;


exports.setup = function() {
    //Inputs
    this.input('location', {
        "type" : "string"
    });
    this.input('tag', {
        "type" : "int"
    });
    
    //Outputs
    this.output('tagURI', {
        "type" : "string",
        "value" : ""
    });
}

exports.initialize = function() {
    var thiz = this;
    
    this.addInputHandler('location' , function () {
        location = thiz.get('location');
    });
    
    this.addInputHandler('tag' , function () {
        if(location){
            var uri = location + "/" + thiz.get('tag');
            thiz.send( 'tagURI', uri);
        }
    });
}