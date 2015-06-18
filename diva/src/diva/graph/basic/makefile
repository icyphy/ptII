# Makefile for graph/basic classes - Ptolemy II specific version of Diva
#
# @Version: $Id: makefile 43472 2006-08-21 23:16:56Z cxh $
# @Authors: Christopher Hylands (makefile only)
#
# @Copyright (c) 2003-2006 The Regents of the University of California.
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

ME =		diva/graph/basic

DIRS =

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	basic
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS = \
	ArcRenderer.java \
	BasicEdgeRenderer.java \
	BasicGraphController.java \
	BasicGraphDemo.java \
	BasicGraphDemoApplet.java \
	BasicGraphModel.java \
	BasicLayoutTarget.java \
	BasicNodeRenderer.java \
	BubbleGraphController.java \
	BubbleGraphDemo.java \
	BubbleGraphDemoApplet.java \
	BubblePane.java \
	BubbleRenderer.java

EXTRA_SRCS =	$(JSRCS) \
	graph.dtd graph.xml

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	$(PTCLASSJAR) \
	$(PTCLASSALLJAR) \
	'BasicGraphController$$1.class' \
	'BasicGraphController$$NodeCreator.class' \
	'BasicGraphModel$$1.class' \
	'BasicGraphModel$$2.class' \
	'BasicGraphModel$$BasicCompositeNode.class' \
	'BasicGraphModel$$BasicEdge.class' \
	'BasicGraphModel$$BasicNode.class' \
	'BasicGraphModel$$Intermediate.class' \
	'BasicGraphModel$$BasicNode$$1.class' \
	'BasicGraphModel$$BasicNode$$2.class' \
	'BasicLayoutTarget$$1.class' \
	'BasicLayoutTarget$$2.class' \
	'BasicLayoutTarget$$3.class' \
	'BasicLayoutTarget$$4.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
