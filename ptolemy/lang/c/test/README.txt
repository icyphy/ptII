
This directory contains test cases for the
C code genetor back-end functionality.

To run a test in this directory from the
parent directory, execute:

java TestCCodeGenerator test\<testname>.java > <testname>.c

(e.g., java TestCCodeGenerator test\TestModule.java > TestModule.c)

The output will be stored in <testname>.c.
The generated C code can be found at the end of the
generated file (it may be preceded by a lot of diagnostic
output).


