# Makefile for the Ptolemy II Finite State Machine (FSM) Domain kernel
#
# @Authors: Thomas Huining Feng
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

ME =            ptolemy/domains/ptera/lib

# Root of the ptII directory
ROOT =		../../../..


# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

DIRS = 		

CLASSPATH =	$(ROOT)

# Used to build zip files
PTPACKAGE = 	lib
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS = \
	Configure.java \
	DebuggerParameter.java \
	EventQueueDebugger.java \
	EventUtils.java \
	ListDirectory.java \
	Plot.java \
	ReceiveInput.java \
	Report.java \
	SetTableau.java \
	Sleep.java \
	Stop.java \
	SynchronizeToRealtime.java \
	TableauParameter.java \
	Test.java

OTHER_FILES_TO_BE_JARED = \
	ConfigureIcon.xml \
	ListDirectoryIcon.xml \
	ListenToInput.xml \
	ListenToStop.xml \
	ParallelTasks.xml \
	PlotIcon.xml \
	ReceiveInputIcon.xml \
	RepeatForCount.xml \
	ReportIcon.xml \
	SendOutput.xml \
	SetTableauIcon.xml \
	SingleQueueMultipleServers.xml \
	SingleQueueMultipleServersTE.xml \
	SleepIcon.xml \
	StopIcon.xml \
	TestIcon.xml \
	TrafficLight.xml \
	pteraLibrary.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'Configure$$Listener.class' \
	'Configure$$Query.class' \
	'DebuggerParameter$$1.class' \
	'DebuggerParameter$$Mode$$1.class' \
	'DebuggerParameter$$Mode$$2.class' \
	'DebuggerParameter$$Mode$$3.class' \
	'DebuggerParameter$$Mode.class' \
	'EventUtils$$1.class' \
	'ListDirectory$$1.class' \
	'ListDirectory$$RecursiveFileFilter.class' \
	'ListDirectory$$RecursiveFileFilter$$FileComparator.class' \
	'Plot$$1.class' \
	'ReceiveInput$$InputListener.class' \
	'Report$$1.class' \
	'Report$$Mode$$1.class' \
	'Report$$Mode$$2.class' \
	'Report$$Mode$$3.class' \
	'Report$$Mode$$4.class' \
	'Report$$Mode$$5.class' \
	'Report$$Mode$$6.class' \
	'Report$$Mode.class' \
	'SetTableau$$TableauState$$1.class' \
	'SetTableau$$TableauState$$2.class' \
	'SetTableau$$TableauState$$3.class' \
	'SetTableau$$TableauState.class' \
	'SetTableau$$1.class' \
	'TableauParameter$$1.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: all jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
