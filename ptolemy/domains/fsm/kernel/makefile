# Makefile for the Ptolemy II Finite State Machine (FSM) Domain kernel
#
# @Authors: Xiaojun Liu
#
# @Version: $Id$
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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

ME =            ptolemy/domains/fsm/kernel

# Root of the ptII directory
ROOT =		../../../..


# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

DIRS = 		util test # demo

CLASSPATH =	../../../..

# Used to build zip files
PTPACKAGE = 	kernel
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		util/util.jar

PTCLASSALLJAR = kernel.jar

JSRCS = \
	Action.java \
	ChoiceAction.java \
	CommitAction.java \
	FSMActor.java \
	FSMController.java \
	FSMDirector.java \
	FSMState.java \
	FSMTransition.java \
	HDFFSMController.java \
	HDFFSMDirector.java \
	HDFFSMState.java \
	HDFFSMTransition.java \
	NewFSMDirector.java \
	State.java \
	Transition.java \
	TransitionAction.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc	

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)


depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
