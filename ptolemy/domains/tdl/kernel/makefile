# Makefile for the Timing Definition Language domain kernel
#
#
# @Authors: Patricia Derler (makefile only)
#
# @Version: $Id:makefile 50570 2008-09-04 03:18:09Z derler $
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

ME =		ptolemy/domains/tdl/kernel

DIRS =

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	kernel
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS = \
	LetTask.java \
	TDLAction.java \
	TDLActionsGraph.java \
	TDLActor.java  \
	TDLCausalityInterface.java \
	TDLCodeGenerator.java \
	TDLCodeGeneratorUtilities.java \
	TDLController.java \
	TDLMode.java \
	TDLModule.java \
	TDLModuleDirector.java \
	TDLReceiver.java \
	TDLRefinement.java \
	TDLRefinementPort.java \
	TDLTask.java \
	TDLTaskPort.java \
	TDLTransition.java 

OTHER_FILES_TO_BE_JARED = \
	tdlentities.xml \
	TDLModeIcon.xml 

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) #test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'TDLAction$$TDLActionComparator.class' \
	'TDLCodeGenerator$$TDLEditorFactory.class' \
	'TDLModule$$1.class'

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
