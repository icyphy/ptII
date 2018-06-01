---
layout: default
---
# Ptolemy II External Access
Below are instructions for accessing the current development version of Ptolemy II, which is stored on [https://github.com/icyphy](Github)

## Other key resources:

* [Book on Ptolemy II](https://ptolemy.berkeley.edu/systems) (free download)
* [Ptolemy II main page](https://ptolemy.berkeley.edu/ptolemyII)
* [Contributing](https://github.com/icyphy/ptII/blob/master/CONTRIBUTING.md)
 * [Ptolemy II Style Guide](https://www2.eecs.berkeley.edu/Pubs/TechRpts/2014/EECS-2014-164.html)
* [Javadoc](https://icyphy.github.io/ptII-test/doc/codeDoc/) output for Java files
* [JsDoc](https://icyphy.github.io/ptII-test/doc/codeDoc/js/index.html) output for Javascript
* [Travis Build](https://travis-ci.org/icyphy/ptII)
 * [PtII specific Travis Notes](https://wiki.eecs.berkeley.edu/ptexternal/Main/Travis)
 * [Downloads](downloads/index.html)
 * [Logs](https://icyphy.github.io/ptII-test/logs/index.html)
 * [Reports](https://icyphy.github.io/ptII-test/reports/index.html)

# How to get Ptolemy II source code
## For the impatient

Install Java 1.8, install git and then run:

```
git clone --depth=50 --branch=master --single-branch https://github.com/icyphy/ptII
cd ptII
export PTII=`pwd`
./configure
$PTII/bin/ant
$PTII/bin/vergil
```

To develop Ptolemy II code, we recommend that you follow the  
[Ptolemy II Eclipse Instructions located in $PTII/doc/eclipse/index.htm](https://cdn.rawgit.com/icyphy/ptII/d3d13556/doc/eclipse/index.htm)

Or, see the [Ant](https://cdn.rawgit.com/icyphy/ptII/0e6c0a96/doc/coding/ant.htm) instructions.

Windows users, Eclipse is the preferred installation method.  If you want to build with Cygwin, see:

* [Cygwin Instructions](https://ptolemy.berkeley.edu/ptolemyII/ptIIlatest/cygwin.htm)
* [Ptolemy II Installation Insructions](https://ptolemy.berkeley.edu/ptolemyII/ptIIlatest/ptII/doc/install.htm)

## See Also
* [Summary of how to get Ptolemy II](summaryOfHowToGetPtII.html)
* [How to edit this page](edit.html)
