# Makefile for Communicating Sequential Processes Dining Philosophers demo 
#
# @Authors: Christopher Hylands, Neil Smyth
#
# @Version: $Id$
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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

ME =		ptolemy/domains/csp/demo/DiningPhilosophers

# Root of the Java directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)

# Used to build jar files
PTPACKAGE = 	DiningPhilosophers
PTCLASSJAR = 	$(PTPACKAGE).jar

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

JSRCS = \
	Chopstick.java \
	Coordinate.java \
	DiningApplet.java \
	DiningPhilosophers.java \
	Philosopher.java \
	PhilosopherListener.java \
	TablePanel.java

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) \
	DiningPhilosophers.htm

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'DiningApplet$$CurrentTimeThread.class' \
	'DiningApplet$$EatingRateListener.class' \
	'DiningApplet$$GoButtonListener.class' \
	'DiningApplet$$StopButtonListener.class' \
	'DiningApplet$$ThinkingRateListener.class' \
	'TablePanel$$ChopstickCoords.class' \
	'TablePanel$$PhilosopherCoords.class'

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass $(PTCLASSJAR)
	@echo "To run all the demos, run 'make demo'"

install: all

demo:
	CLASSPATH="$(CLASSPATH)" appletviewer DiningPhilosophers.htm

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
