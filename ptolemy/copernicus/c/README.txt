
The Ptolemy C code generator (PCCG)

@author Ankush Varma, Shuvra S. Bhattacharyya 
@version $Id$

NOTE: After Ptolemy II 5.x, this package no longer really works.
This package is left as an artifact.

Under Java 1.5, three tests fail:
  LinkedListTest.exe segfaults because the next() method is not found
  LinpackTest fails because java.util.regex.Pattern$Slice cannot be found
  String fails because java.util.regex.Pattern$Slice cannot be found

Under Java 1.4, two tests fail:
  LinkedListTest.exe segfaults because the next() method is not found
  Linpack fails because java.lang.VirtualMachineError cannot be found

These tests worked in Ptolemy II 5.0.1 under Java 1.4 with a version
of soot based on soot 2.0.1.  Updating to Java 1.5 required
soot-2.2.2, which then caused problems.


NOTE: After Ptolemy II 5.x, this package no longer really works.
This package is left as an artifact.

Under Java 1.5, three tests fail:
  LinkedListTest.exe segfaults because the next() method is not found
  LinpackTest fails because java.util.regex.Pattern$Slice cannot be found
  String fails because java.util.regex.Pattern$Slice cannot be found

Under Java 1.4, two tests fail:
  LinkedListTest.exe segfaults because the next() method is not found
  Linpack fails because java.lang.VirtualMachineError cannot be found

These tests worked in Ptolemy II 5.0.1 under Java 1.4 with a version
of soot based on soot 2.0.1.  Updating to Java 1.5 required
soot-2.2.2, which then caused problems.



This directory contains a C back-end for Ptolemy II code generation, as well as
standalone functionality for converting Java class files into C source files
that implement the classes. For two demos of the Java-to-C conversion
functionality, run the following commands:

	cd ptolemy/copernicus/c/test
	make Simple

    cd ptolemy/copernicus/c/test/FIR
    make fir


