# Makefile for Java ptplot program
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997- The Regents of the University of California.
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

# Root of the Java directory
ROOT =		../..

# Get configuration info
CONFIG =	$(ROOT)/mk/tycho.mk
include $(CONFIG)

JPACKAGE = 	ptplot
JPACKAGE_DIR = 	ptplot
JVERSION =	1.2
JDIST =		$(JPACKAGE)$(JVERSION)
JZIP =		$(JPACKAGE_DIR)/$(JPACKAGE).zip
JARFILE = 	$(JPACKAGE).jar
# Classes that are included in the jar file as Java Beand.
JBEANCLASSES =	ptplot/Plot.class ptplot/PlotLive.class

DIRS = 	demo doc #test

CLASSPATH =	..
# Order matters here.

# Sources that compile under JDK1.0.2 and later
JSRCS1_0 = \
	CmdLineArgException.java \
	PlotBox.java \
	Plot.java \
	PlotApplet.java \
	PlotDataException.java \
	PlotLive.java \
	PlotPoint.java

# Sources that require JDK1.1 and later.
JSRCS1_1 = \
	Message.java \
	Pxgraph.java

JSRCS = $(JSRCS1_0) $(JSRCS1_1)

EXTRA_SRCS = $(JSRCS) pxgraph pxgraph.bat testpxgraph index.html

MISC_FILES = demo doc #test 

JCLASS1_0 = $(JSRCS1_0:%.java=%.class)
JCLASS1_1 = $(JSRCS1_1:%.java=%.class)

JCLASS = $(JCLASS1_0) $(JCLASS1_1)

all: sources jclass suball $(JZIP) $(JARFILE)
	chmod a+x pxgraph testpxgraph
install: sources jclass javadocs subinstall $(JARFILE) $(JZIP) 
	chmod a+x pxgraph testpxgraph
docs: javadocs

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/tycommon.mk
