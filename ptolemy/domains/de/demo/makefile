# Makefile for Ptolemy II Continuous Time domain demos
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
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

ME =		ptolemy/domains/ct/demo

# Get configuration info
CONFIG =	$(ROOT)/mk/tycho.mk
include $(CONFIG)

CLASSPATH = ../../../..

TYPACKAGE_DIR = ptolemy/domains/de/demo

JSRCS = \
	ClockApplet.java \
	ClockSystem.java

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
	clockapplet.jar	

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass clockapplet.jar
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

# Classfiles used by the ClockApplet demo
CLOCK_CLASSFILES = \
	collections/Assertable.class \
	collections/CEImpl.class \
	collections/Cell.class \
	collections/Collection.class \
	collections/CollectionEnumeration.class \
	collections/Comparator.class \
	collections/CorruptedEnumerationException.class \
	collections/IllegalElementException.class \
	collections/ImplementationCheckable.class \
	collections/LLCell.class \
	collections/LLCellEnumeration.class \
	collections/LinkedList.class \
	collections/Predicate.class \
	collections/Seq.class \
	collections/SortableCollection.class \
	collections/UpdatableCollection.class \
	collections/UpdatableImpl.class \
	collections/UpdatableSeq.class \
	collections/UpdatableSeqImpl.class \
	ptolemy/actor/Actor.class \
	ptolemy/actor/ActorListener.class \
	ptolemy/actor/AtomicActor.class \
	ptolemy/actor/CompositeActor.class \
	ptolemy/actor/Director.class \
	ptolemy/actor/Executable.class \
	ptolemy/actor/IOPort.class \
	ptolemy/actor/IORelation.class \
	ptolemy/actor/Receiver.class \
	ptolemy/actor/util/CQComparator.class \
	'ptolemy/actor/util/CalendarQueue$$CQEntry.class' \
	'ptolemy/actor/util/CalendarQueue$$EntryComparator.class' \
	'ptolemy/actor/util/CalendarQueue$$SortedLinkedList.class' \
	ptolemy/actor/util/CalendarQueue.class \
	ptolemy/actor/util/FIFOQueue.class \
	ptolemy/data/DoubleToken.class \
	ptolemy/data/IntToken.class \
	ptolemy/data/ScalarToken.class \
	ptolemy/data/Token.class \
	ptolemy/data/TokenPublisher.class \
	'ptolemy/domains/de/demo/ClockApplet$$GoButtonListener.class' \
	'ptolemy/domains/de/demo/ClockApplet$$StopTimeListener.class' \
	ptolemy/domains/de/demo/ClockApplet.class \
	ptolemy/domains/de/demo/ClockApplet.class \
	ptolemy/domains/de/kernel/DEActor.class \
	'ptolemy/domains/de/kernel/DECQDirector$$DECQComparator.class' \
	'ptolemy/domains/de/kernel/DECQDirector$$DEEvent.class' \
	ptolemy/domains/de/kernel/DECQDirector.class \
	ptolemy/domains/de/kernel/DEDirector.class \
	ptolemy/domains/de/kernel/DEIOPort.class \
	ptolemy/domains/de/kernel/DEReceiver.class \
	ptolemy/domains/de/kernel/DESortKey.class \
	ptolemy/domains/de/lib/DEClock.class \
	ptolemy/domains/de/lib/DEPlot.class \
	ptolemy/domains/de/lib/DEPoisson.class \
	ptolemy/domains/de/lib/DEWaitingTime.class \
	ptolemy/graph/DirectedGraph.class \
	ptolemy/graph/Graph.class \
	ptolemy/kernel/ComponentEntity.class \
	ptolemy/kernel/ComponentPort.class \
	ptolemy/kernel/ComponentRelation.class \
	ptolemy/kernel/CompositeEntity.class \
	ptolemy/kernel/Entity.class \
	ptolemy/kernel/Port.class \
	ptolemy/kernel/Relation.class \
	ptolemy/kernel/mutation/Mutation.class \
	ptolemy/kernel/mutation/MutationListener.class \
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
	'ptolemy/kernel/util/Workspace$$ReadDepth.class' \
	ptolemy/kernel/util/Workspace.class \
	ptolemy/plot/CmdLineArgException.class \
	ptolemy/plot/Plot.class \
	'ptolemy/plot/PlotBox$$DragListener.class' \
	'ptolemy/plot/PlotBox$$FillButtonListener.class' \
	'ptolemy/plot/PlotBox$$ZoomListener.class' \
	ptolemy/plot/PlotBox.class \
	ptolemy/plot/PlotPoint.class \

# 'make clean' removes everything in KRUFT
KRUFT = clockapplet.jar

# Jar file consisting of classes used by an applet.  Basically,
# everything but Message and Pxgraph.
clockapplet.jar: $(JSRCS) $(JCLASS)
	(cd $(CLASSPATH); \
		$(JAR) cf \
		$(TYPACKAGE_DIR)/clockapplet.jar \
		$(CLOCK_CLASSFILES))
depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/tycommon.mk
