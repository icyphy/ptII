
This directory implements a C code generator "back end" for software synthesis
in Ptolemy II.  This code converts Java abstract syntax trees (after static
resolution has been performed) into equivalent C code.

This is a very preliminary version, and only a simple subset of Java is
supported at present. 

Partial list of unsupported features:

method calls, inheritance, import statements, interfaces, reflection, garbage
collection.


