

This directory implements a C code generator "back end" for software synthesis
in Ptolemy II.  This code converts Java abstract syntax trees (after static
resolution has been performed) into equivalent C code.

This is a very preliminary version, and only a simple subset of Java is
supported at present. 

Partial list of unsupported features:

multiple source files, inheritance, import statements, interfaces, 
reflection, garbage collection.

.\test contains Java source files for testing the C code generator.
.\bin  contains useful testing scripts


@author Shuvra S. Bhattacharyya 
@version $Id$
