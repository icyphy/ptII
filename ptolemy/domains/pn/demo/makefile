# Makefile for the Java classes used to implement the Ptolemy PN domain kernel
#
# @Authors: Mudit Goel, Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1997-1998 The Regents of the University of California.
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

# Root of the Java directory
ROOT =		../../../..

ME =		java/ptolemy/domains/pn/demo

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)
#JAVAHOME =      /opt/jdk1.2latest

DIRS = 		# demo test doc

# Used to build zip files
JPACKAGE = 	ptolemy.domains.pn.demo
JPACKAGE_DIR = 	ptolemy/domains/pn/demo
PTPACKAGE_DIR = ptolemy/domains/pn/demo
JVERSION =	1.0
JDIST =		$(JPACKAGE)$(JVERSION)
JZIP =		$(JPACKAGE_DIR)/$(JPACKAGE).zip
PTCLASSJAR = 	$(JPACKAGE).jar

CLASSPATH = ../../../..

JSRCS = \
	PNInterleavingExample.java \
	PNPrimeExample.java \
	RLEncoding.java \
	RLEncodingApplet.java \
	TimedPrimeExample.java \
	PNPrimeGalaxy.java \
	PNWavepacket.java

EXTRA_SRCS =	$(JSRCS)

# Files or directories that are present, but that 'make checkjunk'
# should not complain about
MISC_FILES =	$(DIRS) doc #test

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	RLEncodingApplet.jar

JCLASS = $(JSRCS:%.java=%.class)

all: jclass

install: jclass javadocs
docs: javadocs

# Classfiles used by the ClockApplet demo
RLE_CLASSFILES = \
        collections/Assertable.class \
        collections/CEImpl.class \
        collections/Cell.class \
        collections/Collection.class \
        collections/CollectionEnumeration.class \
        collections/Comparator.class \
        collections/CorruptedEnumerationException.class \
        collections/HashedMap.class \
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
        collections/UpdatableMap.class \
        collections/UpdatableMapImpl.class \
        collections/UpdatableSeq.class \
        collections/UpdatableSeqImpl.class \
        ptolemy/actor/Actor.class \
        ptolemy/actor/AtomicActor.class \
        ptolemy/actor/CompositeActor.class \
        ptolemy/actor/Director.class \
        ptolemy/actor/Executable.class \
        ptolemy/actor/IOPort.class \
        ptolemy/actor/IORelation.class \
        ptolemy/actor/Manager.class \
        ptolemy/actor/NoRoomException.class \
        ptolemy/actor/NoTokenException.class \
        ptolemy/actor/Receiver.class \
        ptolemy/actor/process/ProcessDirector.class \
        ptolemy/actor/process/ProcessThread.class \
        ptolemy/actor/process/TerminateProcessException.class \
        ptolemy/actor/process/NotifyThread.class \
        ptolemy/actor/util/CQComparator.class \
        'ptolemy/actor/util/CalendarQueue$$CQEntry.class' \
        'ptolemy/actor/util/CalendarQueue$$CQLinkedList.class' \
        ptolemy/actor/util/CalendarQueue.class \
        ptolemy/actor/util/FIFOQueue.class \
        ptolemy/data/BooleanMatrixToken.class \
        ptolemy/data/BooleanToken.class \
        ptolemy/data/ComplexToken.class \
        ptolemy/data/ComplexMatrixToken.class \
        ptolemy/data/DoubleToken.class \
        ptolemy/data/DoubleMatrixToken.class \
        ptolemy/data/IntMatrixToken.class \
        ptolemy/data/IntToken.class \
        ptolemy/data/LongMatrixToken.class \
        ptolemy/data/LongToken.class \
        ptolemy/data/MatrixToken.class \
        ptolemy/data/Numerical.class \
        ptolemy/data/ObjectToken.class \
        ptolemy/data/ScalarToken.class \
        ptolemy/data/StringToken.class \
        ptolemy/data/Token.class \
        ptolemy/data/TypeLattice.class \
        ptolemy/data/expr/Parameter.class \
        ptolemy/data/expr/ParameterListener.class \
        'ptolemy/domains/de/demo/ClockApplet$$GoButtonListener.class' \
        'ptolemy/domains/de/demo/ClockApplet$$StopTimeListener.class' \
        ptolemy/domains/de/demo/ClockApplet.class \
        'ptolemy/domains/de/demo/HistogramApplet$$CurrentTimeThread.class' \
        'ptolemy/domains/de/demo/HistogramApplet$$GoButtonListener.class' \
        'ptolemy/domains/de/demo/HistogramApplet$$IntervalSbListener.class' \
        ptolemy/domains/de/demo/HistogramApplet.class \
        ptolemy/domains/pn/kernel/BasePNDirector.class \
        ptolemy/domains/pn/kernel/DefaultPNListener.class \
        ptolemy/domains/pn/kernel/PNProcessEvent.class \
        ptolemy/domains/pn/kernel/PNProcessListener.class \
        ptolemy/domains/pn/kernel/PNQueueReceiver.class \
        ptolemy/domains/pn/kernel/ReceiverActionEvent.class \
        ptolemy/domains/pn/kernel/ReceiverListener.class \
	'ptolemy/domains/pn/lib/ImageDisplay$$_PictureFrame.class' \
	ptolemy/domains/pn/lib/ImageDisplay.class \
	ptolemy/domains/pn/lib/MatrixPacker.class \
	ptolemy/domains/pn/lib/MatrixUnpacker.class \
	ptolemy/domains/pn/lib/PNImageSink.class \
	ptolemy/domains/pn/lib/PNImageSource.class \
	ptolemy/domains/pn/lib/RLDecoder.class \
	ptolemy/domains/pn/lib/RLEncoder.class \
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
	ptolemy/media/Picture.class \
	/users/ptII/ptolemy/domains/pn/lib/test/ptII.pbm

# 'make clean' removes everything in KRUFT
KRUFT = RLEncodingApplet.jar

# Jar file consisting of classes used by an applet.  Basically,
# everything but Message and Pxgraph.
RLEncodingApplet.jar: $(JSRCS) $(JCLASS)
		(cd $(CLASSPATH); \
                $(JAR) cf \
                $(PTPACKAGE_DIR)/RLEncodingApplet.jar \
                $(RLE_CLASSFILES))

depend:
	@echo "no dependencies in this directory"



# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk







