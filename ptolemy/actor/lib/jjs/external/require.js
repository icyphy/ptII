// Function that returns the requires() function.
// Implemented by Walter Higgins,
// found here: https://github.com/walterhiggins/commonjs-modules-javax-script.
// Modified by Edward A. Lee, eal@berkeley.edu.
(function ( rootDir, modulePaths, hooks ) {

  var File = java.io.File,
    FileReader = java.io.FileReader,
    BufferedReader = java.io.BufferedReader;
    
  var readModuleFromDirectory = function( dir ) {

    // look for a package.json file
    var pkgJsonFile = new File( dir, './package.json' );
    if ( pkgJsonFile.exists() ) {
    
      // Modified from original by eal@berkeley.edu because scload is not defined.
      // var pkg = scload( pkgJsonFile );
      var json = '';
      buffered = new BufferedReader(new FileReader(pkgJsonFile));
      while (( line = buffered.readLine()) !== null ) {
        json += line + '\n';
      }
      buffered.close(); // close the stream so there's no file locks
      var pkg = JSON.parse(json);
      // End of modified portion.
      
      var mainFile = new File( dir, pkg.main );
      if ( mainFile.exists() ) {
        return mainFile;
      } else {
        return null;
      }
    } else {
      // look for an index.js file
      var indexJsFile = new File( dir, './index.js' );
      if ( indexJsFile.exists() ) {
        return indexJsFile;
      } else { 
        return null;
      }
    }
  };

  var fileExists = function( file ) {
    if ( file.isDirectory() ) {
      return readModuleFromDirectory( file );
    } else {
      return file;
    }
  };

  var _canonize = function(file){ 
    return "" + file.canonicalPath.replaceAll("\\\\","/"); 
  };

  var resolveModuleToFile = function ( moduleName, parentDir ) {
    var file = new File(moduleName);

    if ( file.exists() ) {
      return fileExists(file);
    }
    if ( moduleName.match( /^[^\.\/]/ ) ) {
      // it's a module named like so ... 'events' , 'net/http'
      //
      var resolvedFile;
      for ( var i = 0; i < modulePaths.length; i++ ) {
        resolvedFile = new File( modulePaths[i] + moduleName );
        if ( resolvedFile.exists() ) {
          return fileExists( resolvedFile );
        } else {
          // try appending a .js to the end
          resolvedFile = new File( modulePaths[i] + moduleName + '.js' );
          if ( resolvedFile.exists() ) {
            return resolvedFile;
	  }
        }
      }
    } else {
      // it's of the form ./path
      file = new File( parentDir, moduleName );
      if ( file.exists() ) {
        return fileExists(file);
      } else { 
        // try appending a .js to the end
        var pathWithJSExt = file.canonicalPath + '.js';
        file = new File( parentDir, pathWithJSExt);
        if ( file.exists() ) {
          return file;
        } else {
          file = new File( pathWithJSExt );
          if ( file.exists() ) {
            return file;
	  }
        }
      }
    }
    return null;
  };

  var _loadedModules = {};
  var fmt = java.lang.String.format;
  /*
   require() function implementation
   */
  var _require = function( parentFile, path ) {
    var file,
	canonizedFilename,
	moduleInfo,
	buffered,
        head = '(function(exports,module,require,__filename,__dirname){ ',
	code = '',
	tail = '})',
	line = null;
    
    file = resolveModuleToFile(path, parentFile);
    if ( !file ) {
      var errMsg = '' + fmt("require() failed to find matching file for module '%s' " + 
                            "in working directory '%s' ", [path, parentFile.canonicalPath]);
      if ( ! ( (''+path).match( /^\./ ) ) ) {
        errMsg = errMsg + ' and not found in paths ' + JSON.stringify(modulePaths);
      }
      throw errMsg;
    }
    canonizedFilename = _canonize(file);
    
    moduleInfo = _loadedModules[canonizedFilename];
    if ( moduleInfo ) {
      return moduleInfo;
    }
    if ( hooks ) {
      hooks.loading( canonizedFilename );
    }
    buffered = new BufferedReader(new FileReader(file));
    while ( ( line = buffered.readLine()) !== null ) {
      code += line + '\n';
    }
    buffered.close(); // close the stream so there's no file locks

    moduleInfo = {
      loaded: false,
      id: canonizedFilename,
      exports: {},
      require: _requireClosure(file.parentFile)
    };
    code = head + code + tail;

    _loadedModules[canonizedFilename] = moduleInfo;
    var compiledWrapper = null;
    try {
      compiledWrapper = eval(code);
    } catch (e) {
      throw new Error( "Error evaluating module " + path
        + " line #" + e.lineNumber
        + " : " + e.message, canonizedFilename, e.lineNumber );
    }
    var __dirname = '' + file.parentFile.canonicalPath;
    var parameters = [
      moduleInfo.exports, /* exports */
      moduleInfo,         /* module */
      moduleInfo.require, /* require */
      canonizedFilename,  /* __filename */
      __dirname           /* __dirname */
    ];
    try {
      compiledWrapper
        .apply(moduleInfo.exports,  /* this */
               parameters);   
    } catch (e) {
      throw new Error( "Error executing module " + path
        + " line #" + e.lineNumber
        + " : " + e.message, canonizedFilename, e.lineNumber );
    }
    if ( hooks ) { 
      hooks.loaded( canonizedFilename );
    }
    moduleInfo.loaded = true;
    return moduleInfo;
  };

  var _requireClosure = function( parent ) {
    return function( path ) {
      var module = _require( parent, path );
      return module.exports;
    };
  };
  return _requireClosure( new java.io.File(rootDir) );
  // last line deliberately has no semicolon!
})
