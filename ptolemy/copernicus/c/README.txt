
The Ptolemy C code generator (PCCG)

@author Ankush Varma, Shuvra S. Bhattacharyya 
@version $Id$

This directory contains a C back-end for Ptolemy II code generation, as well as
standalone functionality for converting Java class files into C source files
that implement the classes. For two demos of the Java-to-C conversion
functionality, run the following commands:

	cd ptolemy/copernicus/c/test
	make Simple

    cd ptolemy/copernicus/c/test/FIR
    make fir

Only a limited set of Java language features is presently supported by this C
code generation functionality. We are actively extending the functionality to
include more features. Integration as a back-end to Ptolemy code generation is
also in progress.

