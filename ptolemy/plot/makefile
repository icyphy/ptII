# Makefile for Java ptplot program
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
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
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	ptolemy.plot
PTPACKAGE_ROOTDIR = $(CLASSPATH)
PTPACKAGE_DIR = ptolemy/plot
# If you change PTVERSION, modify the version numbers in:
#    Pxgraph.java, index.html, doc/internals/changes.html
PTVERSION =	2.1
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTZIP =		$(PTPACKAGE_DIR)/$(PTPACKAGE).zip
PTCLASSJAR = 	$(PTPACKAGE).jar
PTCLASSZIP = 	$(PTPACKAGE).zip

CLASSPATH =	../..

SRCDIRS = 	demo

DIRS = 		$(SRCDIRS) doc test

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
	LogicAnalyzer.java \
	LogicAnalyzerFrame.java \
	Message.java

# Files that are not compiled, such as .tcl and .itcl files.
EXTRA_SRCS = $(JSRCS) ptplot pxgraph.bat index.html copyright.txt

# Non-source files such as README files and subdirectories
MISC_FILES = test #bean

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
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
	'Pxgraph$$PrintButtonListener.class' \
	'LogicAnalyzerFrame$$FileMenuListener.class' \
	'LogicAnalyzerFrame$$PlotFilenameFilter.class' \
	'LogicAnalyzerFrame$$SpecialMenuListener.class' \
	'Plot$$Format.class' \
	'PlotApplication$$1.class' \
	'PlotFrame$$FileMenuListener.class' \
	'PlotFrame$$PlotFilenameFilter.class' \
	'PlotFrame$$SpecialMenuListener.class'

JCLASS = $(JSRCS:%.java=%.class)

all: sources jclass $(PTCLASSZIP) $(PTCLASSJAR) ptolemy.plotapplet.jar
	-chmod a+x ptplot
	rm -f pxgraph
	ln -s ptplot pxgraph
install: sources jclass javadocs subinstall $(PTCLASSJAR) ptolemy.plotapplet.jar
	-chmod a+x ptplot
	rm -f pxgraph
	ln -s ptplot pxgraph

ptplot: ptplot.in ../../config.status
	(cd ../..; ./config.status)
../../config.status: ../../configure
	(cd ../..; ./configure)

# Jar file consisting of classes used by an applet.  Basically,
# everything but Message and Pxgraph.
ptolemy.plotapplet.jar: $(JSRCS) $(JCLASS)
	rm -f ptolemy.plotapplet.jar
	(cd $(CLASSPATH); $(JAR) cf $(PTPACKAGE_DIR)/ptolemy.plotapplet.jar \
		$(PTPACKAGE_DIR)/CmdLineArgException.class \
		$(PTPACKAGE_DIR)/Plot.class \
		$(PTPACKAGE_DIR)/PlotApplet.class \
		$(PTPACKAGE_DIR)/PlotBox.class \
		$(PTPACKAGE_DIR)/PlotDataException.class \
		$(PTPACKAGE_DIR)/PlotLive.class \
		$(PTPACKAGE_DIR)/PlotLiveApplet.class \
		$(PTPACKAGE_DIR)/PlotPoint.class \
		'$(PTPACKAGE_DIR)/Plot$$Format.class' \
		'$(PTPACKAGE_DIR)/Plot$$Format.class' \
		'$(PTPACKAGE_DIR)/PlotBox$$DragListener.class' \
		'$(PTPACKAGE_DIR)/PlotBox$$FillButtonListener.class' \
		'$(PTPACKAGE_DIR)/PlotBox$$ZoomListener.class' \
		'$(PTPACKAGE_DIR)/PlotLive$$StartButtonListener.class' \
		'$(PTPACKAGE_DIR)/PlotLive$$StopButtonListener.class' \
		)

docs: javadocs

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

# Include rules necessary to build a distribution
include $(ROOT)/mk/ptdist.mk

# Fix the temporary directory
fixtmpdist:
	@echo "Fixing $(PTTMPDIR)/$(PTDIST)
	echo "all:" > $(PTTMPDIR)/$(PTDIST)/makefile
	echo "	cd ptolemy/plot; $(MAKE) all" >> $(PTTMPDIR)/$(PTDIST)/makefile
	echo "install:" >> $(PTTMPDIR)/$(PTDIST)/makefile
	echo "	cd ptolemy/plot; $(MAKE) install" >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo "clean:" >> $(PTTMPDIR)/$(PTDIST)/makefile
	echo "	cd ptolemy/plot; $(MAKE) clean" >> $(PTTMPDIR)/$(PTDIST)/makefile
	echo "$(PTDIST) - A Java plotting package" > \
		 $(PTTMPDIR)/$(PTDIST)/README
	echo "See ptolemy/plot/index.html" >> $(PTTMPDIR)/$(PTDIST)/README
	@echo "Remove LogicAnalyzer from makefile"
	grep -v LogicAnalyzer \
		$(PTTMPDIR)/$(PTDIST)/ptolemy/plot/makefile > /tmp/ptplotfix
	mv /tmp/ptplotfix $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/makefile
	rm -f $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/LogicAnalyzer*
