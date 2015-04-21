# Makefile for the Java classes used to test the Ptolemy kernel util package
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2006 The Regents of the University of California.
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
ME =		ptolemy/adm/test

# Root of the Ptolemy II directory
ROOT =		../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	testDefs.tcl

JSRCS =

# Non-graphical Java tests written in Tcl
JSIMPLE_TESTS = \
	ant.tcl \
	Coverity.tcl \
	license.tcl \
	Nightly.tcl \
	NightlyInstall.tcl \
	NightlyTest.tcl \
	NightlyUpdate.tcl \
	Release.tcl


# Graphical Java tests that use Tcl.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) \
	#$(JGRAPHICAL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	alljtests.tcl

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS=	$(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

tests:: $(EXTRA_SRCS) jclass test_java test_jsimple

test_java: jclass

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl \
				$(JCLASS_DEPENDENCIES)
	$(JTCLSH) alljtests.tcl

chktestdir:
	@$(ROOT)/adm/bin/chktestdir | sort

# Check the demos for demos that have $PTII in them.
dollarPTII:
	-(cd $(PTII); cat ptolemy/configs/doc/models.txt | sed 's@\$$CLASSPATH/@@' | xargs grep '$PTII' |  egrep -v '(StringAttribute|ptinyos/demo/.*/output|CoSimulation.xml|ptulip/demo/robot/robot.xml)')

PTIITXTFILES=adm/bin/ptIItxtfiles
TXTFILENAMES=/tmp/txtfilenames
JAVAFILENAMES=/tmp/javafilenames
$(JAVAFILENAMES): $(ROOT)/$(PTIITXTFILES)
	@(cd $(ROOT); "$(PTIITXTFILES)" > $(TXTFILENAMES))
	@egrep "\.java$$" $(TXTFILENAMES) > $(JAVAFILENAMES)

missingMakefiles: $(JAVAFILENAMES)
	(cd $(ROOT); cat $(JAVAFILENAMES) | xargs -n 1 dirname | sort | uniq | awk '{print "ls " $$1 "/makefile"}' > /tmp/missingMakefiles.sh; sh /tmp/missingMakefiles.sh 2>&1 | egrep "cannot access") | awk '{print substr($$4, 1, length($$4) - 1) }' | 2>&1 egrep -v "(Entering directory|Leaving directory|/jsoriginal/|ptolemy/apps|\./vendors|ptolemy/backtrack/test|jni/test/jni)"; rm -f /tmp/missingMakefiles.sh

missingDocPackages: $(JAVAFILENAMES)
	@cat $(JAVAFILENAMES) | xargs -n 1 dirname | sort | uniq | sed 's@/@.@g' | sed 's/^\.\.//' > /tmp/missingDocPackages.p
	@(cd $(ROOT)/doc; $(MAKE) echo | sort > /tmp/missingDocPackages.d)
	@comm -23 /tmp/missingDocPackages.p /tmp/missingDocPackages.d | 2>&1 egrep -v "(\.apps\.|\.demo|\.test$$|Entering directory|Exiting directory|ptolemy\.actor\.lib\.opencv|test\.junit|test\.system|test\.system\.junit|vendors\.|\.jsoriginal)"

# Invoked in Release.tcl by the test that does a svn status.
# Needed because it is hard to remove files that have dollar signs in them with tcl.
rmClassFiles:
	(cd $(ROOT); rm -f *.class)

KRUFT = \
	$(JAVAFILENAMES) \
	$(TXTFILENAMES) \
	/tmp/missingMakefiles.sh \
	/tmp/missingDocPackages.d \
	/tmp/missingDocPackages.p

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
