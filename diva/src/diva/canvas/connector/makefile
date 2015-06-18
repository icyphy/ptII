# Makefile for canvas classes and packages-Ptolemy II specific version of Diva
#
# @Version: $Id: makefile 53042 2009-04-10 20:31:21Z cxh $
# @Authors: Christopher Hylands (makefile only)
#
# @Copyright (c) 2003-2009 The Regents of the University of California.
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

ME =		diva/canvas/connector

DIRS =

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	connector
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS = \
	AbstractConnector.java \
	AbstractConnectorTarget.java \
	ArcConnector.java \
	ArcInteractor.java \
	ArcManipulator.java \
	ArcMidpointSite.java \
	Arrowhead.java \
	AutonomousSite.java \
	BasicManhattanRouter.java \
	Blob.java \
	BoundsSite.java \
	CenterSite.java \
	CenterTarget.java \
	Connector.java \
	ConnectorAdapter.java \
	ConnectorEnd.java \
	ConnectorEvent.java \
	ConnectorInteractor.java \
	ConnectorListener.java \
	ConnectorManipulator.java \
	ConnectorTarget.java \
	FixedNormalSite.java \
	IncrManhattanRouter.java \
	ManhattanConnector.java \
	ManhattanRouter.java \
	NullSite.java \
	ParametricSite.java \
	PerimeterSite.java \
	PerimeterTarget.java \
	Router.java \
	StraightConnector.java \
	StraightTerminal.java \
	Terminal.java \
	TerminalFigure.java

EXTRA_SRCS =	$(JSRCS) \
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
	'ConnectorInteractor$$1.class' \
	'ConnectorManipulator$$TargetHighlighter.class' \
	'PerimeterTarget$$1.class' \
	'StraightTerminal$$1.class' \
	'StraightTerminal$$ConnectSite.class'
 
JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
