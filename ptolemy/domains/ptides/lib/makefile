# Makefile for the PTIDES domain library
# PTIDES: Programming Temporally Integrated Distributed Embedded Systems
#
# @Authors: Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2008-2009 The Regents of the University of California.
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

DIRS =		luminary test

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
	luminary/luminary.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	AbsoluteDelay.java \
	ActuationDevice.java \
	ActuatorSetup.java \
	InputDevice.java \
	MasterClock.java \
	NetworkReceiver.java \
	NetworkTransmitter.java \
	OutputDevice.java \
	RealisticSlaveClock.java \
	SensorHandler.java \
	SimulatedPlatformPhysicalTime.java \
	SlaveClock.java \
	VariableDelayCounter.java \
	ExecutionTimeListener.java \
	ExecutionTimeMonitor.java

OTHER_FILES_TO_BE_JARED = \
	ptidesentities.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	luminary test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'ExecutionTimeListener$$ExecutionEventType.class' \
	'ExecutionTimeMonitor$$SchedulePlotterEditorFactory.class' \
	'NetworkReceiver$$1.class' \
	'NetworkReceiver$$PortFunction.class' \
	'ScheduleListener$$ScheduleEventType.class' \
	'SchedulePlotter$$SchedulePlotterEditorFactory.class'

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
