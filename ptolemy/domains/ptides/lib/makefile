# Makefile for the PTIDES domain library
# PTIDES: Programming Temporally Integrated Distributed Embedded Systems
#
# @Authors: Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2008-2012 The Regents of the University of California.
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

ME =		ptolemy/domains/ptides/lib

DIRS =		io luminary qm test 

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	lib
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	io/io.jar \
	luminary/luminary.jar \
	qm/qm.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	ActuationDevice.java \
	ActuatorSetup.java \
	DynamicCoreAssignmentScheduler.java \
	ErrorHandlingAction.java \
	FPPCore.java \
	InputDevice.java \
	MulticoreExecutionTimeMonitor.java \
	NetworkReceiver.java \
	NetworkTransmitter.java \
	NPFCFSCore.java \
	OutputDevice.java \
	PEDFCore.java \
	PtidesPort.java \
	SensorHandler.java \
	SchedulerDirector.java \
	SchedulerModel.java \
	ExecutionTimeListener.java \
	ExecutionTimeMonitor.java \
	PlatformTimeMonitor.java 

OTHER_FILES_TO_BE_JARED = \
	ptidesentities.xml \
	DynamicCoreAssignmentSchedulerIcon.xml \
	FPPCoreIcon.xml \
	NPFCFSCoreIcon.xml \
	PEDFCoreIcon.xml \
	SchedulerModelIcon.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	io luminary qm test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'ErrorHandlingAction$$ErrorHandlingActionString.class' \
	'ExecutionTimeListener$$ExecutionEventType.class' \
	'ExecutionTimeMonitor$$SchedulePlotterEditorFactory.class' \
	'MulticoreExecutionTimeMonitor$$1.class' \
	'MulticoreExecutionTimeMonitor$$2.class' \
	'MulticoreExecutionTimeMonitor$$3.class' \
	'MulticoreExecutionTimeMonitor$$4.class' \
	'MulticoreExecutionTimeMonitor$$ParallelMonitor.class' \
	'NetworkReceiver$$1.class' \
	'NetworkReceiver$$PortFunction.class' \
	'PlatformTimeMonitor$$PlatformTimePlotterEditorFactory.class' \
	'ScheduleListener$$ScheduleEventType.class' \
	'SchedulePlotter$$SchedulePlotterEditorFactory.class'

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
