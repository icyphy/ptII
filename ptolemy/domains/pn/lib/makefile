# Makefile for the Java classes used to implement the Ptolemy PN domain kernel
#
# @Authors: Mudit Goel, Christopher Hylands, based on a file by Thomas M. Parks
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
ROOT =		../../../../..

ME =		java/pt/domains/pn/stars

# Get configuration info
CONFIG =	$(ROOT)/mk/tycho.mk
include $(CONFIG)
#JAVAHOME =      ../../../../../../jdk1.1.5

DIRS = 		demo #test doc

# Used to build zip files
JPACKAGE = 	pt.domains.pn.stars
JPACKAGE_DIR = 	pt/domains/pn/stars
JVERSION =	1.0
JDIST =		$(JPACKAGE)$(JVERSION)
JZIP =		$(JPACKAGE_DIR)/$(JPACKAGE).zip
JARFILE = 	$(JPACKAGE).jar

CLASSPATH = ../../../..

JSRCS = \
	PNAdder.java \
	PNAlternate.java \
	PNAudioPacker.java \
	PNAudioSource.java \
	PNCirConvFilter.java \
	PNDAConverter.java \
	PNInterleave.java \
	PNQuantizer.java \
	PNRDController.java \
	PNRDDecider.java \
	PNRDDecoder.java \
	PNRDIdentifier.java \
	PNRDInfo.java \
	PNRDMainController.java \
	PNRamp.java \
	PNRedirect.java \
	PNSieve.java \
	PNSink.java \

EXTRA_SRCS =	$(JSRCS)

# Files or directories that are present, but that 'make checkjunk' 
# should not complain about
MISC_FILES =  Constan	$(DIRS) doc test

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(JARFILE)

JCLASS = $(JSRCS:%.java=%.class)

all: jclass

install: jclass javadocs
docs: javadocs

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/tycommon.mk
