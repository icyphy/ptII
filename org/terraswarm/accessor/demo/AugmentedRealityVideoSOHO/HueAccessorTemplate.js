"use strict";

exports.setup = function() {       
    console.log('HueAccessorTemplate: setup(): start');
    this.output('data', {'type' : 'JSON'});
    
    var Hue = this.instantiate('Hue', 'devices/Hue'); 

    /* The GenerateHueAccessors.xml model substitutes in the bridgeIP here.*/
    Hue.setParameter('bridgeIP','@bridgeIP@');

    /* The GenerateHueAccessors.xml model substitutes in the userName here.*/
    Hue.setParameter('userName', '@userName@');

    var code = "\
       exports.setup = function() {\n\
           this.input('in');\n\
           this.output('out');\n\
    }\n\
    exports.initialize = function() {\n\
           var thiz = this;\n\
           this.addInputHandler('in', function() {\n\
               /* var inValue = thiz.get('in'); */\n\
               /* FIXME: Need to get real data from the Hue. */\n\
               var inValue = '{ \"bridgeIP\": \"@bridgeIP@\", \"id\": \"@id@\", \"userName\": \"@userName@\" }';\n\
               console.log('HueAccessorTemplate: inputHandler()');\n\
               thiz.send('out', JSON.parse(inValue));\n\
           });\n\
    }";
    var JavaScript = this.instantiateFromCode('JavaScript', code, false);

    this.connect(JavaScript, 'out', 'data');
    this.connect(Hue, 'lights', JavaScript, 'in');
    console.log('HueAccessorTemplate: setup(): end');
};
