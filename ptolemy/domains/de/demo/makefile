# Makefile for Ptolemy II Discrete Event domain demos
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks,
#
# @Version: $Id$
#
# @Copyright (c) 1998-1999 The Regents of the University of California.
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

ME =		ptolemy/domains/de/demo

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

JSRCS = \
	ABROApplet.java \
	AdderApplet.java \
	ClockApplet.java \
	ClockSystem.java \
	HierarchyApplet.java \
	HistogramApplet.java \
	InterruptApplet.java \
	QueueApp.java \
	QueueApplet.java \
	SigmaDeltaApplet.java

# Non-graphical Java demos written in Tcl
# Since there are no demos, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a demo, be sure to add
# $(JSIMPLE_TESTS) to EXTRA_SRCS
JSIMPLE_TESTS = \
	dummy.tcl

# Graphical Java demos that use Tcl.
JGRAPHICAL_TESTS = \
	ClockApplet.tcl \
	ClockSystem.tcl \
	Demo.tcl \
	RampSystem.tcl \
	SamplerSystem.tcl

# Files that are not compiled, such as .tcl and .itcl files.
EXTRA_SRCS =	index.html $(TCL_SRCS) $(JSRCS) \
	$(JGRAPHICAL_TESTS) #$(JSIMPLE_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	#doc

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo.jar

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass demo.jar
	@echo "To run all the demos, run 'make demo'"

install: all

demo:
	CLASSPATH=$(CLASSPATH) appletviewer ABRO.html
	CLASSPATH=$(CLASSPATH) appletviewer Adder.html
	CLASSPATH=$(CLASSPATH) appletviewer Hierarchy.html
	CLASSPATH=$(CLASSPATH) appletviewer Histogram.html
	CLASSPATH=$(CLASSPATH) appletviewer Interrupt.html
	CLASSPATH=$(CLASSPATH) appletviewer Queue.html
	CLASSPATH=$(CLASSPATH) appletviewer SigmaDelta.html
	CLASSPATH=$(CLASSPATH) appletviewer index.html

PTAUXJAR = demo.jar

# 'make clean' removes everything in KRUFT
KRUFT = demo.jar

demo.jar: $(JSRCS) $(JCLASS)
	(cd $(ROOT); \
		$(JAR) cf \
		$(ME)/$@ \
		$(ME)/*.class)
depend:
	@echo "no dependencies in this directory"

# Profiling stuff
JAVA_HOME_ENV = /usr/tools/pure/jdk1.1.6
QUANTIFY_JAVA_HOME_ENV = /usr/tools/pure/quantify-4.2-solaris2

#java.prof: QueueApp.class
#	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(COLLECTIONS)$(CLASSPATHSEPARATOR)$(PROFILEVIEWER)" java -prof ptolemy.domains.de.demo.QueueApp

#viewprof: java.prof
#	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(COLLECTIONS)$(CLASSPATHSEPARATOR)$(PROFILEVIEWER)" java ProfileViewer java.prof

quantify1:
	JAVA_HOME=$(JAVA_HOME_ENV) QUANTIFY_JAVA_HOME=$(QUANTIFY_JAVA_HOME_ENV) CLASSPATH="$(CLASSPATH)" $(JAVA_HOME_ENV)/bin/qjava sun.applet.AppletViewer Adder.html

quantify2:
	JAVA_HOME=$(JAVA_HOME_ENV) QUANTIFY_JAVA_HOME=$(QUANTIFY_JAVA_HOME_ENV) CLASSPATH="$(CLASSPATH)" $(JAVA_HOME_ENV)/bin/qjava ptolemy.domains.de.demo.QueueApp

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
