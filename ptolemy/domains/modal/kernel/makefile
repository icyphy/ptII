# Makefile for the Ptolemy II Finite State Machine (FSM) Domain kernel
#
# @Authors: Xiaojun Liu
#
# @Version: $Id$
#
# @Copyright (c) 1998-2013 The Regents of the University of California.
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

ME =            ptolemy/domains/modal/kernel

# Root of the ptII directory
ROOT =		../../../..


# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

DIRS = 		fmv ia test

CLASSPATH =	$(ROOT)

# Used to build zip files
PTPACKAGE = 	kernel
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		fmv/fmv.jar \
		ia/ia.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	AbstractActionsAttribute.java \
	Action.java \
	ChoiceAction.java \
	CommitAction.java \
	CommitActionsAttribute.java \
	ConfigurableEntity.java \
	Configurer.java \
	ContainmentExtender.java \
	FSMActor.java \
	FSMCausalityInterface.java \
	FSMDirector.java \
	FSMReceiver.java \
	ModalDirector.java \
	MultipleEnabledTransitionsException.java \
	MultirateFSMDirector.java \
	NonStrictFSMDirector.java \
	OutputActionsAttribute.java \
	ParseTreeEvaluatorForGuardExpression.java \
	RefinementActor.java \
	RelationList.java \
	RelationNode.java \
	RelationType.java \
	State.java \
	StateEvent.java \
	TransientState.java \
	Transition.java

OTHER_FILES_TO_BE_JARED = \
	StateIcon.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'AbstractActionsAttribute$$TypeFunction.class' \
	'Configurer$$ContainmentExtender.class' \
	'FSMActor$$1.class' \
	'FSMActor$$PortScope.class' \
	'FSMDirector$$1.class' \
	'FSMActor$$ProbabilityFunction.class' \
	'FSMActor$$ProbabilityFunctionToken.class' \
	'FSMActor$$TimeoutFunction.class' \
	'FSMActor$$TimeoutFunctionToken.class' \
	'State$$UpdateContentsRequest.class' \
	'State$$InvisibleModalModel.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: all jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
