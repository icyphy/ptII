# Makefile for the Java classes used to test the moml filter package
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
ME =		ptolemy/moml/filter/test

# Root of the Ptolemy II directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	testDefs.tcl

JSRCS =

# Non-graphical Java tests written in Tcl
JSIMPLE_TESTS = \
	BackwardCompatibility.tcl \
	MultiportToSinglePort.tcl \
	RemoveGraphicalClasses.tcl


# Graphical Java tests that use Tcl.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) \
	RemoveGraphicalClasses.xml \
	 #$(JGRAPHICAL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	alljtests.tcl \
	auto

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	compat \
	compatDemo \
	compat2 \
	compatDemo2 \
	compat3 \
	compatDemo3

JCLASS =	$(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

tests:: $(EXTRA_SRCS) jclass test_java test_jsimple test_auto

test_java: jclass

# Location of the old tree that we harvest auto/*.xml
# files from old release trees and copy them into compat and run them
OLDTREE = $(ROOT)/adm/dists/ptII1.0.1

# Directory that contains models to check for compatibility
# We could check these into the tree, but it is about 1mb of files
compat:
	@if [ ! -d $(OLDTREE) ]; then \
		echo "$(OLDTREE) does not exist"; \
		echo "skipping compatibility tests"; \
	else \
		echo "Generating $@/ from $(OLDTREE)"; \
		(cd $(OLDTREE); \
			find . -name "*.xml" -print | \
			grep auto | \
			awk '{print "$(OLDTREE)/" $$1}' > /tmp/oldxmls); \
		mkdir $@; \
		echo "Copying old xmls over"; \
		cp `cat /tmp/oldxmls` $@; \
		echo "Copying xmls that have the same name"; \
		rm $@/test1.xml; \
		cp $(OLDTREE)/ptolemy/domains/sdf/kernel/test/auto/test1.xml \
			$@/sdftest1.xml; \
		cp $(OLDTREE)/ptolemy/domains/sdf/lib/vq/test/auto/test1.xml \
			$@/vqtest1.xml; \
		rm $@/test2.xml; \
		cp $(OLDTREE)/ptolemy/domains/sdf/kernel/test/auto/test1.xml \
			$@/sdftest1.xml; \
		cp $(OLDTREE)/ptolemy/domains/sdf/lib/vq/test/auto/test1.xml \
			$@/vqtest1.xml; \
		echo "Removing tests that failed in earlier releases"; \
		rm -f $@/MaximumEntropy.xml \
			$@/OutputActionsAttribute.xml; \
	fi

# Directory that contains models from the demo directory 
# to check for compatibility
compatDemo:
	@if [ ! -d $(OLDTREE) ]; then \
		echo "$(OLDTREE) does not exist"; \
		echo "skipping compatibility tests"; \
	else \
		echo "Generating $@/ from $(OLDTREE)"; \
		(cd $(OLDTREE); \
			find . -name "*.xml" -print | \
			grep demo | \
			awk '{print "$(OLDTREE)/" $$1}' > /tmp/oldxmls); \
		mkdir $@; \
		echo "Copying old xmls over"; \
		cp `cat /tmp/oldxmls` $@; \
		rm -f /tmp/oldxmls; \
	fi

# Location of the old tree that we harvest auto/*.xml
# files from old release trees and copy them into compat and run them
OLDTREE2 = $(ROOT)/adm/dists/ptII2.0.1

# Directory that contains models to check for compatibility
# We could check these into the tree, but it is about 1mb of files
compat2:
	@if [ ! -d $(OLDTREE2) ]; then \
		echo "$(OLDTREE2) does not exist"; \
		echo "skipping compatibility tests"; \
	else \
		echo "Generating $@/ from $(OLDTREE2)"; \
		(cd $(OLDTREE2); \
			find . -name "*.xml" -print | \
			grep auto | \
			awk '{print "$(OLDTREE2)/" $$1}' > /tmp/oldxmls); \
		mkdir $@; \
		echo "Copying old xmls over"; \
		cp `cat /tmp/oldxmls` $@; \
		rm -f /tmp/oldxmls; \
		echo "Copying xmls that have the same name"; \
		rm $@/test1.xml; \
		cp $(OLDTREE2)/ptolemy/domains/sdf/kernel/test/auto/test1.xml \
			$@/sdftest1.xml; \
		cp $(OLDTREE2)/ptolemy/domains/sdf/lib/vq/test/auto/test1.xml \
			$@/vqtest1.xml; \
		rm $@/test2.xml; \
		cp $(OLDTREE2)/ptolemy/domains/sdf/kernel/test/auto/test1.xml \
			$@/sdftest1.xml; \
		cp $(OLDTREE2)/ptolemy/domains/sdf/lib/vq/test/auto/test1.xml \
			$@/vqtest1.xml; \
		echo "Removing tests that failed in earlier releases"; \
		rm -f $@/MaximumEntropy.xml \
			$@/OutputActionsAttribute.xml; \
	fi

# Directory that contains models from the demo directory 
# to check for compatibility
compatDemo2:
	@if [ ! -d $(OLDTREE2) ]; then \
		echo "$(OLDTREE2) does not exist"; \
		echo "skipping compatibility tests"; \
	else \
		echo "Generating $@/ from $(OLDTREE2)"; \
		(cd $(OLDTREE2); \
			find . -name "*.xml" -print | \
			grep demo | \
			awk '{print "$(OLDTREE2)/" $$1}' > /tmp/oldxmls); \
		mkdir $@; \
		echo "Copying old xmls over"; \
		cp `cat /tmp/oldxmls` $@; \
	fi


# Location of the old tree that we harvest auto/*.xml
# files from old release trees and copy them into compat and run them
OLDTREE3 = $(ROOT)/adm/dists/ptII3.0.2

# Directory that contains models to check for compatibility
# We could check these into the tree, but it is about 1mb of files
compat3:
	@if [ ! -d $(OLDTREE3) ]; then \
		echo "$(OLDTREE3) does not exist"; \
		echo "skipping compatibility tests"; \
	else \
		echo "Generating $@/ from $(OLDTREE3)"; \
		(cd $(OLDTREE3); \
			find . -name "*.xml" -print | \
			grep auto | grep -v knownFailed | \
			awk '{print "$(OLDTREE3)/" $$1}' > /tmp/oldxmls); \
		mkdir $@; \
		echo "Copying old xmls over"; \
		cp `cat /tmp/oldxmls` $@; \
		rm -f /tmp/oldxmls; \
		echo "Copying xmls that have the same name"; \
		rm $@/test1.xml; \
		cp $(OLDTREE3)/ptolemy/domains/sdf/kernel/test/auto/test1.xml \
			$@/sdftest1.xml; \
		cp $(OLDTREE3)/ptolemy/domains/sdf/lib/vq/test/auto/test1.xml \
			$@/vqtest1.xml; \
		rm $@/test3.xml; \
		cp $(OLDTREE3)/ptolemy/domains/sdf/kernel/test/auto/test1.xml \
			$@/sdftest1.xml; \
		cp $(OLDTREE3)/ptolemy/domains/sdf/lib/vq/test/auto/test1.xml \
			$@/vqtest1.xml; \
		echo "Removing tests that failed in earlier releases"; \
		rm -f $@/MaximumEntropy.xml \
			$@/OutputActionsAttribute.xml; \
	fi

# Directory that contains models from the demo directory 
# to check for compatibility
compatDemo3:
	@if [ ! -d $(OLDTREE3) ]; then \
		echo "$(OLDTREE3) does not exist"; \
		echo "skipping compatibility tests"; \
	else \
		echo "Generating $@/ from $(OLDTREE3)"; \
		(cd $(OLDTREE3); \
			find . -name "*.xml" -print | \
			grep demo | \
			awk '{print "$(OLDTREE3)/" $$1}' > /tmp/oldxmls); \
		mkdir $@; \
		echo "Copying old xmls over"; \
		cp `cat /tmp/oldxmls` $@; \
	fi

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass compat compatDemo compat2 compatDemo2 compat3 compatDemo3 $(KERNEL_TESTDEFS) alljtests.tcl
	$(JTCLSH) alljtests.tcl 

# Run vergil on each of the demos in compatDemo
demo: compatDemo
	set compatDemo/*.xml; \
	for x do \
		echo $$x;\
		$(ROOT)/bin/vergil $$x; \
	done


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
