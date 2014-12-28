# Makefile for Ptolemy II Higher Order Components
#
# @Authors: (makefile only) Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2001-2014 The Regents of the University of California.
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

# This directory contains polymorphic higher order component actors.

ME =		ptolemy/actor/lib/hoc

DIRS = 		doc demo test

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	hoc
PTCLASSJAR =	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	ApplyFilterOverArray.java \
	ApplyFunction.java \
	ApplyFunctionOverSequence.java \
	Case.java \
	CaseDirector.java \
	CatchUpComposite.java \
	ExecuteActor.java \
	ExecuteCompositeActor.java \
	IterateOverArray.java \
	LifeCycleManager.java \
	MirrorComposite.java \
	MirrorPort.java \
	MobileFunction.java \
	MobileModel.java \
	ModelReference.java \
	ModelUtilities.java \
	MultiCompositeActor.java \
	MultiCompositePort.java \
	MultiInstanceComposite.java \
	RealTimeComposite.java \
	ReflectComposite.java \
	Refinement.java \
	RefinementPort.java \
	RunCompositeActor.java \
	ThreadedComposite.java

OTHER_FILES_TO_BE_JARED = \
	ApplyFunctionIcon.xml \
	CaseIcon.xml \
	ExecuteActorIcon.xml \
	MobileFunctionIcon.xml \
	MobileModelIcon.xml \
	ModelReferenceIcon.xml \
	RunCompositeActorIcon.xml \
	hoc.xml \
	hocSimple.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =	doc test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	doc \
	'AbstractCase$$CaseDirector.class' \
	'ApplyFunction$$1.class' \
	'ApplyFunction$$ReturnTypeFunction.class' \
	'CaseDirector$$1.class' \
	'IterateOverArray$$1.class' \
	'IterateOverArray$$2.class' \
	'IterateOverArray$$3.class' \
	'IterateOverArray$$GLBArrayElementFunction.class' \
	'IterateOverArray$$GLBArrayFunction.class' \
	'IterateOverArray$$IterateComposite.class' \
	'IterateOverArray$$IterateComposite$$1.class' \
	'IterateOverArray$$IterateDirector.class' \
	'IterateOverArray$$IteratePort.class' \
	'MirrorComposite$$1.class' \
	'MirrorComposite$$2.class' \
	'MirrorComposite$$MirrorCompositeContents$$1.class' \
	'MirrorComposite$$MirrorCompositeContents.class' \
	'ModelReference$$1.class' \
	'RealTimeComposite$$InputFrame.class' \
	'RealTimeComposite$$OutputFrame.class' \
	'RealTimeComposite$$QueuedToken.class' \
	'RealTimeComposite$$RealTimeDirector.class' \
	'RealTimeComposite$$RealTimeDirector$$RealTimeThread.class' \
	'ReflectComposite$$1.class' \
	'ReflectComposite$$2.class' \
	'ReflectComposite$$ReflectCompositeContents$$1.class' \
	'ReflectComposite$$ReflectCompositeContents.class' \
	'ThreadedComposite$$1.class' \
	'ThreadedComposite$$QueuedToken.class' \
	'ThreadedComposite$$ThreadedDirector$$CompositeThread.class' \
	'ThreadedComposite$$ThreadedDirector.class' \
	'ThreadedComposite$$TokenFrame$$FrameType.class' \
	'ThreadedComposite$$TokenFrame.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

