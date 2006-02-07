# Makefile for the ptolemy/vergil/fsm package
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2001-2006 The Regents of the University of California.
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


ME =		ptolemy/vergil/fsm

DIRS =		ia modal

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	fsm
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		ia/ia.jar \
		modal/modal.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	Arc.java \
	CaseGraphFrame.java \
	CaseGraphTableau.java \
	FSMGraphController.java \
	FSMGraphFrame.java \
	FSMGraphModel.java \
	FSMGraphTableau.java \
	FSMViewerGraphController.java \
	StateController.java \
	TransitionController.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	'FSMGraphController$$1.class' \
	'FSMGraphController$$LinkCreator.class' \
	'FSMGraphController$$NewStateAction.class' \
	'FSMGraphFrame$$DebugMenuListener.class' \
	'FSMGraphModel$$1.class' \
	'FSMGraphModel$$2.class' \
	'FSMGraphModel$$3.class' \
	'FSMGraphModel$$4.class' \
	'FSMGraphModel$$5.class' \
	'FSMGraphModel$$6.class' \
	'FSMGraphModel$$7.class' \
	'FSMGraphModel$$8.class' \
	'FSMGraphModel$$ArcModel.class' \
	'FSMGraphModel$$PortModel.class' \
	'FSMGraphModel$$StateModel.class' \
	'FSMGraphTableau$$Factory.class' \
	'StateController$$1.class' \
	'StateController$$LookInsideAction.class' \
	'StateController$$StateRenderer.class' \
	'TransitionController$$1.class' \
	'TransitionController$$LinkDropper.class' \
	'TransitionController$$LinkRenderer.class' \
	'TransitionController$$LinkTarget.class' \
	'TransitionController$$LookInsideAction.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
