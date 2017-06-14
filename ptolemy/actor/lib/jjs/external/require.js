// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2014-2016 The Regents of the University of California.
// All rights reserved.
//
// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.
//
// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.
//
//
// Ptolemy II includes the work of others, to see those copyrights, follow
// the copyright link on the splash page or see copyright.htm.
// Function that returns the requires() function.
// Implemented by Walter Higgins,
// found here: https://github.com/walterhiggins/commonjs-modules-javax-script.
// Modified by Edward A. Lee, eal@berkeley.edu.
//
// FIXME: This implementation will reload a module each time require() is called.
// It should make a record of the module and its version number and/or file update
// time and reload only if the previously loaded version doesn't match.
//
// When this file is evaluated with load(), load() returns the following
// function, which takes three arguments, a root directory, the specification
// of the module (a module name, file name, or path), and an optional hooks
// object that provides two functions, hooks.loading(), which reports that a
// module is loading, and hooks.loaded(), which reports that the module has been
// loaded.  Evaluating this returned function returns a new function that
// implements the requires(module) capability.
//
(function (rootDir, modulePaths, hooks) {


    var File = java.io.File,
        FileReader = java.io.FileReader,
        BufferedReader = java.io.BufferedReader;

    // var readModuleFromDirectoryOLD = function( dir ) {

    //   // look for a package.json file
    //   var pkgJsonFile = new File( dir, './package.json' );
    //   if ( pkgJsonFile.exists() ) {

    //     // --- Modified from original by eal@berkeley.edu because scload is not defined.
    //     // var pkg = scload( pkgJsonFile );
    //     var json = '';
    //     buffered = new BufferedReader(new FileReader(pkgJsonFile));
    //     while (( line = buffered.readLine()) !== null ) {
    //       json += line + '\n';
    //     }
    //     buffered.close(); // close the stream so there's no file locks
    //     var pkg = JSON.parse(json);
    //     // --- End of modified portion.

    //     var mainFile = new File( dir, pkg.main );
    //     if ( mainFile.exists() ) {
    //       return mainFile;
    //     } else {
    //       // --- Modified from original by eal@berkeley.edu to look for index.js.
    //       mainFile = new File( dir, 'index.js');
    //       if ( mainFile.exists() ) {
    //           return mainFile;
    //       } else {
    //           return null;
    //       }
    //     }
    //   } else {
    //     // look for an index.js file
    //     var indexJsFile = new File( dir, './index.js' );
    //     if ( indexJsFile.exists() ) {
    //       return indexJsFile;
    //     } else {
    //       return null;
    //     }
    //   }
    // };


    var readModuleFromDirectory = function (dir) {
        // --- Modified from original by cxh@eecs.berkeley.edu to look in the classpath. */
        var JNLPUtilities = Java.type('ptolemy.actor.gui.JNLPUtilities');

        // look for a package.json file
        // --- Modified from original by cxh@eecs.berkeley.edu to look in the classpath. */
        //var pkgJsonFile = new File( dir, './package.json' );
        var pkgJsonFile = JNLPUtilities.getResourceSaveJarURLAsTempFile(dir + '/package.json');
        if (pkgJsonFile !== null && pkgJsonFile.exists()) {

            // --- Modified from original by eal@berkeley.edu because scload is not defined.
            // var pkg = scload( pkgJsonFile );
            var json = '';
            buffered = new BufferedReader(new FileReader(pkgJsonFile));
            while ((line = buffered.readLine()) !== null) {
                json += line + '\n';
            }
            buffered.close(); // close the stream so there's no file locks
            var pkg = JSON.parse(json);
            // --- End of modified portion.

            var mainFile = new File(dir, pkg.main);
            if (mainFile.exists()) {
                return mainFile;
            } else {
                // // --- Modified from original by eal@berkeley.edu to look for index.js.
                // mainFile = new File(dir, 'index.js');
                // if (mainFile.exists()) {
                //     return mainFile;
                // } else {
                //     return null;
                // }
                // --- Modified from original by eal@berkeley.edu to look for index.js.
                var pkgMainFile = JNLPUtilities.getResourceSaveJarURLAsTempFile(dir + '/' + pkg.main);
                if (pkgMainFile !== null && pkgMainFile.exists()) {
                    return pkgMainFile;
                } else {
                    mainFile = new File(dir, 'index.js');
                    if (mainFile.exists()) {
                        return mainFile;
                    } else {
                        var indexJSFile = JNLPUtilities.getResourceSaveJarURLAsTempFile(dir + '/index.js');
                        if (indexJSFile !== null && indexJSFile.exists()) {
                            return indexJSFile;
                        } else {
                            return null;
                        }
                    }
                }
            }
        } else {
            // look for an index.js file
            // --- Modified from original by cxh@eecs.berkeley.edu to look in the classpath. */
            //var indexJsFile = new File( dir, './index.js' );
            var indexJsFile = JNLPUtilities.getResourceSaveJarURLAsTempFile(dir + '/index.json');
            if (indexJsFile !== null && indexJsFile.exists()) {
                return indexJsFile;
            } else {
                return null;
            }
        }
    };

    var fileExists = function (file) {
        if (file.isDirectory()) {
            return readModuleFromDirectory(file);
        } else {
            return file;
        }
    };

    var _canonize = function (file) {
        return "" + file.canonicalPath.replaceAll("\\\\", "/");
    };

    /**
     *  Find the file that corresponds with a module.
     *
     *  @param moduleName the name of the module to be loaded.  'foo',
     *  'foo.js', './foo/bar.js' and '../foo.js' should all work.
     *  @param the parent directory, which is of type java.io.File.
     *  @param modulePaths An array of fully qualified directories to search
     *  @return the file that corresponds with the module.
     */
    var resolveModuleToFile = function (moduleName, parentDir, modulePaths) {
        // print('require.js: moduleName: ' + moduleName );

        // --- Modified from original by cxh@eecs.berkeley.edu to search in the classpath.
        var JNLPUtilities = Java.type('ptolemy.actor.gui.JNLPUtilities');
        var moduleFilePath = Java.type('ptolemy.util.FileUtilities').nameToFile(
            '$CLASSPATH/ptolemy/actor/lib/jjs/', null).getAbsolutePath();
        if (moduleName.match(/^[^\.\/]/)) {
            // it's a module named like so ... 'events' , 'net/http'

            // Replace '@accessor-modules' with 'modules'.
            // See https://www.icyphy.org/accessors/wiki/Notes/NPM#CapeCodeNPMDesignDecisions
            moduleName = moduleName.replace(/@accessors-modules/, 'modules');

            // Convert names like http-client to httpClient because Java does
            // not support package names with hyphens.
            if (moduleName.indexOf('-') != -1) {
                var newModuleName = "";
                var lastSlashIndex = moduleName.length;
                // Don't replace hypens in filenames.
                if (moduleName.lastIndexOf('/') != -1 && moduleName.endsWith('.js')) {
                    lastSlashIndex = moduleName.lastIndexOf('/') - 1;
                }
                for (var i = 0; i < moduleName.length; i++) {
                    var char = moduleName.charAt(i)
                    // If the last part of the module name is a
                    // filename with a hyphen, then don't replace the
                    // -.  MochaTest requires this.
                    if (i >= lastSlashIndex || char !== '-') {
                        newModuleName = newModuleName + char;
                    } else {
                        i = i + 1;
                        newModuleName = newModuleName + moduleName.charAt(i).toUpperCase();
                    }
                }
                moduleName = newModuleName;
            }

            // print('require.js: moduleName: ' + moduleName );
            for (var i = 0; i < modulePaths.length; i++) {
                var modulePath = modulePaths[i];
                // Windows?
                if (moduleFilePath.indexOf('\\') != -1) {
                    modulePath = modulePath.replace(/\//g, '\\');
                }
                // Remove the value of __moduleFile from the start of modulePaths.
                var shortModulePath = modulePath.replace(moduleFilePath, 'ptolemy/actor/lib/jjs');
                shortModulePath = shortModulePath.replace(moduleFilePath, 'ptolemy\\actor\\lib\\jjs');
                var classPathFile = JNLPUtilities.getResourceSaveJarURLAsTempFile(shortModulePath + moduleName);
                if (classPathFile !== null) {
                    if (classPathFile.isFile()) {
                        return classPathFile;
                    } else {
                        // FIXME: check to see if it has a trailing /
                        var resourceDirectory = classPathFile.toString();
                        return readModuleFromDirectory(resourceDirectory.substr(0, resourceDirectory.length()));
                    }
                } else {
                    // try appending a .js to the end.
                    classPathFile = JNLPUtilities.getResourceSaveJarURLAsTempFile(shortModulePath + moduleName + '.js');
                    if (classPathFile !== null && classPathFile.isFile()) {
                        return classPathFile;
                    } else {
                        // Search for moduleName/moduleName.js.
                        classPathFile = JNLPUtilities.getResourceSaveJarURLAsTempFile(shortModulePath + moduleName + "/" + moduleName + '.js');
                        if (classPathFile !== null && classPathFile.isFile()) {
                            return classPathFile;
                        }
                    }
                }
            }
        } else {
            // it's of the form ./path or ../path
            var startIndex = 2;
            var newParentDirectory = parentDir;
            if (moduleName.substr(0, 3) === '../') {
                startIndex = 3;
                newParentDirectory = parentDir.getParent();
            }
            var testPath = newParentDirectory + "/" + moduleName.substr(startIndex)
            // print('require.js: ./ or ../ moduleName: ' + moduleName + ' testPath: ' + testPath);
            var classPathFile2 = JNLPUtilities.getResourceSaveJarURLAsTempFile(testPath);
            if (classPathFile2 !== null && classPathFile2.isFile()) {
                // print('require.js: moduleName: ' + moduleName + ' returning ' + classPathFile2);
                return classPathFile2;
            } else {
                // try appending a .js to the end
                testPath = newParentDirectory + "/" + moduleName + ".js";
                // print('require.js: moduleName: ' + moduleName + ' testPath: ' + testPath);
                classPathFile2 = JNLPUtilities.getResourceSaveJarURLAsTempFile(testPath);
                if (classPathFile2 !== null && classPathFile2.isFile()) {
                    // print('require.js: moduleName: ' + moduleName + ' returning ' + classPathFile2);
                    return classPathFile2;
                } else {
                    testPath = moduleName + ".js";
                    // print('require.js: moduleName: ' + moduleName + ' testPath: ' + testPath);
                    classPathFile2 = JNLPUtilities.getResourceSaveJarURLAsTempFile(testPath);
                    if (classPathFile2 !== null && classPathFile2.isFile()) {
                        // print('require.js: moduleName: ' + moduleName + ' returning ' + classPathFile2);
                        return classPathFile2;
                    }
                }
            }
        }
        // --- End of modified section.

        // --- Modified from original by eal@berkeley.edu to search cwd last rather than first.
        // var file = new File(moduleName);
        // if ( file.exists() ) {
        //   return fileExists(file);
        // }
        // --- End of modified section.
        // if (typeof moduleName !== 'string') {
        //     throw 'Invalid module name: ' + JSON.stringify(moduleName);
        // }
        // if ( moduleName.match( /^[^\.\/]/ ) ) {
        //   // it's a module named like so ... 'events' , 'net/http'
        //   //
        //   var resolvedFile;
        //   for ( var i = 0; i < modulePaths.length; i++ ) {
        //     resolvedFile = new File( modulePaths[i] + moduleName );
        //     if ( resolvedFile.exists() ) {
        //       return fileExists( resolvedFile );
        //     } else {
        //       // try appending a .js to the end
        //       resolvedFile = new File( modulePaths[i] + moduleName + '.js' );
        //       if ( resolvedFile.exists() ) {
        //         return resolvedFile;
        //       }
        //     }
        //   }
        // } else {
        //   // it's of the form ./path
        //   file = new File( parentDir, moduleName );
        //   if ( file.exists() ) {
        //     return fileExists(file);
        //   } else {
        //     // try appending a .js to the end
        //     var pathWithJSExt = file.canonicalPath + '.js';
        //     file = new File( parentDir, pathWithJSExt);
        //     if ( file.exists() ) {
        //       return file;
        //     } else {
        //       file = new File( pathWithJSExt );
        //       if ( file.exists() ) {
        //         return file;
        //       }
        //     }
        //   }
        // }
        // // --- Modified from original by eal@berkeley.edu to search cwd last rather than first.
        // var file = new File(moduleName);
        // if ( file.exists() ) {
        //   return fileExists(file);
        // }
        // // --- End of modified section.

        return null;
    };

    var _loadedModules = {};
    var fmt = java.lang.String.format;
    /*
     require() function implementation
     */
    var _require = function (parentFile, path, modulePaths) {
        var file,
            canonizedFilename,
            moduleInfo,
            buffered,
            head = '(function(exports,module,require,__filename,__dirname){ ',
            code = '',
            tail = '})',
            line = null;

        file = resolveModuleToFile(path, parentFile, modulePaths);
        if (!file) {
            // Use parentFile.absolutePath instead of parentFile.canonicalFile
            // because parentFile.canonicalFile will fail under certain circumstances.
            var errMsg = '' + fmt("require() failed to find matching file for module '%s' " +
                "in working directory '%s' ", [path, parentFile.absolutePath]);
            if (!(('' + path).match(/^\./))) {
                errMsg = errMsg + ' and not found in paths ' + JSON.stringify(modulePaths);
            }
            throw errMsg;
        }
        canonizedFilename = _canonize(file);

        moduleInfo = _loadedModules[canonizedFilename];
        if (moduleInfo) {
            return moduleInfo;
        }
        if (hooks) {
            hooks.loading(canonizedFilename);
        }
        buffered = new BufferedReader(new FileReader(file));
        while ((line = buffered.readLine()) !== null) {
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
            // Uncomment the code below if you want to see what files
            // are being required.
            // print is a Nashorn built-in. If we use console.log(), then
            // we would need to put it in a try/catch block.
            // print("require.js: " + canonizedFilename + " read from: " + file);
            compiledWrapper = eval(code);
        } catch (e) {
            var message = e.message;
            if (!message) {
                message = e.toString();
            }
            throw new Error("Error evaluating module " + path +
                " line #" + e.lineNumber +
                " : " + message + "\nIn file: " + canonizedFilename);
        }
        var __dirname = '' + file.parentFile.canonicalPath;
        var parameters = [
            moduleInfo.exports, /* exports */
            moduleInfo, /* module */
            moduleInfo.require, /* require */
            canonizedFilename, /* __filename */
            __dirname /* __dirname */
        ];
        // alert(JSON.stringify(parameters));
        try {
            compiledWrapper
                .apply(moduleInfo.exports, /* this */
                    parameters);
        } catch (e) {
            var exceptionMessage = e.message;
            if (!exceptionMessage) {
                exceptionMessage = e.toString();
            }
            throw new Error("Error executing module " + path +
                " line #" + e.lineNumber +
                " : " + exceptionMessage + "\nIn file: " + canonizedFilename);
        }
        if (hooks) {
            hooks.loaded(canonizedFilename);
        }
        moduleInfo.loaded = true;
        return moduleInfo;
    };

    var _requireClosure = function (parent) {
        return function (path) {
            var module = _require(parent, path, modulePaths);
            return module.exports;
        };
    };
    return _requireClosure(new java.io.File(rootDir));
    // last line deliberately has no semicolon!
});
