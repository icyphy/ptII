# Makefile for the Discrete Event domain actor library
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2014 The Regents of the University of California.
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

ME =		ptolemy/domains/de/lib

DIRS = 		aspect test #demo

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)


# Used to build jar files
PTPACKAGE = 	lib
#PTCLASSJAR = 	$(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		aspect/aspect.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	AverageOverTime.java \
	DateToEvent.java \
	DateToModelTime.java \
	Derivative.java \
	DETransformer.java \
	EventButton.java \
	EventFilter.java \
	EventToDate.java \
	GetCurrentMicrostep.java \
	Inhibit.java \
	Integrator.java \
	Merge.java \
	MostRecent.java \
	NonInterruptibleTimer.java \
	PID.java \
	Previous.java \
	Queue.java \
	Register.java \
	Server.java \
	SharedQueue.java \
	SuppressSimultaneousEvents.java \
	TestActorPortDepth.java \
	TimeCompare.java \
	TimeGap.java \
	TimedDelay.java \
	Timer.java \
	VariableDelay.java \
	WaitingTime.java \
	Wire.java

OTHER_FILES_TO_BE_JARED = \
	DateToEventIcon.xml \
	DateToModelTime.xml \
	deentities.xml \
	DerivativeIcon.xml \
	DerivativeIcon.gif \
	IntegratorIcon.gif \
	IntegratorIcon.xml \
	NonInterruptibleTimerIcon.xml \
	PIDicon.xml \
	PIDicon.gif \
	PreviousIcon.xml \
	QueueIcon.xml \
	ServerIcon.xml \
	SharedQueueIcon.xml \
	TimeGapIcon.xml \
	TimedDelayIcon.xml \
	TimerIcon.xml \
	VariableDelayIcon.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	 $(DIRS) \


# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'EventButton$$1.class' \
	'EventButton$$ButtonListener.class' \
	'Server$$Job.class'

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
