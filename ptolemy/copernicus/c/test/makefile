# Makefile for testing Ptolemy II C code generation
#
# @Authors:  (makefile) Shuvra S. Bhattacharyya, Christopher Hylands --- based 
# on a file by Tom Parks.
#
# @Version: $Id$
#
# @Copyright (c) 2001-2005 The Regents of the University of California.
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

# FIXME: put FIR back when singleclass translation works again.
# DIRS =		FIR
# $PTII/configure looks for the CaffeineMark package and sets PTCM_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTCM_DIR is empty.
DIRS =		$(PTCM_DIR)

# Root of the Java directory
ROOT =		../../../..

# $PTII/configure looks for Soot and JHDL and sets
# variables in $PTII/mk/ptII.mk
CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)$(CLASSPATHSEPARATOR).

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	testDefs.tcl

# Write these in alphabetical order ONLY.
JSRCS = \
	FIRSingle.java \
        Simple.java

# Script that removes testOutput/j2c_lib and invoke.cache
# Run 'rm alljtests.tcl; make CLEANLIB=' to avoid 
# running cleanLib again.
CLEANLIB = _cleanLib.tcl

# Non-graphical Java tests written in Tcl. List these in alphabetical order
# ONLY.
JSIMPLE_TESTS = \
	$(CLEANLIB) \
    Arrays.tcl\
    Cases.tcl \
    CommandLine.tcl \
    Clone.tcl \
    Exceptions.tcl\
    FIR2.tcl \
    FIRSingle.tcl \
    LinkedListTest.tcl \
    Linpack.tcl\
    InnerField.tcl \
    InnerLocal.tcl \
    InstanceOfTest.tcl \
    Interface1.tcl \
    ObjectSingle.tcl \
    PrintStreamTest.tcl \
    RightShift.tcl \
    Simple.tcl \
    SimpleSingle.tcl \
    StringTest.tcl

# Disabled tests:
#   FIR.tcl
#   ramp.tcl


# Graphical Java tests that use Tcl.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

EXTRA_SRCS = \
	array.h \
	$(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) #$(JGRAPHICAL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = \
	$(DIRS) \
	cm \
	simple \
	C.tcl \
	FIRSingleConfig.h \
	FIRSingleMain.c \
	FIR.tcl \
	FIR.xml \
	generateC.tcl \
	HashSetTest.tcl \
	Object.tcl \
	ramp.tcl \
	ramp.xml \
	readme.txt \
	alljtests.tcl

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JSRCS = \
	Arrays.java \
	BasicException.java \
	Cases.java \
	Clone.java \
	CommandLine.java \
	Exceptions.java \
	FIR2.java \
	FIRSingle.java \
	GlobalExceptions.java \
	HashSetTest.java \
	InheritedException.java \
	InnerField.java \
	InnerLocal.java \
	InstanceOfTest.java \
	Interface1.java \
	Interface1_Class1.java \
	Interface1_Class2.java \
	Interface1_Main.java \
	LinkedListTest.java \
	Linpack.java \
	NestedExceptions.java \
	PrintStreamTest.java \
	RightShift.java \
	Simple.java \
	StringTest.java

JCLASS =	$(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

tests:: $(EXTRA_SRCS) jclass test_java test_jsimple

test_java: jclass

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

JAVAFLAGS = -Xms132m -Xmx132m
# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl
	$(JTCLSH) alljtests.tcl

# Useful rule for quickly testing
ptjacl:
	$(JTCLSH)

demo: Linpack

# Various tests.
Linpack:
	$(JTCLSH) Linpack.tcl
        
FIRSingle:
	$(JTCLSH) FIRSingle.tcl

Object:
	CLASSPATH="$(CLASSPATH)" \
		$(JAVA) ptolemy.copernicus.c.JavaToC \
		".$(CLASSPATHSEPARATOR)$(JAVA_SYSTEM_JAR)" \
		java.lang.Object

ramp:
	@echo Generating C code from the Ptolemy II model in ramp.xml.
	sh $(ROOT)/bin/ptjacl ramp.tcl
	@echo Finished with C code generation. The following files were created:
	ls -l -a testOutput/ramp.out

# Generate C code under single class mode for a simple class 
Simple:
	rm -f Simple.class Simple.c Simple.h Simple.i.h Simple.o
	@echo "Generating the class file ..."
	javac Simple.java
	@echo "Translating the class file to C ..."
	CLASSPATH="$(CLASSPATH)" \
		$(JAVA) -Dj2c_lib=\j2c_lib ptolemy.copernicus.c.JavaToC \
		".$(CLASSPATHSEPARATOR)$(JAVA_SYSTEM_JAR)" -v -singleClass Simple
	@echo "Compiling the generated C with 'gcc -c'..."
	gcc -c -I../runtime Simple.c
	@echo "Compilation finished."
	@echo "The generated source files are Simple.c, Simple.i.h, and "
	@echo "Simple.h. The resulting object file is Simple.o"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

