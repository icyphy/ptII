
FIR filter test for Java to C Translation.

@author Shuvra S. Bhattacharyya 
@version $Id$

To run this test, run the batch file bin\runme.bat (from this directory), or
carry out the following steps:

-- javac FIR.java

-- java ptolemy.copernicus.c.JavaToC .;C:\jdk1.3.1\jre\lib\rt.jar FIR -singleClass 
(This should generate three files: FIR.c, FIR.h, and FIR.i.h)

-- Run the following sequence of compilations:
gcc -c FIR.c
gcc -c FIRMain.c
gcc -o firtest FIRMain.o FIR.o

-- Run 'firtest'

The output will be that produced by a single invocation of the filter on the
given data (as specified in FIRconfig.h). To change paramters of the filter,
change corresponding values in FIRconfig.h, and recompile the .c files.

A corresponding Ptolemy II model is provided in FIRtest.xml. This model drives
an equivalent FIR filter with an equivalent input stream. Thus, one can compare
the output of the generated C code with the output of what a corresponding
Ptolemy II actor would produce.

The correct output for this demo is:

11.000000
4.000000
9.000000
0.000000
0.000000
0.000000
0.000000
0.000000
0.000000
0.000000
