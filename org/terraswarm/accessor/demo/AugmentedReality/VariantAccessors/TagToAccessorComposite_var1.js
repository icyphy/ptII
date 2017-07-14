exports.setup = function(){
    
    //Inputs
    this.input('trigger');
    this.input('tags');
    this.input('index', {
        'type':'int', 
        'value':0
    });
    
    //Outputs
    this.output('translation');
    this.output('accessor', {
        'type': 'string',
        'value': ''
    });
    
    //Accessors
    var DummyTagMapAccessorCode = getResource('VariantAccessors/DummyTagMapAccessor.js', 2000);
    var DummyTagMapAccessor = this.instantiateFromCode('DummyTagMapAccessor', DummyTagMapAccessorCode);
    
    var TagToAccessorVariant1Code = getResource('VariantAccessors/TagToAccessorVariant1.js', 2000);
    var TagToAccessorVariant1 = this.instantiateFromCode('TagToAccessorVariant1', TagToAccessorVariant1Code);

    //Connections
    this.connect('trigger', DummyTagMapAccessor, 'trigger');
    
    this.connect('tags', TagToAccessorVariant1, 'tags');
    this.connect('index', TagToAccessorVariant1, 'index');
    
    this.connect(DummyTagMapAccessor, 'IdToResource', TagToAccessorVariant1, 'IdToResource');
    
    this.connect(TagToAccessorVariant1, 'translation', 'translation');
    this.connect(TagToAccessorVariant1, 'accessor', 'accessor');
};
