# Makefile for canvas tutorial
#
# @Version: $Id: makefile 57040 2010-01-27 20:52:32Z cxh $
# @Authors: Christopher Hylands (makefile only)
#
# @Copyright (c) 2004-2009 The Regents of the University of California.
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

ME =		diva/canvas/tutorial

DIRS =

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	tutorial
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS = \
	ArcTutorial.java \
	CompositeFigureTutorial.java \
	ConnectorTutorial.java \
	DragTutorial.java \
	FigureTutorial.java \
	IconTutorial.java \
	LabelTutorial.java \
	ManipulatorTutorial.java \
	SimpleTutorial.java \
	TerminalTutorial.java \
	TransformedFigureTutorial.java \
	VectorFigureTutorial.java

EXTRA_SRCS =	$(JSRCS) \
	demo.gif \
	package.html \

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	$(PTCLASSJAR) \
	$(PTCLASSALLJAR) \
	'ArcTutorial$$1.class' \
	'ArcTutorial$$2.class' \
	'ArcTutorial$$3.class' \
	'ArcTutorial$$SelfPTarget.class' \
	'CompositeFigureTutorial$$1.class' \
	'ConnectorTutorial$$1.class' \
	'ConnectorTutorial$$2.class' \
	'ConnectorTutorial$$3.class' \
	'ConnectorTutorial$$SitedRectangle.class' \
	'ConnectorTutorial$$SRTarget.class' \
	'DragTutorial$$1.class' \
	'FigureTutorial$$1.class' \
	'FigureTutorial$$CustomRectangle.class' \
	'IconTutorial$$1.class' \
	'LabelTutorial$$1.class' \
	'ManipulatorTutorial$$1.class' \
	'SimpleTutorial$$1.class' \
	'TerminalTutorial$$1.class' \
	'TransformedFigureTutorial$$1.class' \
	'TransformedFigureTutorial$$CloudFigure.class' \
	'VectorFigureTutorial$$1.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass doc-files
	@echo "Run 'make demos' to run all the tutorials"

install: all jars

doc-files:
	if [ ! -d doc-files ]; then \
	    echo "Updating doc-files directory for javadoc"; \
	    mkdir doc-files; \
	    cp ../../doc/packages/canvas/tutorial/images/*.gif doc-files; \
	fi

DEMOS = $(JSRCS:%.java=%)
demos: $(DEMOS)

# Rules for running the examples. These are just there to save messing
# about with classpath
ArcTutorial: ArcTutorial.class
	$(JAVA) -classpath $(CLASSPATH) diva.canvas.tutorial.ArcTutorial

CompositeFigureTutorial: CompositeFigureTutorial.class
	$(JAVA) -classpath $(CLASSPATH) diva.canvas.tutorial.CompositeFigureTutorial

ConnectorTutorial: ConnectorTutorial.class
	$(JAVA) -classpath $(CLASSPATH) diva.canvas.tutorial.ConnectorTutorial

DragTutorial: DragTutorial.class
	$(JAVA) -classpath $(CLASSPATH) diva.canvas.tutorial.DragTutorial

IconTutorial: IconTutorial.class
	$(JAVA) -classpath $(CLASSPATH) diva.canvas.tutorial.IconTutorial

LabelTutorial: LabelTutorial.class
	$(JAVA) -classpath $(CLASSPATH) diva.canvas.tutorial.LabelTutorial

FigureTutorial: FigureTutorial.class
	$(JAVA) -classpath $(CLASSPATH) diva.canvas.tutorial.FigureTutorial

ManipulatorTutorial: ManipulatorTutorial.class
	$(JAVA) -classpath $(CLASSPATH) diva.canvas.tutorial.ManipulatorTutorial

SimpleTutorial: SimpleTutorial.class
	$(JAVA) -classpath $(CLASSPATH) diva.canvas.tutorial.SimpleTutorial

SwingTutorial: SwingTutorial.class
	$(JAVA) -classpath $(CLASSPATH) diva.canvas.tutorial.SwingTutorial

TerminalTutorial: TerminalTutorial.class
	$(JAVA) -classpath $(CLASSPATH) diva.canvas.tutorial.TerminalTutorial

TransformedFigureTutorial: TransformedFigureTutorial.class
	$(JAVA) -classpath $(CLASSPATH) \
		diva.canvas.tutorial.TransformedFigureTutorial

VectorFigureTutorial: VectorFigureTutorial.class
	$(JAVA) -classpath $(CLASSPATH) \
		diva.canvas.tutorial.VectorFigureTutorial

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
