# Makefile for the Metropolis II Ptolemy kernel
#
# @Authors: Christopher Brooks (makefile only)
#
# @Version: $Id: makefile 64744 2012-10-01 22:51:43Z cxh $
#
# @Copyright (c) 2012 The Regents of the University of California.
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
    ActMachine.java \
    BlockingActor.java \
    BlockingFire.java \
    ConstraintSolver.java \
    FireMachine.java \
    MappingConstraintSolver.java \
    MetroIICompositeActor.java \
    MetroIIDEDirector.java \
    MetroIIDEDirectorForPtides.java \
    MetroIIDirector.java \
    MetroIIEventHandler.java \
    MetroIIFSMDirector.java \
    MetroIIMirrorComposite.java \
    MetroIIModalModel.java \
    MetroIIPNDirector.java \
    MetroIIPNQueueReceiver.java \
    MetroIIPtidesDirector.java \
    MetroIIPtidesListEventQueue.java \
    MetroIIPtidesPlatform.java \
    MetroIIPtidesPort.java \
    MetroIIPtidesReceiver.java \
    MetroIISRDirector.java \
    MetroIISystemCDirector.java \
    NonBlockingFire.java \
    ResumableActor.java \
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
	'ActMachine.class' \
	'ActMachine$State.class' \
	'BlockingActor.class' \
    'BlockingFire.class' \
    'ConstraintSolver.class' \
    'FireMachine.class' \
    'FireMachine$State.class' \
    'MappingConstraintSolver.class' \
    'MetroIICompositeActor$1.class' \
    'MetroIICompositeActor.class' \
    'MetroIIDEDirector.class' \
    'MetroIIDEDirectorForPtides.class' \
    'MetroIIDEDirectorForPtides$Pair.class' \
    'MetroIIDEDirector$Pair.class' \
    'MetroIIDirector.class' \
    'MetroIIEventHandler.class' \
    'MetroIIFSMDirector$1.class' \
    'MetroIIFSMDirector.class' \
    'MetroIIMirrorComposite$1.class' \
    'MetroIIMirrorComposite$2.class' \
    'MetroIIMirrorComposite.class' \
    'MetroIIMirrorComposite$MetroIIMirrorCompositeContents$1.class' \
    'MetroIIMirrorComposite$MetroIIMirrorCompositeContents.class' \
    'MetroIIModalModel$1.class' \
    'MetroIIModalModel.class' \
    'MetroIIPNDirector$1.class' \
    'MetroIIPNDirector.class' \
    'MetroIIPNQueueReceiver.class' \
    'MetroIIPtidesDirector.class' \
    'MetroIIPtidesListEventQueue.class' \
    'MetroIIPtidesPlatform.class' \
    'MetroIIPtidesPlatform$PtidesMirrorPort.class' \
    'MetroIIPtidesPlatform$PtidesNetworkType.class' \
    'MetroIIPtidesPlatform$PtidesPlatformContents.class' \
    'MetroIIPtidesPlatform$PtidesPlatformDirector.class' \
    'MetroIIPtidesPort.class' \
    'MetroIIPtidesReceiver.class' \
    'MetroIIPtidesReceiver$Event.class' \
    'MetroIIPtidesReceiver$TagComparator.class' \
    'MetroIISRDirector$1.class' \
    'MetroIISRDirector.class' \
    'MetroIISystemCDirector$1.class' \
    'MetroIISystemCDirector.class' \
    'NonBlockingFire.class' \
    'ResumableFire.class' \
    'StartOrResumable.class' \
	'TimeScheduler.class' 


JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
