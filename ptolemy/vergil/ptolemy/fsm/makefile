# Makefile for the Vergil package
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2001 The Regents of the University of California.
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

ME =		ptolemy/vergil/ptolemy/fsm

DIRS =		modal

# Root of the Java directory
ROOT =		../../../../

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	fsm
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	modal/modal.jar
PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	Arc.java \
	FSMGraphController.java \
	FSMGraphModel.java \
	FSMGraphFrame.java \
	FSMGraphTableau.java \
	FSMStateController.java \
	FSMTransitionController.java \
	FSMViewerController.java \
	InterfaceAutomatonGraphController.java \
	InterfaceAutomatonGraphFrame.java \
	InterfaceAutomatonGraphTableau.java

# FIXME: Needs to be converted to a tableau?
#	PtolemyFSMNotation.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	lib \
	test \
	$(PTCLASSJAR) \
	'Arc$$1.class' \
	'Arc$$2.class' \
	'FSMGraphFrame$$DebugMenuListener.class' \
	'FSMGraphModel$$7.class' \
	'FSMGraphModel$$8.class' \
	'FSMGraphController$$1.class' \
	'FSMGraphController$$NewStateAction.class' \
	'FSMGraphController$$StateCreator.class' \
	'FSMGraphFrame$$LookInsideAction.class' \
	'FSMGraphFrame$$PortContextMenuFactory.class' \
	'FSMGraphFrame$$PortContextMenuFactory$$PortDescriptionFactory.class' \
	'FSMGraphFrame$$StateContextMenuFactory.class' \
	'FSMGraphFrame$$TransitionContextMenuFactory.class' \
	'FSMGraphController$$LinkCreator.class' \
	'FSMGraphModel$$1.class' \
	'FSMGraphModel$$2.class' \
	'FSMGraphModel$$3.class' \
	'FSMGraphModel$$4.class' \
	'FSMGraphModel$$5.class' \
	'FSMGraphModel$$6.class' \
	'FSMGraphModel$$ArcModel.class' \
	'FSMGraphModel$$RemoveableNodeModel.class' \
	'FSMGraphModel$$PortModel.class' \
	'FSMGraphModel$$StateModel.class' \
	'FSMGraphModel$$ToplevelModel.class' \
	'FSMGraphTableau$$Factory.class' \
	'FSMPortController$$NameWrapper.class' \
	'FSMPortController$$PortRenderer.class' \
	'FSMStateController$$StateRenderer.class' \
	'FSMStateController$$EntityRenderer.class' \
	'FSMStateController$$LookInsideAction.class' \
	'FSMTransitionController$$1.class' \
	'FSMTransitionController$$LinkRenderer.class' \
	'FSMTransitionController$$LinkTarget.class' \
	'FSMTransitionController$$LinkDropper.class' \
	'FSMViewerController$$SchematicContextMenuFactory.class' \
	'InterfaceAutomatonGraphController$$ComposeWithAction.class' \
	'InterfaceAutomatonGraphTableau$$Factory.class'


JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

vergil:
	$(JAVA) -classpath "$(CLASSPATH)" ptolemy.vergil.VergilApplication

vergildebug:
	$(JAVA) -classpath "$(CLASSPATH)" -Djava.compiler=NONE ptolemy.vergil.VergilApplication

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
