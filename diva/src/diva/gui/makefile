# Makefile for gui classes and packages-Ptolemy II specific version of Diva
#
# @Version: $Id: makefile 62302 2011-10-25 17:06:55Z cxh $
# @Authors: Christopher Hylands (makefile only)
#
# @Copyright (c) 2003-2005 The Regents of the University of California.
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

ME =		diva/gui

DIRS = 		toolbox tutorial

# Root of the Ptolemy II directory
ROOT =		../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	gui
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	toolbox/toolbox.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	AbstractDocument.java \
	AbstractStoragePolicy.java \
	AbstractView.java \
	AppContext.java \
	AppContextDelegate.java \
	AppletContext.java \
	Application.java \
	ApplicationContext.java \
	ApplicationExceptionHandler.java \
	ApplicationResources.java \
	BasicFrame.java \
	BasicPage.java \
	BasicStoragePolicy.java \
	ContextView.java \
	DefaultActions.java \
	DefaultStoragePolicy.java \
	DesktopContext.java \
	Document.java \
	DocumentFactory.java \
	ExtensionFileFilter.java \
	GUIUtilities.java \
	MDIContext.java \
	MultipageDocument.java \
	MultipageModel.java \
	MultipageParser.java \
	MultipageWriter.java \
	Page.java \
	StoragePolicy.java \
	View.java \
	ViewAdapter.java \
	ViewEvent.java \
	ViewListener.java

OTHER_FILES_TO_BE_JARED = \
	ExtensionFileFilter-license.htm

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED) \
	package.html

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	$(PTCLASSJAR) \
	$(PTCLASSALLJAR) \
	'AppletContext$$1.class' \
	'ApplicationContext$$1.class' \
	'BasicFrame$$1.class' \
	'BasicFrame$$2.class' \
	'DefaultActions$$10.class' \
	'DefaultActions$$1.class' \
	'DefaultActions$$2.class' \
	'DefaultActions$$3.class' \
	'DefaultActions$$4.class' \
	'DefaultActions$$5.class' \
	'DefaultActions$$6.class' \
	'DefaultActions$$7.class' \
	'DefaultActions$$8.class' \
	'DefaultActions$$9.class' \
	'DesktopContext$$1.class' \
	'DesktopContext$$DesktopInternalFrame.class' \
	'DesktopContext$$DesktopPseudoFrame.class' \
	'DesktopContext$$FrameManager.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
