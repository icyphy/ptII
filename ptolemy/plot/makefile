# Makefile for Java ptplot program
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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

DIRS = 		demo doc test plotml compat

# Root of the Ptolemy II directory
ROOT =		../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	plot
# If you change PTVERSION, modify the version numbers in:
#    PlotBox.java,
#    PlotApplication.java, doc/main.htm, doc/changes.htm, doc/install.htm 
PTVERSION =	4.1
PTDIST =	ptplot$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		plotml/plotml.jar \
		compat/compat.jar

PTCLASSALLJAR = plot.jar

# Jar file consisting of classes used by an applet.
PTAUXJAR = 		plotapplet.jar

# Jar file consisting of classes used by pxgraph applets.
# The pxgraph classes appear in this jar file and in the plot.jar
PXGRAPHAPPLETJAR =	pxgraphapplet.jar

# Jar file consisting of classes used by an application, including the
# com/microstar/xml classes.  Note that this jar file does not include
# the pxgraph classes.
PLOTMLAPPLETJAR = plotmlapplet.jar

# Jar file consisting of classes used by an application, including the
# ptolemy/gui and com/microstar/xml classes.  Note that this
# jar file does not include the pxgraph classes.
PLOTAPPLICATIONJAR = plotapplication.jar

# Java Sources, keep these alphabetical,
JSRCS = \
	CmdLineArgException.java \
	EditablePlot.java \
	EditListener.java \
	EPSGraphics.java \
	Histogram.java \
	HistogramApplet.java \
	Plot.java \
	PlotApplet.java \
	PlotApplication.java \
	PlotBox.java \
	PlotDataException.java \
	PlotFrame.java \
	PlotLive.java \
	PlotLiveApplet.java \
	PlotPoint.java

# Used to build distributions with InstallShield's isjava tool
ISJAVA_SRC = ptplot.isj

# Files that are not compiled, such as .tcl and .itcl files.
EXTRA_SRCS = $(JSRCS) \
	$(ISJAVA_SRC) \
	copyright.txt \
	index.htm

# Non-source files such as README files and subdirectories
MISC_FILES = test #bean

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(DIRS) \
	adm \
	histogram \
	manifest.tmp \
	$(PTCLASSALLJAR) \
	'EditablePlot$$EditMouseListener.class' \
	'EditablePlot$$ModifyListener.class' \
	'EditablePlot$$UndoListener.class' \
	'Histogram$$1.class' \
	'Plot$$1.class' \
	'Plot$$2.class' \
	'PlotFrame$$1.class' \
	'PlotFrame$$2.class' \
	'PlotFrame$$FormatListener.class' \
	ptolemy.plot$(JVERSION).tar.gz \
	ptolemy.plot$(JVERSION).zip \
	$(PTAUXJAR) \
	$(PXGRAPHAPPLETJAR) \
	$(PLOTMLAPPLETJAR) \
	$(PLOTAPPLICATIONJAR) \
	'PlotBox$$CommandListener.class' \
	'PlotBox$$DragListener.class' \
	'PlotBox$$FillButtonListener.class' \
	'PlotBox$$ZoomListener.class' \
	'PlotLive$$StartButtonListener.class' \
	'PlotLive$$StopButtonListener.class' \
	'Plot$$Format.class' \
	'PlotApplication$$1.class' \
	'PlotFrame$$FileMenuListener.class' \
	'PlotFrame$$PlotFilenameFilter.class' \
	'PlotFrame$$SpecialMenuListener.class'

JCLASS = $(JSRCS:%.java=%.class)

all: sources jclass 

install: all jars \
		$(PXGRAPHAPPLETJAR) $(PLOTMLAPPLETJAR) $(PLOTAPPLICATIONJAR)
	(cd ../../bin; $(MAKE) install)

# Class files used in all applets and applications
BASECLASSFILES = \
		$(ME)/CmdLineArgException.class \
		$(ME)/EPSGraphics.class \
		$(ME)/Histogram.class \
		$(ME)/HistogramApplet.class \
		$(ME)/Plot.class \
		$(ME)/PlotBox.class \
		$(ME)/PlotDataException.class \
		$(ME)/PlotPoint.class \
		'$(ME)/Plot$$Format.class' \
		'$(ME)/PlotBox$$CommandListener.class' \
		'$(ME)/PlotBox$$DragListener.class' \
		'$(ME)/PlotBox$$FillButtonListener.class' \
		'$(ME)/PlotBox$$ZoomListener.class' \

