
This directory contains a C back-end for Ptolemy II code generation, as well as
standalone functionality for converting Java class files into C source files
that implement the classes. For a demo of the Java-to-C conversion functionality,
run the following commands:

	cd ptolemy/copernicus/c/test
	make Object

This will produce the source files java.lang.Object.c and java.lang.Object.h,
which are derived by converting java.lang.Object.

Only a limited set of Java language features is presently supported by
this C code generation functionality. We are actively extending the
functionality to include more features.

Shuvra S. Bhattacharyya (ssb@eng.umd.edu) 
08/07/2001

