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

CLASSPATH =	..

# Note that this package has the the name 'ptplot', not
# tycho.java.ptplot.  The Itcl and java package have different root
# directories.

TYPACKAGE = 	ptplot
TYPACKAGE_ROOTDIR = $(CLASSPATH)
TYPACKAGE_DIR = $(TYPACKAGE)
# If you change JVERSION, modify the version numbers in:
#    Pxgraph.java, index.html, doc/internals/changes.html
TYVERSION =	1.3
TYDIST =	$(TYPACKAGE)$(TYVERSION)
TYZIP =		$(TYPACKAGE_DIR)/$(TYPACKAGE).zip
JARFILE = 	$(TYPACKAGE).jar
# Classes that are included in the jar file as Java Beand.
JBEANCLASSES =	ptplot/Plot.class ptplot/PlotLive.class

SRCDIRS = 	demo
DIRS = 		$(SRCDIRS) doc #test

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

# Files that are not compiled, such as .tcl and .itcl files.
EXTRA_SRCS = $(JSRCS) pxgraph pxgraph.bat testpxgraph ptplot2mif.tcl index.html

# Non-source files such as README files and subdirectories
MISC_FILES = demo doc #bean #test \

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	adm \
	manifest.tmp \
	ptplot.jar \
	ptplot.zip \
	ptplot$(JVERSION).tar.gz \
	ptplot$(JVERSION).zip \
	ptplotapplet.jar

# Files to be removed by 'make realclean'
REALCLEAN_STUFF = 

JCLASS1_0 = $(JSRCS1_0:%.java=%.class)
JCLASS1_1 = $(JSRCS1_1:%.java=%.class)

JCLASS = $(JCLASS1_0) $(JCLASS1_1)

all: sources jclass suball $(TYZIP) $(JARFILE) ptplotapplet.jar
	chmod a+x pxgraph testpxgraph
install: sources jclass javadocs subinstall $(JARFILE) $(TYZIP) ptplotapplet.jar
	chmod a+x pxgraph testpxgraph

# Jar file consisting of classes used by an applet.  Basically,
# everything but Message and Pxgraph.
ptplotapplet.jar:
	(cd $(CLASSPATH); $(JAR) cfm $(TYPACKAGE_DIR)/ptplotapplet.jar \
		$(TYPACKAGE_DIR)/CmdLineArgException.class \
		$(TYPACKAGE_DIR)/Plot.class \
		$(TYPACKAGE_DIR)/PlotApplet.class \
		$(TYPACKAGE_DIR)/PlotBox.class \
		$(TYPACKAGE_DIR)/PlotDataException.class \
		$(TYPACKAGE_DIR)/PlotLive.class \
		$(TYPACKAGE_DIR)/PlotPoint.class)

docs: javadocs

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/tycommon.mk
