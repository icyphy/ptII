# Makefile for Java ptplot program
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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

ME =		ptolemy/plot

DIRS = 		$(SRCDIRS) doc test
SRCDIRS = 	demo

# Root of the Ptolemy II directory
ROOT =		../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	plot
# If you change PTVERSION, modify the version numbers in:
#    Pxgraph.java, PlotApplication.java, doc/index.htm, doc/changes.htm
PTVERSION =	2.2
PTDIST =	ptplot$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

# Jar file consisting of classes used by an applet.  Basically,
# everything but PlotApplication and Pxgraph.
PTAUXJAR = plotapplet.jar

# Java Sources, keep these alphabetical,
JSRCS = \
	CmdLineArgException.java \
	EditablePlot.java \
	EditablePlotApplication.java \
	EPSGraphics.java \
	Histogram.java \
	HistogramApplet.java \
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

# Used to build distributions with InstallShield's isjava tool
ISJAVA_SRC = ptplot.isj

# Files that are not compiled, such as .tcl and .itcl files.
EXTRA_SRCS = $(JSRCS) \
	$(ISJAVA_SRC) \
	PtolemyGraphics1_1.java.in \
	PtolemyGraphics1_2.java.in \
	copyright.txt \
	index.htm \
	ptplot.in \
	pxgraph.bat

# Non-source files such as README files and subdirectories
MISC_FILES = test #bean

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(DIRS) \
	adm \
	manifest.tmp \
	ptplot \
	pxgraph \
	$(PTCLASSJAR) \
	$(PTCLASSZIP)\
	ptolemy.plot$(JVERSION).tar.gz \
	ptolemy.plot$(JVERSION).zip \
	$(PTAUXJAR) \
	PtolemyGraphics.java \
	'Message$$1.class' \
	'Message$$2.class' \
	'PlotBox$$CommandListener.class' \
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

all: sources ptplot jclass $(PTCLASSZIP) $(PTCLASSJAR) $(PTAUXJAR)
	-chmod a+x ptplot
	rm -f pxgraph
	ln -s ptplot pxgraph
install: all jars bin_install_dir
	-chmod a+x ptplot
	rm -f pxgraph
	ln -s ptplot pxgraph
	$(INSTALL_PROGRAM) ptplot $(BIN_INSTALL_DIR)
	$(INSTALL_PROGRAM) pxgraph $(BIN_INSTALL_DIR)

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
# zip might not get the permissions of configure right, so we run /bin/sh
../../config.status: ../../configure
	(cd ../..; /bin/sh ./configure)

$(PTAUXJAR): $(JSRCS) $(JCLASS)
	rm -f $@
	(cd $(ROOT); $(JAR) cf $(ME)/$@ \
		$(ME)/CmdLineArgException.class \
		$(ME)/EPSGraphics.class \
		$(ME)/Histogram.class \
		$(ME)/HistogramApplet.class \
		$(ME)/Message.class \
		$(ME)/Plot.class \
		$(ME)/PlotApplet.class \
		$(ME)/PlotBox.class \
		$(ME)/PlotDataException.class \
		$(ME)/PlotLive.class \
		$(ME)/PlotLiveApplet.class \
		$(ME)/PlotPoint.class \
		$(ME)/PtolemyGraphics.class \
		'$(ME)/Plot$$Format.class' \
		'$(ME)/Plot$$Format.class' \
		'$(ME)/PlotBox$$CommandListener.class' \
		'$(ME)/PlotBox$$DragListener.class' \
		'$(ME)/PlotBox$$FillButtonListener.class' \
		'$(ME)/PlotBox$$ZoomListener.class' \
		'$(ME)/PlotLive$$StartButtonListener.class' \
		'$(ME)/PlotLive$$StopButtonListener.class' \
            )



depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

# Include rules necessary to build a distribution
include $(ROOT)/mk/ptdist.mk

# Fix the temporary directory used when building a distribution
fixtmpdist:
	@echo "Fixing $(PTTMPDIR)/$(PTDIST)"
	echo "makefiles all sources install docs depend checkjunk sccsinfo sccsclean test extraclean clean:" > $(PTTMPDIR)/$(PTDIST)/makefile
	echo '	cd ptolemy/plot; make $$@' >>$(PTTMPDIR)/$(PTDIST)/makefile
	echo "realclean itcldocs tests idocs weblint dists:" >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo "	echo \"Nothing need be done for $$@ in ptolemy/plot\"" >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo "$(PTDIST) - A Java plotting package" > \
		$(PTTMPDIR)/$(PTDIST)/README
	echo "Ptplot is a set of two dimensional signal plotters >> \
		$(PTTMPDIR)/$(PTDIST)/README
	echo "implemented in Java." >>  $(PTTMPDIR)/$(PTDIST)/README
	echo "Ptplot is embeddable in applets and applications." >> \
		$(PTTMPDIR)/$(PTDIST)/README
	echo "For more information, see ptolemy/plot/index.htm" >> \
		$(PTTMPDIR)/$(PTDIST)/README
	@echo "Remove LogicAnalyzer from makefile"
	grep -v LogicAnalyzer \
		$(PTTMPDIR)/$(PTDIST)/ptolemy/plot/makefile > /tmp/ptplotfix
	mv /tmp/ptplotfix $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/makefile
	rm -f $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/LogicAnalyzer*
	@echo "JShield needs the ico file to be in the top level directory"
	cp $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/img/ptplot.ico \
		$(PTTMPDIR)/$(PTDIST)
	@echo "Setting up doc directory"
	mkdir -p $(PTTMPDIR)/$(PTDIST)/doc/codeDoc
	cd $(PTTMPDIR)/$(PTDIST); \
	CLASSPATH=. $(JAVADOC) $(JDOCFLAGS) -d doc/codeDoc ptolemy.plot
	@echo "Fixing up doc/download directory"
	rm -rf $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download
	mkdir $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download
	echo "<html>" > $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.htm
	echo "<head>" >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.htm
	echo "<title>Download Ptplot</title>" >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.htm
	echo '<body bgcolor="#faf0e6">'  >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.htm
	echo "<H1>Download Ptplot<H1>" >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.htm
	echo "The Ptplot distribution itself does not include all the available download formats." >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.htm
	echo 'See <a href="http://ptolemy.eecs.berkeley.edu/ptplot/download"><code>http://ptolemy.eecs.berkeley.edu/ptplot/download</code></a>' >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.htm
	echo "<hr>" >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.htm
	echo "</body>" >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.htm
	echo "</html>" >> $(PTTMPDIR)/$(PTDIST)/ptolemy/plot/doc/download/index.htm
