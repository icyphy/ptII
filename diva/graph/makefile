# Makefile for graph classes and packages - Ptolemy II specific version of Diva
#
# @Version: $Id$
# @Authors: Christopher Hylands (makefile only)
#
# @Copyright (c) 2003 The Regents of the University of California.
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

ME =		diva/graph

DIRS = 		basic layout modular toolbox test tutorial

# Root of the Ptolemy II directory
ROOT =		../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	graph
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	basic/basic.jar \
	layout/layout.jar \
	modular/modular.jar \
	toolbox/toolbox.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	AbstractGraphController.java \
	AbstractGraphModel.java \
	BasicEdgeController.java \
	BasicNodeController.java \
	EdgeController.java \
	EdgeCreator.java \
	EdgeInteractor.java \
	EdgeRenderer.java \
	GraphController.java \
	GraphEvent.java \
	GraphException.java \
	GraphListener.java \
	GraphModel.java \
	GraphPane.java \
	GraphUtilities.java \
	GraphViewEvent.java \
	GraphViewListener.java \
	JGraph.java \
	MutableGraphModel.java \
	NodeController.java \
	NodeDragInteractor.java \
	NodeInteractor.java \
	NodeRenderer.java \
	SimpleGraphController.java


EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	$(PTCLASSJAR) \
	$(PTCLASSALLJAR)

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
