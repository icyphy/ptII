# Makefile for the ptolemy/vergil/actor package
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2001-2012 The Regents of the University of California.
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
	ActorInteractionAddon.java \
	ActorViewerGraphController.java \
	ClassDefinitionController.java \
	DocApplicationSpecializer.java \
	DocBuilder.java \
	DocBuilderEffigy.java \
	DocBuilderGUI.java \
	DocBuilderTableau.java \
	DocEffigy.java \
	DocManager.java \
	DocTableau.java \
	DocViewer.java \
	ExternalIOPortController.java \
	FindPackages.java \
	IOPortController.java \
	KielerLayoutConnector.java \
	LinkController.java \
	LinkManhattanConnector.java \
	PortConnectSite.java \
	PortTerminal.java \
	TypeOpaqueCompositeActorTableauFactory.java

OTHER_FILES_TO_BE_JARED = \
	docViewerHelp.htm

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	'ActorController$$EntityLayout.class' \
	'ActorController$$ListenToActorAction.class' \
	'ActorController$$OpenInstanceAction.class' \
	'ActorController$$SaveInLibraryAction.class' \
	'ActorEditorGraphController$$1.class' \
	'ActorEditorGraphController$$2.class' \
	'ActorEditorGraphController$$LinkCreator.class' \
	'ActorEditorGraphController$$ListenToActorFactory.class' \
	'ActorEditorGraphController$$NewRelationAction.class' \
	'ActorEditorGraphController$$RelationCreator.class' \
	'ActorGraphFrame$$ActorGraphPane.class' \
	'ActorGraphFrame$$AdvancedLayoutDialogAction.class' \
	'ActorGraphFrame$$CreateHierarchyAction.class' \
	'ActorGraphFrame$$DebugMenuListener.class' \
	'ActorGraphFrame$$ExportDesignPatternAction.class' \
	'ActorGraphFrame$$ImportDesignPatternAction.class' \
	'ActorGraphFrame$$ImportLibraryAction.class' \
	'ActorGraphFrame$$InstantiateAttributeAction.class' \
	'ActorGraphFrame$$InstantiateEntityAction.class' \
	'ActorGraphFrame$$InstantiatePortAction.class' \
	'ActorGraphFrame$$LayoutAction.class' \
	'ActorGraphFrame$$SaveInLibraryAction.class' \
	'ActorGraphModel$$ExternalPortModel.class' \
	'ActorGraphModel$$IconModel.class' \
	'ActorGraphModel$$LinkModel$$1.class' \
	'ActorGraphModel$$LinkModel$$2.class' \
	'ActorGraphModel$$LinkModel$$LinkChangeListener.class' \
	'ActorGraphModel$$LinkModel.class' \
	'ActorGraphModel$$PortModel.class' \
	'ActorGraphModel$$VertexModel.class' \
	'ActorGraphTableau$$Factory.class' \
	'ActorInstanceController$$1.class' \
	'ActorInstanceController$$2.class' \
	'ActorInstanceController$$ConvertToClassAction.class' \
	'ActorViewerGraphController$$EntityLayout.class' \
	'ClassDefinitionController$$1.class' \
	'ClassDefinitionController$$2.class' \
	'ClassDefinitionController$$ConvertToInstanceAction.class' \
	'ClassDefinitionController$$CreateInstanceAction.class' \
	'ClassDefinitionController$$CreateSubclassAction.class' \
	'DocBuilderEffigy$$Factory.class' \
	'DocBuilderGUI$$1.class' \
	'DocBuilderGUI$$2.class' \
	'DocBuilderGUI$$3.class' \
	'DocBuilderGUI$$4.class' \
	'DocBuilderTableau$$Factory.class' \
	'DocEffigy$$Factory.class' \
	'DocTableau$$Factory.class' \
	'DocViewer$$1.class' \
	'DocViewer$$2.class' \
	'DocViewer$$3.class' \
	'DocViewer$$BuildMenuListener.class' \
	'ExternalIOPortController$$PortRenderer$$1.class' \
	'ExternalIOPortController$$PortRenderer$$2.class' \
	'ExternalIOPortController$$PortRenderer.class' \
	'FindPackages$$1.class' \
	'FindPackages$$ClassFileFilter.class' \
	'FindPackages$$DirectoryFileFilter.class' \
	'IOPortController$$EntityPortRenderer$$1.class' \
	'IOPortController$$EntityPortRenderer$$2.class' \
	'IOPortController$$EntityPortRenderer.class' \
	'LinkController$$LinkDropper.class' \
	'LinkController$$LinkRenderer.class' \
	'LinkController$$LinkTarget.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
