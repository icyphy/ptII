//This is a dummy accessor (it already contains all the information it
//"accesses") that is supposed to be a placeholder for a location accessor
//like RedPin that works at the room level. This accessor is supposed to
//output the room depicted in the AR image 


//Name of the spatial ontology used in this model
var spatialOntology = 'ARDemo';

var location = spatialOntology + "://" + 'Berkeley/CoryHall/DOPCenter';

exports.setup = function() {
    //Inputs
    this.input('trigger');
    
    //Outputs
    this.output('location', {
        'type': 'string'
    });
}

exports.initialize = function() {
    var thiz = this;
    
    this.addInputHandler('trigger' , function () {
        thiz.send( 'location', location);
    });
}
