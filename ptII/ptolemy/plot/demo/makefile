# Makefile for Java ptplot program, demo directory
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2009 The Regents of the University of California.
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

ME =		ptolemy/plot/demo

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

PTPACKAGE = 	demo
PTCLASSJAR = 	$(PTPACKAGE).jar

DIRS =

# Order matters here.
JSRCS = \
	HistogramDemo.java \
	PlotDemo.java \
	PlotFourierSeries.java \
	PlotLiveDemo.java \
	PlotLiveDemoApplet.java \
	TwoPlotExample.java

OTHER_FILES_TO_BE_JARED = \
	bargraph.plt \
	data.plt \
	errorbars.plt \
	logaxes.plt \
	plotmlSample.txt \
	sinusoids.xml

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED) \
	BarGraph.htm \
	Binary.htm \
	Butterfly.htm \
	EditablePlot.htm \
	Endian.htm \
	ErrorBars.htm \
	FourierSeries.htm \
	Histogram.htm \
	Live.htm \
	LogAxes.htm \
	Marks.htm \
	MarksSimple.htm \
	TwoPlotExample.java \
	Sinusoids.htm \
	index.htm \
	sinusoidsShow.htm \
	toc.htm

# The data directory contains binary plot files that cannot be checked
# into sccs.
MISC_FILES = data $(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	TwoPlotExample.class \
	'TwoPlotExample$$1.class' \
	'PlotLiveDemo$$1$$1.class' \
	'PlotLiveDemo$$1.class' \
	'PlotLiveDemo$$2.class'

JCLASS = $(JSRCS:%.java=%.class)

all: sources jclass suball #$(JZIP)
install: sources jclass subinstall #$(JZIP)


tests:: $(EXTRA_SRCS)
	echo "Sorry no automatic Java tests yet"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
