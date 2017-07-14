exports.setup = function(){
    //Inputs
    this.input('trigger');
    this.input('tags');
    this.input('index', {
        'type':'int', 
        'value':0
    });
    
    //Outputs
    this.output('accessor', {
        'type': 'string',
        'value': ''
    });
    this.output('translation');
    
    //Accessors
    var DummyHostLocationCode = getResource('VariantAccessors/DummyHostLocation.js', 2000);
    var DummyHostLocation = this.instantiateFromCode('DummyHostLocation', DummyHostLocationCode);

    var TagSelectorCode = getResource('VariantAccessors/TagSelector.js', 2000);
    var TagSelector = this.instantiateFromCode('TagSelector',TagSelectorCode);
    
    var TagURIBuilderCode = getResource('VariantAccessors/TagURIBuilder.js', 2000);
    var TagURIBuilder = this.instantiateFromCode('TagURIBuilder',TagURIBuilderCode);

    var DummyTagURIMapCode = getResource('VariantAccessors/DummyTagURIMap.js', 2000);
    var DummyTagURIMap = this.instantiateFromCode('DummyTagURIMap',DummyTagURIMapCode);
        
        
    //Connections
    this.connect('trigger', DummyHostLocation, 'trigger');
    this.connect('index', TagSelector, 'index');
    this.connect('tags', TagSelector, 'tags');
    
    this.connect(DummyHostLocation, 'location', TagURIBuilder, 'location');
    this.connect(TagSelector, 'tag', TagURIBuilder, 'tag');
    
    this.connect(TagURIBuilder, 'tagURI', DummyTagURIMap, 'tagURI');
    
    this.connect(TagSelector, 'translation', 'translation');
    this.connect(DummyTagURIMap, 'accessor', 'accessor');
    
}
