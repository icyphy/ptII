# Makefile for Ptolemy PN domain kernel demos 
#
# @Authors: Mudit Goel, Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1997 The Regents of the University of California.
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
ROOT =		../../../../../..

ME =		java/pt/domains/pn/kernel/demo

# Get configuration info
CONFIG =	$(ROOT)/mk/tycho.mk
include $(CONFIG)

DIRS = 		#test doc

# Used to build zip files
JPACKAGE = 	pt.domains.pn.kernel.demo
JPACKAGE_DIR = 	pt/domains/pn/kernel/demo
JVERSION =	1.0
JDIST =		$(JPACKAGE)$(JVERSION)
JZIP =		$(JPACKAGE_DIR)/$(JPACKAGE).zip
JARFILE = 	$(JPACKAGE).jar

CLASSPATH = ../../../../..

JSRCS = \
	PNExampleApplet.java

EXTRA_SRCS =	$(JSRCS) \
	PNExample.html \
	PNPrimeExample.tcl

# Files or directories that are present, but that 'make checkjunk' 
# should not complain about
MISC_FILES =	doc #test

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass

install: jclass javadocs
docs: javadocs

tests:: $(EXTRA_SRCS) test_prime_java

test_prime: test_prime_java test_prime_tclblend

# Use java
test_prime_java:
	CLASSPATH=$(CLASSPATH) $(JAVA) pt.domains.pn.kernel.PNPrimeExample 50

# Use Tcl Blend
test_prime_tclblend:
	tyjtclsh PNPrimeExample.tcl

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/tycommon.mk
