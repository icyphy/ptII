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

ME =		java/pt/domains/pn/kernel

# Get configuration info
CONFIG =	$(ROOT)/mk/tycho.mk
include $(CONFIG)
#JAVAHOME =      /opt/jdk1.2latest

DIRS = 		demo #test doc

CLASSPATH = ../../../..

# Used to build zip files
TYPACKAGE = 	pt.domains.pn.kernel
TYPACKAGE_ROOTDIR = $(CLASSPATH)
TYPACKAGE_DIR = pt/domains/pn/kernel
TYVERSION =	1.0
TYDIST =	$(TYPACKAGE)$(TYVERSION)
TYZIP =		$(TYPACKAGE_DIR)/$(TYPACKAGE).zip
JARFILE = 	$(TYPACKAGE).jar

JSRCS = PNActor.java \
	PNCompositeActor.java \
	PNDirector.java \
	PNInPort.java \
	PNOutPort.java \
	PNPort.java \

EXTRA_SRCS =	$(JSRCS)

# Files or directories that are present, but that 'make checkjunk' 
# should not complain about
MISC_FILES =	$(DIRS) doc test

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
