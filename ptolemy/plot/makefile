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

# Java Sources, keep these alphabetical,
JSRCS = \
	CmdLineArgException.java \
	EPSGraphics.java \
	LogicAnalyzer.java \
	LogicAnalyzerFrame.java \
	Message.java \
	Plot.java \
	PlotApplet.java \
	PlotApplication.java \
	PlotBox.java \
	PlotDataException.java \
	PlotFrame.java \
	PlotLive.java \
	PlotLiveApplet.java \
	PlotPoint.java \
	PtolemyGraphics.java \
	Pxgraph.java

# Files that are not compiled, such as .tcl and .itcl files.
EXTRA_SRCS = $(JSRCS) \
	PtolemyGraphics1_1.java.in \
	PtolemyGraphics1_2.java.in \
	copyright.txt \
	index.html \
	ptplot \
	pxgraph.bat 

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
	PtolemyGraphics.java \
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

# FIXME: this is ugly, dangerous, and unportable.
#
# This is a workaround so that EPSGraphics can extend Graphics in 
# both JDK1.1 and JDK1.2
# See the class comment in PtolemyGraphics1_2 for the complete story

# There are two files:
# PtolemyGraphics1_1.java.in and PtolemyGraphics1_2.java.in
#
# These files have java.in suffixes so that the rule that runs javadoc
# for all of the Ptolemy II packages does not attempt to create documentation
# for these files.
# 
# PtolemyGraphics1_2.java.in extends Graphics, and has a method body
# for the new bogus method Sun introduced into the Graphics class in JDK 1.2
#
# PtolemyGraphics1_1.java.in extends Graphics, and has no methods
# 
# $PTII/conf/confTest.java is a small Java application that simply
# prints the value of the java.version property.
#
# When configure is run, $PTII/conf/confTest is compiled and run, and
# the value of the JVERSION variable in $PTII/mk/ptII.mk is set. 
#
# The ptolemy/plot makefile checks the JVERSION variable and copies the
# appropriate file to PtolemyGraphics.java.  
#
# I modified EPSGraphics.java so that it extends PtolemyGraphics instead
# of directly extending Graphics.
#
# We will have problems if other JDK 1.2 implementation return different
# values for JDK 1.2.  Also, this will break when JDK1.2 is upgraded to
# JDK1.2.1 etc.  I'll work on a fix to this.
#
#
# Don't use GNU make only directives here, since that will make the
# portability problem even worse, instead, use a sh case statement
# so that we can look for the "1.2" substring.  If we don't do this,
# then the version number from JDK1.2.1 etc. will not match properly.
#
PtolemyGraphics.java: PtolemyGraphics1_1.java.in PtolemyGraphics1_2.java.in \
				$(ROOT)/mk/ptII.mk
	@case "$(JVERSION)" in \
	1.2*) \
	    echo "configure determined that java.version='$(JVERSION)'"; \
	    echo " so we are running JDK1.2 or later."; \
	    echo "Copying PtolemyGraphics1_2.java.in to PtolemyGraphics.java"; \
	    cp PtolemyGraphics1_2.java.in PtolemyGraphics.java; \
	    ;; \
	*) \
	    echo "configure determined that java.version='$(JVERSION)'"; \
	    echo " so we are not running JDK1.2 or later."; \
	    echo "Copying PtolemyGraphics1_1.java.in to PtolemyGraphics.java"; \
	    cp PtolemyGraphics1_1.java.in PtolemyGraphics.java; \
	    ;; \
	esac


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

# Fix the temporary directory used when building a distribution
fixtmpdist:
	@echo "Fixing $(PTTMPDIR)/$(PTDIST)"
	echo "all:" > $(PTTMPDIR)/$(PTDIST)/makefile
	echo "	cd ptolemy/plot; $(MAKE) all" >> $(PTTMPDIR)/$(PTDIST)/makefile
	echo "sources:" > $(PTTMPDIR)/$(PTDIST)/makefile
	echo "	cd ptolemy/plot; $(MAKE) sources" >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo "install:" >> $(PTTMPDIR)/$(PTDIST)/makefile
	echo "	cd ptolemy/plot; $(MAKE) install" >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo "clean:" >> $(PTTMPDIR)/$(PTDIST)/makefile
	echo "	cd ptolemy/plot; $(MAKE) clean" >> $(PTTMPDIR)/$(PTDIST)/makefile
	echo "$(PTDIST) - A Java plotting package" > \
		$(PTTMPDIR)/$(PTDIST)/README
	echo "Ptplot is a set of two dimensional signal plotters >> \
		$(PTTMPDIR)/$(PTDIST)/README
	echo "implemented in Java." >>  $(PTTMPDIR)/$(PTDIST)/README
	echo "Ptplot is embeddable in applets and applications." >> \
		$(PTTMPDIR)/$(PTDIST)/README
	echo "For more information, see ptolemy/plot/index.html" >> \
		$(PTTMPDIR)/$(PTDIST)/README
	@echo "Remove LogicAnalyzer from makefile"
	grep -v LogicAnalyzer \
		$(PTTMPDIR)/$(PTDIST)/ptolemy/plot/makefile > /tmp/ptplotfix
	mv /tmp/ptplotfix $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/makefile
	rm -f $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/LogicAnalyzer*
	@echo "JShield needs the ico file to be in the top level directory"
	cp $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/img/ptplot.ico \
		$(PTTMPDIR)/$(PTDIST)
	@echo "Fixing up doc/download directory"
	rm -rf $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download
	mkdir $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download
	echo "<html>" > $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.html
	echo "<head>" >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.html
	echo "<title>Download Ptplot</title>" >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.html
	echo '<body bgcolor="#faf0e6">'  >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.html
	echo "<H1>Download Ptplot<H1>" >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.html
	echo "The Ptplot distribution itself does not include all the available download formats." >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.html
	echo 'See <a href="http://ptolemy.eecs.berkeley.edu/ptplot/download"><code>http://ptolemy.eecs.berkeley.edu/ptplot/download</code></a>' >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.html
	echo "<hr>" >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.html
	echo "</body>" >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.html
	echo "</html>" >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.html
