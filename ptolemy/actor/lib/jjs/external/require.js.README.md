commonjs-modules-javax-script
=============================

A simple implementation of require() for use in Java's embedded Javascript engine (JRE 6,7 and 8)

## Goal

To provide a `require()` function which supports [CommonJS][cjsmodules] style
modules in the Javascript engine bundled with Java 6, 7 and 8. This is not meant to be a full
port of commonjs or node modules, this is just the `require()` function. 

## require - CommonJS-style module loading in Java 6,7 and 8's Javascript Engine.

Node.js is a server-side javascript environment with an excellent
module loading system based on CommonJS. Modules in Node.js are really
simple. Each module is in its own javascript file and all variables
and functions within the file are private to that file/module only.
There is a very concise explanation of CommonJS modules at...

[http://wiki.commonjs.org/wiki/Modules/1.1.1.][cjsmodules]

Node.js also has good documentation on [Modules][njsmod].

If you want to export a variable or function you use the module.export
property.

For example imagine you have 3 files program.js, inc.js  and math.js ...

### math.js

    exports.add = function(a,b){
        return a + b;
    }

### inc.js

    var math = require('./math');
    exports.increment = function(n){
        return math.add(n, 1);
    }

### program.js

    var inc = require('./inc').increment;
    var a = 7;
    a = inc(a);
    print(a);

You can see from the above sample code that programs can use modules
and modules themeselves can use other modules. Modules have full
control over what functions and properties they want to provide to
others.

[cjsmodules]: http://wiki.commonjs.org/wiki/Modules/1.1.1.

## Bootstrapping 

To create the `require` function you must first load and evaluate the `require.js` source file. Since you can't `require('require')` use the native (or custom) `load()` function instead to first load `require.js`.

 * On JDK 8's Javascript Engine (Nashorn) there is a native `load()` function which can be used...

```javascript
var Require = load('./require.js');
var require = Require( './' , [ 'libpath1', 'libpath2' ] );
// now you can use require to load commonjs/node style modules
var myModule = require('./mymodule');
```
        
 * On JDK 6 and 7 there is no native `load()` function so define one like this...

```javascript
var load = function( path ) { 
  var line = null,
      reader = null
      contents = '';
  reader = new java.io.BufferedReader( new java.io.FileReader( new java.io.File( path ) ) );
  while ( ( line = reader.readLine() ) != null ) {
      contents += line + '\n'
  }
  return eval( '(' + contents + ')' );
}
```
   ..then bootstrap require like this...

```javascript
var Require = load('./require.js');
var require = Require( new java.io.File( './' ), [ 'libpath1', 'libpath2' ] );
// now you can use require to load commonjs/node style modules
var myModule = require('./mymodule');
```

### module name resolution

When resolving module names to file paths, use the following rules...

 1. if the module does not begin with './' or '/' then ...

    1.1 Look in any of the supplied library paths. If it's not there then throw an error.

 2. If the module begins with './' or '/' then ...
    
    2.1 if the module begins with './' then it's treated as a file path. File paths are 
        always relative to the module from which the require() call is being made.

    2.2 If the module begins with '/' then it's treated as an absolute path.

    If the module does not have a '.js' suffix, and a file with the same name and a .js sufix exists, 
    then the file will be loaded.

 3. If the module name resolves to a directory then...
    
    3.1 look for a package.json file in the directory and load the `main` property e.g.

        // package.json located in './some-library/'
        {
          "main": './some-lib.js',
          "name": 'some-library'
        }
    
    3.2 if no package.json file exists then look for an index.js file in the directory

## Caveats

 * This is not battle-hardened code.
 * I don't know what I'm doing.

