# Makefile for the Vergil graph support package
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

ME =		ptolemy/vergil/ptolemy/kernel

DIRS =

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	kernel
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	AnimationRenderer.java \
	AttributeController.java \
	EditorGraphController.java \
	EntityController.java \
	EntityPortController.java \
	IconController.java \
	KernelGraphFrame.java \
	KernelGraphTableau.java \
	Link.java \
	LinkController.java \
	NodeControllerFactory.java \
	ParameterizedNodeController.java \
	PortController.java \
	PortDialogFactory.java \
	PtolemyGraphModel.java \
	PtolemyNodeController.java \
	PtolemyGraphController.java \
	RelationController.java \
	RenameDialogFactory.java \
	ViewerGraphController.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	'AbstractPtolemyGraphModel$$GraphChangeListener.class' \
	'AttributeController$$AttributeRenderer.class' \
	'EditorDropTarget$$1.class' \
	'EditorDropTarget$$DTListener.class' \
	'EditorGraphController$$1.class' \
	'EditorGraphController$$2.class' \
	'EditorGraphController$$ConnectedVertexCreator.class' \
	'EditorGraphController$$LinkCreator.class' \
	'EditorGraphController$$NewPortAction.class' \
	'EditorGraphController$$NewRelationAction.class' \
	'EditorGraphController$$PortCreator.class' \
	'EditorGraphController$$RelationCreator.class' \
	'EntityController$$1.class' \
	'EntityController$$EntityContextMenuFactory.class' \
	'EntityController$$EntityLayout.class' \
	'EntityController$$EntityRenderer.class' \
	'EntityPortController$$EntityPortRenderer.class' \
	'EntityPortController$$PortContextMenuFactory$$PortDescriptionFactory.class' \
	'EntityPortController$$PortContextMenuFactory.class' \
	'KernelGraphFrame$$AttributeContextMenuFactory.class' \
	'KernelGraphFrame$$EntityContextMenuFactory.class' \
	'KernelGraphFrame$$LookInsideAction.class' \
	'KernelGraphFrame$$PortContextMenuFactory$$PortDescriptionFactory.class' \
	'KernelGraphFrame$$PortContextMenuFactory.class' \
	'KernelGraphFrame$$RelationContextMenuFactory.class' \
	'KernelGraphTableau$$Factory.class' \
	'Link$$1.class' \
	'Link$$2.class' \
	'Link$$3.class' \
	'Link$$4.class' \
	'LinkController$$LinkContextMenuFactory.class' \
	'LinkController$$LinkDropper.class' \
	'LinkController$$LinkRenderer.class' \
	'LinkController$$LinkTarget.class' \
	'PortController$$NameWrapper.class' \
	'PortController$$PortRenderer.class' \
	'PortDialogFactory$$1.class' \
	'PtolemyGraphModel$$1.class' \
	'PtolemyGraphModel$$2.class' \
	'PtolemyGraphModel$$3.class' \
	'PtolemyGraphModel$$4.class' \
	'PtolemyGraphModel$$5.class' \
	'PtolemyGraphModel$$6.class' \
	'PtolemyGraphModel$$7.class' \
	'PtolemyGraphModel$$8.class' \
	'PtolemyGraphModel$$9.class' \
	'PtolemyGraphModel$$AttributeModel.class' \
	'PtolemyGraphModel$$ExternalPortModel.class' \
	'PtolemyGraphModel$$IconModel.class' \
	'PtolemyGraphModel$$LinkModel.class' \
	'PtolemyGraphModel$$LinkModel$$LinkChangeListener.class' \
	'PtolemyGraphModel$$PortModel.class' \
	'PtolemyGraphModel$$RemoveableNodeModel.class' \
	'PtolemyGraphModel$$ToplevelModel.class' \
	'PtolemyGraphModel$$VertexModel.class' \
	'RenameDialogFactory$$1.class' \
	'RelationController$$RelationContextMenuFactory.class' \
	'RelationController$$RelationRenderer.class' \
	'ViewerGraphController$$EditDirectorParameterStylesFactory.class' \
	'ViewerGraphController$$EditDirectorParametersFactory.class' \
	'ViewerGraphController$$SchematicContextMenuFactory.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
