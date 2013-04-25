# Makefile for gui code for homer - a model gui builder
#
# @Version: $Id$
# @Author: Christopher Brooks (makefile only)
#
# @Copyright (c) 2011 The Regents of the University of California.
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
##########################################################################

ME =		gui

DIRS =

# Root of Ptolemy II directory
ROOT =		../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(PTHOMER_JARS)

# Used to build jar files
PTPACKAGE = 	gui
PTCLASSJAR =	$(PTPACKAGE).jar

# All the .class files necessary to run a Ptolemy II domain applet
#PTCLASSALLJARS = \

#PTCLASSALLJAR = $(PTPACKAGE).jar

OTHER_FILES_TO_BE_JARED = \
	devices.xml

EXTRA_SRCS = $(OTHER_FILES_TO_BE_JARED)

# Keep this list alphabetized.
JSRCS = \
	AttributeTreeModel.java \
	HomerMainFrame.java \
	HomerMenu.java \
	NamedObjectTree.java \
	OpenLayoutDialog.java \
	RemoteObjectList.java \
	SizeDialog.java \
	TabScenePanel.java \
	TabbedLayoutScene.java \
	WidgetPropertiesFrame.java \

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS)

OPTIONAL_FILES = \
	'HomerMenu$$1.class' \
	'HomerMenu$$10.class' \
	'HomerMenu$$11.class' \
	'HomerMenu$$2.class' \
	'HomerMenu$$3.class' \
	'HomerMenu$$4.class' \
	'HomerMenu$$5.class' \
	'HomerMenu$$6.class' \
	'HomerMenu$$7.class' \
	'HomerMenu$$8.class' \
	'HomerMenu$$9.class' \
	'NamedObjectTree$$1.class' \
	'NamedObjectTree$$2.class' \
	'NamedObjectTree$$3.class' \
	'RemoteObjectList$$1.class' \
	'RemoteObjectList$$2$$1.class' \
	'RemoteObjectList$$2.class' \
	'RemoteObjectList$$3.class' \
	'TabScenePanel$$1.class' \
	'TabScenePanel$$2.class' \
	'TabScenePanel$$3.class' \
	'TabScenePanel$$4.class' \
	'TabScenePanel$$5.class' \
	'TabScenePanel$$6.class' \
	'TabScenePanel$$7.class' \
	'TabScenePanel$$8.class' \
	'TabScenePanel$$NamedObjectPopupMenu$$1.class' \
	'TabScenePanel$$NamedObjectPopupMenu$$2.class' \
	'TabScenePanel$$NamedObjectPopupMenu.class' \
	'TabbedLayoutScene$$1.class' \
	'TabbedLayoutScene$$2.class' \
	'TabbedLayoutScene$$TabButton.class' \
	'TabbedLayoutScene$$TabSceneButton$$1.class' \
	'TabbedLayoutScene$$TabSceneButton$$2.class' \
	'TabbedLayoutScene$$TabSceneButton$$3.class' \
	'TabbedLayoutScene$$TabSceneButton$$4.class' \
	'TabbedLayoutScene$$TabSceneButton$$5.class' \
	'TabbedLayoutScene$$TabSceneButton.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass $(EXTRA_SRCS)
install: jclass jars

# Include rules for directories that contain only subdirectories.
include $(ROOT)/mk/ptcommon.mk
