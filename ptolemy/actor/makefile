# Makefile for Java Ptolemy concurrent functional classes
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2003 The Regents of the University of California.
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

ME =		ptolemy/actor

DIRS = 		util sched process gui lib corba parameters test

# Root of the Ptolemy II directory
ROOT =		../..

CLASSPATH =	$(ROOT)
# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	actor
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	hoc/hoc.jar \
	gui/gui.jar \
	lib/lib.jar \
	parameters/parameters.jar \
	process/process.jar \
	sched/sched.jar \
	util/util.jar 

PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	AbstractReceiver.java \
	Actor.java \
	AtomicActor.java \
	CompositeActor.java \
	Director.java \
	Executable.java \
	ExecutionListener.java \
	FiringEvent.java \
	GraphReader.java \
	InvariantViolationException.java \
	IODependence.java \
	IOPort.java \
	IORelation.java \
	Mailbox.java \
	Manager.java \
	NoRoomException.java \
	NoTokenException.java \
	QueueReceiver.java \
	Receiver.java \
	RunCompositeActor.java \
	StreamExecutionListener.java \
	TypeAttribute.java \
	TypeConflictException.java \
	TypedActor.java \
	TypedAtomicActor.java \
	TypedCompositeActor.java \
	TypedIOPort.java \
	TypedIORelation.java \
	TypeEvent.java \
	TypeListener.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	util sched process hoc gui lib parameters test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	corba \
	doc \
	'FiringEvent$$1.class' \
	'FiringEvent$$FiringEventType.class' \
	'Manager$$1.class' \
	'Manager$$2.class' \
	'Manager$$State.class' \
	'TypedIOPort$$1.class' \
	'TypedIOPort$$TypeTerm.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
