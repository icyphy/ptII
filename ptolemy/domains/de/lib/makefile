# Makefile for the Continuous Time domain actor library
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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

# Note that the Java classes contained in this directory were written
# by Sun.  These classes came from tcljava, see license.terms for more info.

# Root of the Java directory
ROOT =		../../../..

ME =		ptolemy/domains/de/lib

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

DIRS = 		test #demo

CLASSPATH =	../../../..

# Used to build zip files
PTPACKAGE = 	ptolemy.domains.de.kernel
PTPACKAGE_ROOTDIR = $(CLASSPATH)
PTPACKAGE_DIR = ptolemy/domains/de/kernel
PTVERSION =	1.0
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSZIP =		$(PTPACKAGE_DIR)/$(PTPACKAGE).zip
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	DEABRO.java \
	DEABROalt.java \
	DEANDGate.java \
	DEBus.java \
	DEClock.java \
	DEDelay.java \
	DEFIFOQueue.java \
	DEFIRfilter.java \
	DEFullAdder.java \
	DEGain.java \
	DEHistogram.java \
	DEInterruptibleServer.java \
	DELogicAnalyzer.java \
	DENot.java \
	DEORGate.java \
	DEPassGate.java \
	DEPlot.java \
	DEPoisson.java \
	DEProcessor.java \
	DEPseudoPlot.java \
	DERamp.java \
	DERegister.java \
	DESampler.java \
	DEServer.java \
	DEServerAlt.java \
	DEStats.java \
	DEStatistics.java \
	DETestLevel.java \
	DEWaitingTime.java \
	DEWaveForm.java \
	DEXORGate.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	 $(DIRS) doc #demo

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass javadocs $(PTCLASSJAR)
docs: javadocs

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk



