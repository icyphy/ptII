ptolemy/copernicus/README.txt
$Id$
The copernicus directory contains code generation facilities for
Ptolemy II that use Soot.

Soot is an optimization framework provides tools to transform byte codes


For more information, see $PTII/doc/codegen.htm#soot

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


