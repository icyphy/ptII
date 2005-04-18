# Makefile for the ptolemy/vergil/actor package
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2001-2005 The Regents of the University of California.
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


ME =		ptolemy/vergil/actor

DIRS =		lib img

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	actor
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	img/img.jar \
	lib/lib.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	ActorController.java \
	ActorEditorGraphController.java \
	ActorGraphFrame.java \
	ActorGraphModel.java \
	ActorGraphTableau.java \
	ActorInstanceController.java \
	ActorViewerGraphController.java \
	ClassDefinitionController.java \
	ExternalIOPortController.java \
	IOPortController.java \
	LinkController.java \
	TypeOpaqueCompositeActorTableauFactory.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	'ActorController$$1.class' \
	'ActorController$$DebugListenerAction.class' \
	'ActorController$$EntityLayout.class' \
	'ActorController$$ListenToActorAction.class' \
	'ActorController$$LookInsideAction.class' \
	'ActorController$$SaveInFileAction.class' \
	'ActorController$$SaveInLibraryAction.class' \
	'ActorEditorGraphController$$1.class' \
	'ActorEditorGraphController$$LinkCreator.class' \
	'ActorEditorGraphController$$ListenToActorFactory.class' \
	'ActorEditorGraphController$$NewRelationAction.class' \
	'ActorEditorGraphController$$RelationCreator.class' \
	'ActorGraphFrame$$CreateHierarchyAction.class' \
	'ActorGraphFrame$$DebugMenuListener.class' \
	'ActorGraphFrame$$ImportLibraryAction.class' \
	'ActorGraphFrame$$InstantiateEntityAction.class' \
	'ActorGraphFrame$$LayoutAction.class' \
	'ActorGraphFrame$$SaveInLibraryAction.class' \
	'ActorGraphFrame$$InstantiateAttributeAction.class' \
	'ActorGraphModel$$1.class' \
	'ActorGraphModel$$2.class' \
	'ActorGraphModel$$ExternalPortModel.class' \
	'ActorGraphModel$$IconModel.class' \
	'ActorGraphModel$$LinkModel.class' \
	'ActorGraphModel$$LinkModel$$LinkChangeListener.class' \
	'ActorGraphModel$$PortModel.class' \
	'ActorGraphModel$$VertexModel.class' \
	'ActorGraphTableau$$Factory.class' \
	'ActorInstanceController$$1.class' \
	'ActorInstanceController$$2.class' \
	'ActorInstanceController$$ConvertToClassAction.class' \
	'ClassDefinitionController$$1.class' \
	'ClassDefinitionController$$2.class' \
	'ClassDefinitionController$$ConvertToInstanceAction.class' \
	'ClassDefinitionController$$CreateInstanceAction.class' \
	'ClassDefinitionController$$CreateSubclassAction.class' \
	'ExternalIOPortController$$1.class' \
	'ExternalIOPortController$$NameWrapper.class' \
	'ExternalIOPortController$$PortRenderer.class' \
	'IOPortController$$1.class' \
	'IOPortController$$EntityPortRenderer.class' \
	'IOPortController$$ListenToPortAction.class' \
	'LinkController$$1.class' \
	'LinkController$$LinkDropper.class' \
	'LinkController$$LinkRenderer.class' \
	'LinkController$$LinkTarget.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
