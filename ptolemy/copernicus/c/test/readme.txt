copernicus/c/test/

This directory contains various tcl and java files used for testing.

Note that Arrays.java and Exceptions.java implement multiple tests by
calling java files implementing individual tests. 

Calling the Java-to-C compiler with the option "-target C6000" generates C
code and makefile that will call the c6x compiler for C6000 DSPs. The file
generic.cmd is required by the makefile. 
