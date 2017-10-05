"use strict";

exports.setup = function() {       
    this.output('data', {'type' : 'JSON'});
    
    var Hue = this.instantiate('Hue', 'devices/Hue'); 
    Hue.input('bridgeIP', {
        /* The GenerateHueAccessors.xml model substitutes in the userName here.*/
        'value': '@bridgeIP@'
    });
    
    /* The GenerateHueAccessors.xml model substitutes in the userName here.*/
    var userName = '@userName@';

    Hue.setParameter('userName', userName);

    var code = "\
       exports.setup = function() {\n\
           this.input('in');\n\
           this.output('out');\n\
    }\n\
    exports.initialize = function() {\n\
           var thiz = this;\n\
           this.addInputHandler('in', function() {\n\
               var inValue = thiz.get('in');\n\
               thiz.send('out', JSON.parse(inValue));\n\
           });\n\
    }";
    var JavaScript = this.instantiateFromCode('JavaScript', code, false);

    this.connect(JavaScript, 'out', 'data');
    this.connect(Hue, 'received', JavaScript, 'in');
};
