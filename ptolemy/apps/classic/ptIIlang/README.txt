ptolemy/apps/classic/ptIIlang/README.txt
$Id$

This directory contains ptIIlang, which is an extension of 
ptlang from Ptolemy Classic.

ptIIlang.y is a Yacc file that is is processed by yacc or bison
to create ptIIlang.c.  Bison is a GNU version of yacc.

Edit ptIIlang.y, do not edit ptIIlang.c, your changes in ptIIlang.c
will be erased when yacc or bison is run.

ptIIlang.y a very old file and the resulting code in ptIIlang.c
is pre-ansi-C.

Building
--------
Under Windows, Cygwin might include bison.  
Try running make to generate ptIIlang.c
If bison is not present, run Cygwin setup and install bison

Testing
-------
The makefile all rule will run ptIIlang on a Ptolemy Classic .pl file
Feel free to edit the makefile to run on other test files
The CCG actors can be found in the Ptolemy Classic repository in
ptolemy/src/domains/cgc/dsp/stars/CGCFastFIR.pl

Adding a new output file
------------------------
In ptIIlang.y, search for "CREATE THE .java FILE".
That section of code creates the .java file by opening the file
and writing to it.

Caveats
-------
Remember, C is not Java, so you need to be careful about allocating
and copying strings.  If possible, use sprintf(str1, ...) and 
sprintf(str2, ....

Try to follow the style as much as possible.

If you make not backward compatible changes, mark them with a FIXME:

		// FIXME: Java incompatibility

---



