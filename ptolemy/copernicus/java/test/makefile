# Makefile for java deep and shallow code generation tests 
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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

# Location of this directory, relative to the Ptolemy II directory
ME =		ptolemy/copernicus/java/test

# Root of the Ptolemy II directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	testDefs.tcl

JSRCS = \

# Non-graphical Java tests written in Tcl
JSIMPLE_TESTS = \
	QuickSmoke.tcl

# Long tests, that take so damn long they are not even done in the
# nightly build.
JLONG_TESTS = \
	$(JSIMPLE_TESTS) \
	Deep.tcl

# Graphical Java tests that use Tcl.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

EXTRA_SRCS = \
	alldeep \
	$(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) #$(JGRAPHICAL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	alljtests.tcl longjtests.tcl

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = cg

JCLASS =	$(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

tests:: $(EXTRA_SRCS) jclass test_java test_jsimple

test_java: jclass

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

# Increase the amount of memory.  See the JTCLSH variable in $PTII/ptII.mk
JAVAFLAGS = -Xms228m -Xmx228m

# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl
	@echo "There are also long tests in this directory."
	@echo "Run 'make test_long' to run these tests, which will take 5 hrs"
	$(JTCLSH) alljtests.tcl

test_long: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) longjtests.tcl
	$(JTCLSH) longjtests.tcl

# lonjtests.tcl is used to source all the long tcl files that use Java
longjtests.tcl: makefile
	rm -f $@
	echo '# CAUTION: automatically generated file by a rule in makefile' > $@
	echo '# This file will source all the Tcl files that use Java. ' >> $@
	echo '# This file will source the tcl files list in the' >> $@
	echo '# makefile SIMPLE_JTESTS and JGRAPHICAL_TESTS and JLONG_TESTS variables' >> $@
	echo '# This file is different from all.itcl in that all.itcl' >> $@
	echo '# will source all the .itcl files in the current directory' >> $@
	echo '#' >> $@
	echo '# Set the following to avoid endless calls to exit' >> $@
	echo "if {![info exists reallyExit]} {set reallyExit 0}" >> $@
	echo '# Exiting when there are no more windows is wrong' >> $@
	echo "#::tycho::TopLevel::exitWhenNoMoreWindows 0" >> $@
	echo '# If there is no update command, define a dummy proc.  Jacl needs this' >> $@
	echo 'if {[info command update] == ""} then { ' >> $@
	echo '    proc update {} {}' >> $@
	echo '}' >> $@
	echo "#Do an update so that we are sure tycho is done displaying" >> $@
	echo "update" >> $@
	echo "set savedir \"[pwd]\"" >> $@
	echo "if {\"$(JSIMPLE_TESTS)\" != \"\"} {foreach i [list $(JSIMPLE_TESTS)] {puts \$$i; cd \"\$$savedir\"; if [ file exists \$$i ] { if [ catch {source \$$i} msg] {puts \"Error: \$$msg\"}}}}" >> $@
	if [ "x$(JGRAPHICAL_TESTS)" != "x" ]; then \
		for x in $(JGRAPHICAL_TESTS); do \
			echo "puts stderr $$x" >> $@; \
			echo "cd \"\$$savedir\"" >> $@; \
			echo "if [ file exists $$x ] { if [catch {source $$x} msg] {puts \"Error: \$$msg\"}}" >> $@; \
		done; \
	fi
	if [ "x$(JLONG_TESTS)" != "x" ]; then \
		for x in $(JLONG_TESTS); do \
			echo "puts stderr $$x" >> $@; \
			echo "cd \"\$$savedir\"" >> $@; \
			echo "if [ file exists $$x ] { if [catch {source $$x} msg] {puts \"Error: \$$msg\"}}" >> $@; \
		done; \
	fi
	echo "catch {doneTests}" >> $@
	echo "exit" >> $@

# 'make ptjacl' will start up jacl with the proper classpath
ptjacl:
	$(JTCLSH)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