# Class files used in simple applets
APPLETCLASSFILES = \
		$(ME)/PlotApplet.class \
		$(ME)/PlotLive.class \
		$(ME)/PlotLiveApplet.class \
		'$(ME)/PlotLive$$StartButtonListener.class' \
		'$(ME)/PlotLive$$StopButtonListener.class' \

# Class files that permit editing and histograms, used by
# complex applets and applications
COMPLEXCLASSFILES = \
		'$(ME)/EditablePlot$$EditMouseListener.class' \
		'$(ME)/EditablePlot$$ModifyListener.class' \
		'$(ME)/EditablePlot$$UndoListener.class' \
		$(ME)/EditablePlot.class \
		$(ME)/Histogram.class \
		$(ME)/HistogramApplet.class

# Class files used in applications
APPLICATIONCLASSFILES = \
		$(COMPLEXCLASSFILES) \
		'$(ME)/PlotApplication$$1.class' \
		$(ME)/PlotApplication.class \
		'$(ME)/PlotFrame$$1.class' \
		'$(ME)/PlotFrame$$2.class' \
		'$(ME)/PlotFrame$$FileMenuListener.class' \
		'$(ME)/PlotFrame$$FormatListener.class' \
		'$(ME)/PlotFrame$$SpecialMenuListener.class' \
		$(ME)/PlotFrame.class

# Jar file for simple applets
$(PTAUXJAR): $(JSRCS) $(JCLASS)
	rm -f $@
	(cd $(ROOT); $(JAR) cf $(ME)/$@ \
		$(BASECLASSFILES) \
		$(APPLETCLASSFILES) \
            )


# Class files used in pxgraph applets
$(PXGRAPHAPPLETJAR): $(JSRCS) $(JCLASS)
	rm -f $@
	(cd $(ROOT); $(JAR) cf $(ME)/$@ \
		$(BASECLASSFILES) \
		$(APPLETCLASSFILES) \
		$(ME)/compat/PxgraphApplet.class \
		$(ME)/compat/PxgraphApplication.class \
		$(ME)/compat/PxgraphParser.class \
            )

