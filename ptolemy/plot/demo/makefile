# Makefile for Java ptplot program, demo directory
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
ROOT =		../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/tycho.mk
include $(CONFIG)

JPACKAGE = 	ptplot.demo
JPACKAGE_DIR =	ptplot/demo
JVERSION =	1.1
JDIST =		$(JPACKAGE)$(JVERSION)
JZIP =		ptplot/demo/$(JPACKAGE).zip

DIRS = 		audio #doc test

CLASSPATH =	../..

# Order matters here.
JSRCS1_0 = \
	PlotDemo.java \
	PlotFourierSeries.java \
	PlotLiveDemo.java \
	PlotLiveDemoApplet.java \

# Sources that require JDK1.1 and later.
JSRCS1_1 = \

JSRCS = $(JSRCS1_0) $(JSRCS1_1)

EXTRA_SRCS = $(JSRCS) \
	BarGraph.html \
	Binary.html \
	Butterfly.html \
	ErrorBars.html \
	FourierSeries.html \
	Live.html \
	LogAxes.html \
	Marks.html \
	Ptplot.html \
	Ptplot.js \
	PtplotHelp.html \
	Sinusoids.html \
	bargraph.plt \
	data.plt \
	errorbars.plt \
	index.html \
	logaxes.plt

# The data directory contains binary plot files that cannot be checked
# into sccs.
MISC_FILES = data $(DIRS) doc #test 

JCLASS1_0 = $(JSRCS1_0:%.java=%.class)
JCLASS1_1 = $(JSRCS1_1:%.java=%.class)

JCLASS = $(JCLASS1_0) $(JCLASS1_1)

all: sources jclass suball #$(JZIP)
install: sources jclass javadocs subinstall #$(JZIP) 
docs: javadocs

tests:: $(EXTRA_SRCS)
	echo "Sorry no automatic Java tests yet"

# Get the rest of the rules
include $(ROOT)/mk/tycommon.mk
