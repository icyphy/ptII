# Makefile for Java Ptolemy polymorphic actor classes
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
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

# This directory contains polymorphic actors.

ME =		ptolemy/actor/gui

DIRS = 		style test

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	gui
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
# Except: NOTE: HTMLEffigy has to come before Configuration
# because its inner class, HTMLEffigy$Factory, is referenced.
JSRCS = \
	CompositeActorApplication.java \
	Configuration.java \
	ConfigurationEffigy.java \
	Configurer.java \
	DebugListenerTableau.java \
	DocumentationViewer.java \
	DocumentationViewerTableau.java \
	EditorPaneFactory.java \
	EditParametersDialog.java \
	Effigy.java \
	EffigyFactory.java \
	HTMLEffigy.java \
	HTMLEffigyFactory.java \
	HTMLViewer.java \
	HTMLViewerTableau.java \
	ModelDirectory.java \
	ModelFrame.java \
	ModelPane.java \
	MoMLApplet.java \
	MoMLApplication.java \
	Placeable.java \
	PortConfigurer.java \
	PortConfigurerDialog.java \
	PtExecuteApplication.java \
	PtolemyApplet.java \
	PtolemyApplication.java \
	PtolemyEffigy.java \
	PtolemyQuery.java \
	PtolemyTableauFactory.java \
	PtolemyFrame.java \
	RunTableau.java \
	TableauFrame.java \
	TextEditor.java \
	TextEditorTableau.java \
	TextEffigy.java \
	Tableau.java \
	TableauFactory.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'CompositeActorApplication$$StateReporter.class' \
	'Configuration$$1.class' \
	'Configuration$$2.class' \
	'Configurer$$1.class' \
	'DebugListenerTableau$$1.class' \
	'DebugListenerTableau$$DebugListenerFrame.class' \
	'HTMLEffigy$$Factory.class' \
	'HTMLViewerTableau$$Factory.class' \
	'ModelFrame$$FileMenuListener.class' \
	'ModelFrame$$HelpMenuListener.class' \
	'ModelPane$$1.class' \
	'ModelPane$$2.class' \
	'ModelPane$$3.class' \
	'ModelPane$$4.class' \
	'PtolemyApplet$$GoButtonListener.class' \
	'PtolemyApplet$$StopButtonListener.class' \
	'PtolemyApplication$$1.class' \
	'PtolemyEffigy$$Factory.class' \
	'PtolemyQuery$$1.class' \
	'PtolemyTop$$ViewMenuListener.class' \
	'RunTableau$$Factory.class' \
	'RunTableau$$RunFrame$$DebugMenuListener.class' \
	'RunTableau$$RunFrame.class' \
	'Tableau$$1.class' \
	'TableauFrame$$1.class' \
	'TableauFrame$$ViewMenuListener.class' \
	'TextEditorTableau$$Factory.class' \
	'TextEffigy$$Factory.class'

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass $(PTCLASSJAR)

demo: all
	$(ROOT)/bin/ptolemy /users/cxh/ptII/ptolemy/moml/demo/modulation.xml 
	$(ROOT)/bin/vergil /users/cxh/ptII/ptolemy/moml/demo/modulation.xml 
depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
