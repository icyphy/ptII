ptolemy/copernicus/README.txt
$Id$
The copernicus directory contains code generation facilities for
Ptolemy II that use Soot.

Copernicus reads in Ptolemy .class files that describe a model
and its actors and generates output, either Java or Applets.

For more information, see $PTII/doc/copernicus.htm

Quick Example
-------------
Generate Java code for a simple model:

	 cd $PTII/ptolemy/copernicus
	 $PTII/bin/copernicus ../actor/lib/test/auto/IIR.xml 

The Java code will be in $PTII/ptolemy/copernicus/java/cg/IIR
To decompile the code:

	 cd $PTII/ptolemy/copernicus/java/cg/IIR
	 make davaDecompile

Limitations
-----------
As of 1/2008, the Copernicus code generator had the following limitations:
- Only works on SDF
- Only works on Flat models, hierarchy is not supported
- Not all actors are supported
- C code generation barely works.  Use $PTII/ptolemy/codegen instead

What does work fairly well is the applet code generation.


Notes about working with Copernicus 
----------------------------------
Copernicus uses Soot (http://www.sable.mcgill.ca/soot/).

Soot is an optimization framework provides tools to transform byte codes

The transforms happen in what Soot calls phases.  See the Main.java
file, for example copernicus/java/Main.java

To add debugging, add " debug:true" to the addTransform() calls in Main.java

A good decompiler to use with Soot is Dava, which is part of Soot.  To
run it, try 
    $PTII/bin/soot -f d -d . ptolemy.actor.lib.Ramp
Then look in the ./dava/src/ptolemy/actor/lib/Ramp.java



Soot
----

We are currently using Soot 2.0.1 with the following changes.   

revision 1.16
date: 2004/04/01 23:43:16;  author: neuendor;  state: Exp;  lines: +96 -5
patches for dealing with other classconstants.CVS: ----------------------------------------------------------------------
----------------------------
revision 1.15
date: 2003/11/16 06:59:28;  author: neuendor;  state: Exp;  lines: +89 -98
more patches
----------------------------
revision 1.14
date: 2003/11/13 23:10:59;  author: neuendor;  state: Exp;  lines: +69 -77
fixed a jimple writing bug.
----------------------------
revision 1.13
date: 2003/11/13 21:47:02;  author: neuendor;  state: Exp;  lines: +429 -192
added constant propagation for class constants.
----------------------------
revision 1.12
date: 2003/10/22 19:05:16;  author: neuendor;  state: Exp;  lines: +365 -301
Added patches to:
soot/dava/DavaBody
soot/jimple/internal/javaRep/DNewArrayExpr
soot/jimple/toolkits/callgraph/ReachableMethods
----------------------------
revision 1.11
date: 2003/10/06 17:35:43;  author: cxh;  state: Exp;  lines: +6742 -6795
Made change to Options.java so that empty arguments now work.  Checked in CFG patch from Ankush
----------------------------
revision 1.10
date: 2003/09/17 23:44:23;  author: neuendor;  state: Exp;  lines: +7138 -11621
soot 2.0.1

$PTII/lib/sootclasses.jar includes the changed Java files.


