# Makefile for the PN GUI demos
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

ME =		ptolemy/domains/pn/demo/RunLength

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

DIRS =

CLASSPATH = $(DIVA_JAR)$(CLASSPATHSEPARATOR)$(ROOT)

# Used to build jar files
PTPACKAGE = 	RLEDivaApplet
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = ImageDisplay.java \
	MatrixPacker.java \
	MatrixUnpacker.java \
	PNImageSink.java \
	PNImageSource.java \
	RLDecoder.java \
	RLEDiva.java \
	RLEncoder.java \
	ThreadDemo.java 

EXTRA_SRCS =	$(JSRCS) \
	RLEDiva.htm \
	RLEncoding.gif \
	ptII.gif \
	ptII.pbm

# Files or directories that are present, but that 'make checkjunk'
# should not complain about
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	'ImageDisplay$$_PictureFrame.class' \
	'ImageDisplay$$_PictureFrameListener.class' \
	'RLEDiva$$1.class' \
	'RLEDiva$$2.class' \
	'RLEDiva$$3.class' \
	'RLEDiva$$4.class' \
	'RLEDiva$$5.class' \
	'RLEDiva$$6.class' \
	'RLEDiva$$7.class' \
	'RLEDiva$$LayoutListener.class' \
	'RLEDiva$$StateListener.class' \
	'RLEDiva$$ThreadRenderer.class' \
	'ThreadDemo$$1.class' \
	'ThreadDemo$$2.class' \
	'ThreadDemo$$3.class' \
	'ThreadDemo$$StateListener.class' \
	'ThreadDemo$$ThreadRenderer.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass $(PTCLASSJAR)
	@echo "To run all the demos, run 'make demo'"

install: all

demo:
	CLASSPATH="$(CLASSPATH)" appletviewer RLEDiva.htm

# 'make clean' removes everything in KRUFT
KRUFT = $(PTCLASSJAR)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
