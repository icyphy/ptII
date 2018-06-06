---
layout: default
---
# Installing Ptolemy II
## Installers for various platforms

* [Download a snapshot using installers](downloads/index.html)

## Clone and build the source code

This method is preferred because then you can update to the latest version at any time.
[Install Java 1.8 or newer](http://www.oracle.com/technetwork/java/javase/downloads/index.html), install git and then run:

```
git clone --depth=50 --branch=master --single-branch https://github.com/icyphy/ptII
cd ptII
export PTII=`pwd`
./configure
$PTII/bin/ant
$PTII/bin/vergil
```

The last line starts vergil, the Ptolemy II GUI.
You may want to export the PTII environment variable in bash profile (or profile for whatever shell you use) and include $PTII/bin in your PATH variable.
To update to the current version in the repository after installing as above:

```
cd $PTII
./configure
$PTII/bin/ant
```

## Resources and documentation

* [Book on Ptolemy II](https://ptolemy.berkeley.edu/systems) (free download)
* [Github source repository](https://github.com/icyphy/ptII)
* [Ptolemy II project main page](https://ptolemy.berkeley.edu/ptolemyII)
* [Javadoc documentation for Ptolemy II Java classes](https://icyphy.github.io/ptII-test/doc/codeDoc/)
* [JsDoc documentation for JavaScript components](https://icyphy.github.io/ptII-test/doc/codeDoc/js/index.html)
* [Contributing](https://github.com/icyphy/ptII/blob/master/CONTRIBUTING.md)
  * [Ptolemy II Style Guide](https://www2.eecs.berkeley.edu/Pubs/TechRpts/2014/EECS-2014-164.html)

* [Nightly build (using Travis)](https://travis-ci.org/icyphy/ptII)
  * [PtII specific Travis Notes](https://wiki.eecs.berkeley.edu/ptexternal/Main/Travis) 
  * [Logs](https://icyphy.github.io/ptII-test/logs/index.html)
  * [JUnit test output](https://icyphy.github.io/ptII-test/reports/junit/html/index.html)
  * [Complete test reports](https://icyphy.github.io/ptII-test/reports/index.html)
  * [Historical JUnit test summaries](https://github.com/icyphy/ptII-test/issues/1)  

## See also

* [Using Eclipse to develop Ptolemy II $PTII/doc/eclipse/index.htm](https://cdn.rawgit.com/icyphy/ptII/master/doc/eclipse/index.htm)
* [Using Ant to build Ptolemy II](https://cdn.rawgit.com/icyphy/ptII/master/doc/coding/ant.htm)

The above two instructions are in the Ptolemy II tree, pulled from the repository via [RawGit](https://rawgit.com). For example, the Eclipse instructions are in the tree at  $PTII/doc/eclipse/index.htm translate to URL https://cdn.rawgit.com/icyphy/ptII/master/doc/eclipse/index.htm.

Windows users, Eclipse is the preferred installation method.  If you want to build with Cygwin, see:

* [Cygwin Instructions](https://ptolemy.berkeley.edu/ptolemyII/ptIIlatest/cygwin.htm)

## See Also
* [How to edit this page](edit.html)
