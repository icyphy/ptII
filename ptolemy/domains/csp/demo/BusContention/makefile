# Makefile for the CSP BusContention Demo
#
# @Authors: Mudit Goel, Christopher Hylands, based on a file by Thomas M. Parks
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

# Root of the Java directory
ROOT =		../../../../..

ME =		ptolemy/domains/csp/demo/BusContention

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

DIRS = 		# demo test doc

# Used to build jar files
PTPACKAGE = 	BusContention
PTCLASSJAR = 	$(PTPACKAGE).jar

CLASSPATH = $(DIVA_JAR)$(CLASSPATHSEPARATOR)$(ROOT)

JSRCS = \
	BusContentionApplet.java \
	ContentionAlarm.java \
	Controller.java \
	Memory.java \
	PortChannelCode.java \
	Processor.java

EXTRA_SRCS = \
	$(JSRCS) \
	BusContentionApplet.htm

# Files or directories that are present, but that 'make checkjunk'
# should not complain about
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	'BusContentionApplet$$1.class' \
	'BusContentionApplet$$2.class' \
	'BusContentionApplet$$3.class' \
	'BusContentionApplet$$4.class' \
	'BusContentionApplet$$BusContentionGraphController.class' \
	'BusContentionApplet$$LayoutListener.class' \
	'BusContentionApplet$$LocalEdgeRenderer.class' \
	'BusContentionApplet$$StateListener.class' \
	'BusContentionApplet$$ThreadRenderer.class' \

JCLASS = $(JSRCS:%.java=%.class)

all: jclass  $(PTCLASSJAR)
	@echo "To run all the demos, run 'make demo'"

install: all


# 'make clean' removes everything in KRUFT
KRUFT =


depend:
	@echo "no dependencies in this directory"

demo:
	CLASSPATH="$(CLASSPATH)" appletviewer BusContentionApplet.htm

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