# Jar file for PlotML applets, including xml classes
$(PLOTMLAPPLETJAR): $(JSRCS) $(JCLASS)
	rm -f $@
	(cd $(ROOT); $(JAR) cf $(ME)/$@ \
		$(BASECLASSFILES) \
		$(APPLETCLASSFILES) \
		$(COMPLEXCLASSFILES) \
		$(ME)/plotml/*.class \
		com/microstar/xml/*.class \
            )

# Jar file for applications, including ptolemy/gui and xml classes
$(PLOTAPPLICATIONJAR): $(JSRCS) $(JCLASS)
	rm -f $@
	(cd $(ROOT); $(JAR) cf $(ME)/$@ \
		$(BASECLASSFILES) \
		$(APPLICATIONCLASSFILES) \
		$(COMPLEXCLASSFILES) \
		ptolemy/gui/*.class \
		$(ME)/plotml/*.class \
		com/microstar/xml/*.class \
            )

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

# Include rules necessary to build a distribution
include $(ROOT)/mk/ptdist.mk

PTTMPME= $(PTTMPDIR)/$(PTDIST)/$(ME)
# Fix the temporary directory used when building a distribution
fixtmpdist:
	@echo "Fixing $(PTTMPDIR)/$(PTDIST)"
	echo "all makefiles sources install docs depend checkjunk sccsinfo sccsclean test extraclean clean: mk/ptII.mk" > $(PTTMPDIR)/$(PTDIST)/makefile
	echo '	cd ptolemy/plot; make $$@' >>$(PTTMPDIR)/$(PTDIST)/makefile
	echo '# Generate ptII.mk by running configure' >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo 'mk/ptII.mk: configure mk/ptII.mk.in' >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo '	./configure' >> $(PTTMPDIR)/$(PTDIST)/makefile
	echo "realclean itcldocs tests idocs weblint dists:" >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo "	echo \"Nothing need be done for $$@ in ptolemy/plot)\"" >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo "$(PTDIST) - A Java plotting package" > \
		$(PTTMPDIR)/$(PTDIST)/README
	echo "Ptplot is a set of two dimensional signal plotters >> \
		$(PTTMPDIR)/$(PTDIST)/README
	echo "implemented in Java." >>  $(PTTMPDIR)/$(PTDIST)/README
	echo "Ptplot is embeddable in applets and applications." >> \
		$(PTTMPDIR)/$(PTDIST)/README
	echo "For more information, see ptolemy/plot/doc/index.htm" >> \
		$(PTTMPDIR)/$(PTDIST)/README
	echo "" >> $(PTTMPDIR)/$(PTDIST)/README
	echo "Installer notes " >> $(PTTMPDIR)/$(PTDIST)/README
	echo "If you are using the auto installer, then note that" >> \
		$(PTTMPDIR)/$(PTDIST)/README
	echo "under Windows, the Ptplot programs will be added to" >> \
		$(PTTMPDIR)/$(PTDIST)/README
	echo "your start menu under Ptolemy -> Ptplot.  Note further" >> \
		$(PTTMPDIR)/$(PTDIST)/README
	echo "that directory names with spaces do not work very well" >> \
		$(PTTMPDIR)/$(PTDIST)/README
	echo " because of limitations in the InstallShield tool." >> \
		$(PTTMPDIR)/$(PTDIST)/README
	mv /tmp/ptplotfix $(PTTMPME)/makefile
	@echo "JShield needs the ico file to be in the top level directory"
	cp $(PTTMPME)/doc/img/ptplot.ico \
		$(PTTMPDIR)/$(PTDIST)
	@echo "Copy over xml stuff"
	mkdir -p $(PTTMPDIR)/$(PTDIST)/com
	cp -r $(PTII)/com/microstar $(PTII)/com/makefile \
		$(PTTMPDIR)/$(PTDIST)/com
	@echo "Copy over gui stuff"
	cp -r $(PTII)/ptolemy/gui $(PTTMPDIR)/$(PTDIST)/ptolemy
	#
	# Setting up top level doc directory
	#
	mkdir -p $(PTTMPDIR)/$(PTDIST)/doc/img
	cp -r $(PTII)/doc/banner.htm \
		$(PTII)/doc/contentsheader.htm \
		$(PTII)/doc/default.css \
		$(PTII)/doc/footer.htm \
		$(PTII)/doc/mainheader.htm \
		$(PTII)/doc/toc.css \
		 $(PTTMPDIR)/$(PTDIST)/doc
	cp -r $(PTII)/doc/img/ptIIplanetIcon.gif \
		$(PTII)/doc/img/ptIIbanner2.gif \
		$(PTII)/doc/img/prev.gif \
		$(PTTMPDIR)/$(PTDIST)/doc/img
	# Create a file in the top level doc directory that points to
	# the real docs
	echo "<html>" > $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "<head>" >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "<title>Ptplot</title>" > $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "</head>" >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "<h1>Ptplot</h1>" >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo 'The Ptplot documentation is in <a href="../ptolemy/plot/doc/index.htm"><code>../ptolemy/plot/doc</code>' >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "</body>" >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "</htmp>" >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	#
	# Setting up doc/codeDoc directory
	#
	mkdir -p $(PTTMPDIR)/$(PTDIST)/doc/codeDoc
	cd $(PTTMPDIR)/$(PTDIST); \
	CLASSPATH=. $(JAVADOC) $(JDOCFLAGS) -d doc/codeDoc \
		com.microstar.xml \
		ptolemy.gui \
		ptolemy.plot \
		ptolemy.plot.compat \
		ptolemy.plot.plotml
	@echo "Fixing up doc/download directory"
	rm -rf $(PTTMPME)/doc/download
	mkdir $(PTTMPME)/doc/download
	echo "<html>" > $(PTTMPME)/doc/download/index.htm
	echo "<head>" >> $(PTTMPME)/doc/download/index.htm
	echo "<title>Download Ptplot</title>" >> $(PTTMPME)/doc/download/index.htm
	echo '<body bgcolor="#faf0e6">'  >> $(PTTMPME)/doc/download/index.htm
	echo "<H1>Download Ptplot</H1>" >> $(PTTMPME)/doc/download/index.htm
	echo "The Ptplot distribution itself does not include all the available download formats." >> $(PTTMPME)/doc/download/index.htm
	echo 'See <a href="http://ptolemy.eecs.berkeley.edu/java/ptplot" target="_top"><code>http://ptolemy.eecs.berkeley.edu/java/ptplot</code></a>' >> $(PTTMPME)/doc/download/index.htm
	echo "</body>" >> $(PTTMPME)/doc/download/index.htm
	echo "</html>" >> $(PTTMPME)/doc/download/index.htm
