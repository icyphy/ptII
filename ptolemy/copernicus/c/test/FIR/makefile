# Makefile for testing Ptolemy II C code generation
#
# @Authors:  (makefile) Shuvra S. Bhattacharyya --- based on a file by 
# Christopher Hylands and Tom Parks
#
# @Version: $Id$
#
# @Copyright (c) 2001-2002 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY

ME =		ptolemy/copernicus/c/test

# Root of the Java directory
ROOT =		../../../../..

# $PTII/configure looks for Soot and JHDL and sets
# variables in $PTII/mk/ptII.mk
CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

JSRCS = \
	FIR.java

EXTRA_SRCS = \
	FIRMain.c \
	FIRconfig.h \
	FIRtest.xml \
	README.txt \
	$(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) #$(JGRAPHICAL_TESTS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	bin

JCLASS =	$(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

demo: fir

# Generate C code for the FIR example
fir: 
		echo "Remove earlier versions of .o and .exe files (if they exist)"; \
		rm -f *.o; \
		rm -f *.exe; \
		echo "Generating the class file ..."; \
		javac FIR.java; \
		echo "Translating the class file to C ..."
		CLASSPATH="$(CLASSPATH)" \
		$(JAVA) -Dj2c_lib=\j2c_lib ptolemy.copernicus.c.JavaToC \
		".$(CLASSPATHSEPARATOR)$(JAVA_SYSTEM_JAR)" -singleClass FIR; \
		echo "Compiling the generated C ..."; \
		gcc -c -I../../runtime FIR.c; \
		gcc -c -I../../runtime FIRMain.c; \
		echo "Compiling the required runtime file(s) ..."; \
		cd ../../runtime; \
		rm -f runtime.o; \
		gcc -c runtime.c; \
		cd ../test/FIR; \
		echo "Linking the .o files ..."; \
		gcc -o firtest FIRMain.o FIR.o ../../runtime/runtime.o; \
		echo "Executing the generated executable (firtest.exe) ..."; \
		cmd /c firtest

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

