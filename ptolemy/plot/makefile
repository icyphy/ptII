# Makefile for Java ptplot program
#
# @Authors: Christopher Hylands
#
# @Version: @(#)makefile	1.77 08/31/98
#
# @Copyright (c) 1997-1998 The Regents of the University of California.
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

# Root of the Ptolemy II directory
ROOT =		../..

# Get configuration info
CONFIG =	$(ROOT)/mk/tycho.mk
include $(CONFIG)

CLASSPATH =	../..

# Note that this package has the the name 'ptolemy.plot', not
# tycho.java.ptolemy.plot.  The Itcl and java package have different root
# directories.

TYPACKAGE = 	ptolemy.plot
TYPACKAGE_ROOTDIR = $(CLASSPATH)
TYPACKAGE_DIR = ptolemy/plot
# If you change JVERSION, modify the version numbers in:
#    Pxgraph.java, index.html, doc/internals/changes.html
TYVERSION =	2.0
TYDIST =	$(TYPACKAGE)$(TYVERSION)
TYZIP =		$(TYPACKAGE_DIR)/$(TYPACKAGE).zip
JARFILE = 	$(TYPACKAGE).jar
# Classes that are included in the jar file as Java Bean.
JBEANCLASSES =	ptolemy/plot/Plot.class ptolemy/plot/PlotLive.class

SRCDIRS = 	demo
DIRS = 		$(SRCDIRS) doc test

#############################################################################
########### Pxgraph.java will be removed eventually, after the pxgraph
########### script is fixed.
#############################################################################

# Java Sources
JSRCS = \
	CmdLineArgException.java \
	Plot.java \
	PlotApplet.java \
	PlotApplication.java \
	PlotBox.java \
	PlotDataException.java \
	PlotFrame.java \
	PlotLive.java \
	PlotLiveApplet.java \
	PlotPoint.java \
	Pxgraph.java \
	Message.java

# Files that are not compiled, such as .tcl and .itcl files.
EXTRA_SRCS = $(JSRCS) pxgraph pxgraph.bat ptplot2mif.tcl index.html

# Non-source files such as README files and subdirectories
MISC_FILES = test #bean

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(DIRS) \
	adm \
	manifest.tmp \
	ptolemy.plot.jar \
	ptolemy.plot.zip \
	ptolemy.plot$(JVERSION).tar.gz \
	ptolemy.plot$(JVERSION).zip \
	ptolemy.plotapplet.jar \
	'Message$$1.class' \
	'Message$$2.class' \
	'PlotBox$$DragListener.class' \
	'PlotBox$$FillButtonListener.class' \
	'PlotBox$$ZoomListener.class' \
	'PlotLive$$StartButtonListener.class' \
	'PlotLive$$StopButtonListener.class' \
	'Pxgraph$$1.class' \
	'Pxgraph$$2.class' \
	'Pxgraph$$AboutButtonListener.class' \
	'Pxgraph$$ExitButtonListener.class' \
	'Pxgraph$$HTMLButtonListener.class' \
	'Pxgraph$$PrintButtonListener.class'

# Files to be removed by 'make realclean'
REALCLEAN_STUFF =

JCLASS = $(JSRCS:%.java=%.class)

all: sources jclass $(TYZIP) $(JARFILE) ptolemy.plotapplet.jar
	-chmod a+x pxgraph
install: sources jclass javadocs subinstall $(JARFILE) $(TYZIP) ptolemy.plotapplet.jar
	-chmod a+x pxgraph

# Jar file consisting of classes used by an applet.  Basically,
# everything but Message and Pxgraph.
ptolemy.plotapplet.jar: $(JSRCS) $(JCLASS)
	(cd $(CLASSPATH); $(JAR) cf $(TYPACKAGE_DIR)/ptolemy.plotapplet.jar \
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
