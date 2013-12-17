# Makefile for the Metropolis II Ptolemy kernel
#
# @Authors: Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2012-2013 The Regents of the University of California.
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

ME =		ptolemy/domains/metroII/kernel

DIRS =		util test

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(PTPROTOBUF_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	kernel
#PTCLASSJAR =	$(PTPACKAGE).jar
PTCLASSALLJAR = $(PTPACKAGE).jar
# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	util/util.jar

JSRCS = \
	BlockingFire.java \
	ConstraintSolver.java \
	EventDictionary.java \
	FireMachine.java \
	GetFirable.java \
	Graph.java \
	MappingConstraintSolver.java \
	MetroIIDebugger.java \
	MetroIIEventBuilder.java \
	MetroIIFSMDirector.java \
	MetroIICompositeActor.java \
	MetroIIDEDirector.java \
	MetroIIDEDirectorForPtides.java \
	MetroIIDirector.java \
	MetroIIMirrorComposite.java \
	MetroIIPNDirector.java \
	MetroIIPNQueueReceiver.java \
	MetroIIPtidesDirector.java \
	MetroIIPtidesListEventQueue.java \
	MetroIIPtidesPlatform.java \
	MetroIIPtidesPort.java \
	MetroIIPtidesReceiver.java \
	MetroIISRDirector.java \
	MetroIISystemCDirector.java \
	MetroIIModalModel.java \
	MetroIISequentialSDFDirector.java \
	NonBlockingFire.java \
	Pair.java \
	ResumableFire.java \
	StartOrResumable.java \
	TimeScheduler.java

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
    doc \
	'ActMachine$$1.class' \
	'ActMachine$$State.class' \
	'FireMachine$$1.class' \
	'FireMachine$$State.class' \
	'MappingConstraintSolver$$ConstraintCounter.class' \
	'MetroIIFSMDirector$$1.class' \
	'MetroIICompositeActor$$1.class' \
	'MetroIIDEDirectorForPtides$$Pair.class' \
	'MetroIIMirrorComposite$$1.class' \
	'MetroIIMirrorComposite$$2.class' \
	'MetroIIMirrorComposite$$MetroIIMirrorCompositeContents$$1.class' \
	'MetroIIMirrorComposite$$MetroIIMirrorCompositeContents.class' \
	'MetroIIModalModel$$1.class' \
	'MetroIIPNDirector$$1.class' \
	'MetroIIPtidesPlatform$$PtidesMirrorPort.class' \
	'MetroIIPtidesPlatform$$PtidesNetworkType.class' \
	'MetroIIPtidesPlatform$$PtidesPlatformContents.class' \
	'MetroIIPtidesPlatform$$PtidesPlatformDirector.class' \
	'MetroIIPtidesReceiver$$Event.class' \
	'MetroIIPtidesReceiver$$TagComparator.class' \
	'MetroIISequentialSDFDirector$$1.class' \
	'MetroIISRDirector$$1.class' \
	'MetroIISystemCDirector$$1.class' \
	'MetroModalModel$$1.class' \
	'MetroSequentialSDFDirector$$1.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
