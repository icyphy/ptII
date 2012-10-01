# Makefile for Java Ptolemy concurrent functional classes
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2011 The Regents of the University of California.
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


# $PTII/configure looks for antlr and sets PTPTALON_DIR in 
# $PTII/mk/ptII.mk to ptalon if it is found.
# Only go into ptalon if $(PTPTALON_DIR) is set by configure.
# If $(PTPTALON_DIR) is empty, then configure failed and compilation
# will fail.
#
DIRS = 		util sched process gui injection lib corba parameters gt designs test \
		$(PTPTALON_DIR)

# Root of the Ptolemy II directory
ROOT =		../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	actor
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	designs/designs.jar \
	gui/gui.jar \
	injection/injection.jar \
	lib/lib.jar \
	parameters/parameters.jar \
	process/process.jar \
	sched/sched.jar \
	util/util.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	AbstractInitializableAttribute.java \
	AbstractReceiver.java \
	Actor.java \
	ActorFiringListener.java \
	ApplicationConfigurer.java \
	AtomicActor.java \
	CausalityMarker.java \
	CustomRenderedPort.java \
	CompositeActor.java \
	ConstantPublisherPort.java \
	DesignPatternGetMoMLAction.java \
	Director.java \
	DoNothingDirector.java \
	Executable.java \
	ExecutionListener.java \
	FiringEvent.java \
	FiringsRecordable.java \
	GraphReader.java \
	IOPort.java \
	IOPortEvent.java \
	IOPortEventListener.java \
	IORelation.java \
	Initializable.java \
	InvariantViolationException.java \
	IntermediateReceiver.java \
	LazyTypedCompositeActor.java \
	LocalClock.java \
	Mailbox.java \
	Manager.java \
	NoRoomException.java \
	NoTokenException.java \
	PublisherPort.java \
	PubSubPort.java \
	QuantityManager.java \
	QuasiTransparentDirector.java \
	QueueReceiver.java \
	Receiver.java \
	RelationWidthInference.java \
	StateReceiver.java \
	StreamExecutionListener.java \
	SubscriberPort.java \
	SubscriptionAggregatorPort.java \
	SuperdenseTimeDirector.java \
	TimedActor.java \
	TokenGotEvent.java \
	TokenGotListener.java \
	TokenSentEvent.java \
	TokenSentListener.java \
	TypeAttribute.java \
	TypeConflictException.java \
	TypeEvent.java \
	TypeListener.java \
	TypeOpaqueCompositeActor.java \
	TypedActor.java \
	TypedAtomicActor.java \
	TypedCompositeActor.java \
	TypedIOPort.java \
	TypedIORelation.java

OTHER_FILES_TO_BE_JARED = \
	ActorModule.properties \
	JavaSEActorModule.properties

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	util sched process gt gui injection lib parameters designs test ptalon

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	corba \
	doc \
	'FiringEvent$$1.class' \
	'FiringEvent$$FiringEventType.class' \
	'LazyTypedCompositeActor$$ContainedObjectsIterator.class' \
	'Manager$$1.class' \
	'Manager$$ErrorReportingThread.class' \
	'Manager$$PtolemyRunThread.class' \
	'Manager$$State.class' \
	'Manager$$UnpauserThread.class' \
	'PublisherPort$$1.class' \
	'PubSubPort$$1.class' \
	'TypedIOPort$$1.class' \
	'TypedIOPort$$TypeTerm.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
