
This directory contains test cases for the
Ptolemy II C code genetor back-end functionality.

To run a test in this directory from the parent directory, execute:

java TestCCodeGenerator test\<testname>.java > <testname>.c
(e.g., java TestCCodeGenerator test\TestModule.java > TestModule.c)

or (if .\bin is in your path), execute:

javatoc <testname>
(e.g., javatoc TestModule)

Diagnostic output will be stored in test\<testname>-out.txt.  The generated C
code will be placed in test\<testname>.c and test\<testname>.h.

@author Shuvra S. Bhattacharyya 
@version $Id$
