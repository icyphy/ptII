# Makefile for Ptolemy II Communicating Sequential Processes demos
#
# @Authors: Christopher Hylands, Neil Smyth
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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

# Note that the Java classes contained in this directory were written
# by Sun.  These classes came from tcljava, see license.terms for more info.

# Root of the Java directory
ROOT =		../../../..

ME =		ptolemy/domains/csp/demo

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

CLASSPATH = ../../../..

JAVA_HOME_ENV = /usr/tools/pure/jdk1.1.6
QUANTIFY_JAVA_HOME_ENV = /usr/tools/pure/quantify-4.2-solaris2

PTPACKAGE_DIR = ptolemy/domains/csp/demo

JSRCS = \
	BasicDemo.java \
	BusContentionDemo.java \
	Coordinate.java \
	DiningApplet.java \
	DiningPhilosophers.java \
	Eratosthenes.java \
	MM1.java \
	PausingMM1.java \
	PhilosopherListener.java \
	TablePanel.java \

# Non-graphical Java demos written in Tcl
# Since there are no demos, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a demo, be sure to add
# $(JSIMPLE_TESTS) to EXTRA_SRCS
JSIMPLE_TESTS = \
	dummy.tcl

# Graphical Java demos that use Tcl.
JGRAPHICAL_TESTS = \
	BasicDemo.tcl


EXTRA_SRCS =	index.html $(TCL_SRCS) $(JSRCS) $(JGRAPHICAL_TESTS) \
		#$(JSIMPLE_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	doc

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	diningapplet.jar

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass
	@echo "To run all the demos, run 'make demo'"

install: all

demo: demo_jgraphical

# These demos do not require a graphics terminal, but do use TclBlend and Java
demo_jgraphical: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl
	CLASSPATH=$(CLASSPATH)$(AUXCLASSPATH) $(JTCLSH) alljtests.tcl

# This is a graphical test
Exponential: $(EXTRA_SRCS) jclass Exponential.tcl
	CLASSPATH=$(CLASSPATH)$(AUXCLASSPATH) $(JTCLSH) Exponential.tcl

docs: javadocs


# Classfiles used by the DiningApplet demo
DINING_CLASSFILES = \
	ptolemy/actor/Actor.class \
	ptolemy/actor/AtomicActor.class \
	ptolemy/actor/CompositeActor.class \
	ptolemy/actor/Director.class \
	ptolemy/actor/Executable.class \
	ptolemy/actor/IOPort.class \
	ptolemy/actor/IORelation.class \
	ptolemy/actor/Manager.class \
	ptolemy/actor/process/ProcessDirector.class \
	ptolemy/data/DoubleToken.class \
	ptolemy/data/ScalarToken.class \
	ptolemy/data/Token.class \
	ptolemy/data/expr/Parameter.class \
	ptolemy/data/expr/ParameterListener.class \
	ptolemy/domains/csp/demo/Coordinate.class \
	ptolemy/domains/csp/demo/PhilosopherListener.class \
	'ptolemy/domains/csp/demo/TablePanel$$ChopstickCoords.class' \
	'ptolemy/domains/csp/demo/TablePanel$$PhilosopherCoords.class' \
	ptolemy/domains/csp/demo/TablePanel.class \
	ptolemy/domains/csp/kernel/CSPActor.class \
	ptolemy/domains/csp/kernel/CSPDirector.class \
	ptolemy/domains/csp/kernel/CSPReceiver.class \
	ptolemy/domains/csp/kernel/ConditionalBranch.class \
	ptolemy/domains/csp/kernel/ConditionalReceive.class \
	ptolemy/domains/csp/kernel/ConditionalSend.class \
	ptolemy/domains/csp/lib/CSPChopstick.class \
	ptolemy/domains/csp/lib/CSPPhilosopher.class \
	ptolemy/kernel/ComponentEntity.class \
	ptolemy/kernel/ComponentPort.class \
	ptolemy/kernel/ComponentRelation.class \
	ptolemy/kernel/CompositeEntity.class \
	ptolemy/kernel/Entity.class \
	ptolemy/kernel/Port.class \
	ptolemy/kernel/Relation.class \
	ptolemy/kernel/util/Attribute.class \
	'ptolemy/kernel/util/CrossRefList$$CrossRef.class' \
	'ptolemy/kernel/util/CrossRefList$$CrossRefEnumeration.class' \
	ptolemy/kernel/util/CrossRefList.class \
	ptolemy/kernel/util/IllegalActionException.class \
	ptolemy/kernel/util/InternalErrorException.class \
	ptolemy/kernel/util/InvalidStateException.class \
	ptolemy/kernel/util/KernelException.class \
	ptolemy/kernel/util/NameDuplicationException.class \
	ptolemy/kernel/util/Nameable.class \
	ptolemy/kernel/util/NamedList.class \
	ptolemy/kernel/util/NamedObj.class \
	ptolemy/kernel/util/NoSuchItemException.class \
	ptolemy/kernel/util/PtolemyThread.class \
	'ptolemy/kernel/util/Workspace$$ReadDepth.class' \
	ptolemy/kernel/util/Workspace.class \
	

# 'make clean' removes everything in KRUFT
KRUFT = diningapplet.jar

# Jar file consisting of classes used by an applet.  Basically,
# everything but Message and Pxgraph.
diningapplet.jar: $(JSRCS) $(JCLASS)
	(cd $(CLASSPATH); \
		$(JAR) cf \
		$(PTPACKAGE_DIR)/diningapplet.jar \
		$(DINING_CLASSFILES))

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk



