# Makefile for the Ptolemy II doc/design/usingVergil directory
#
# Author: Christopher Hylands (makefile only)
# Version: $Id$
#
# Copyright (c) 2001-2005 The Regents of the University of California.
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
##########################################################################

ME =		doc/design/usingVergil

# Sub directories to run make in.
DIRS =		images

# Root of Ptolemy II directory
ROOT =		../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	usingVergil
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	images/images.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

OTHER_FILES_TO_BE_JARED = \
	index.htm
	usingVergila2.htm \
	usingVergila3.htm \
	usingVergila4.htm \
	usingVergila5.htm \
	usingVergila6.htm \
	usingVergila7.htm \
	usingVergila8.htm \
	usingVergila9.htm \
	usingVergila10.htm \
	usingVergila11.htm \
	usingVergila12.htm


EXTRA_SRCS = \
	$(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

install: jars

updateimages:
	for file in usingVergil/Output/images/usingVergil[0-9]*.gif; \
	do \
		cp $$file `echo $$file | sed 's@usingVergil/Output/images/usingVergil@index-@'g`; \
	done

WEBWORKSSRC=../src/webworks/usingVergil4/Output
update:
	cp $(WEBWORKSSRC)/*.html .
	cp $(WEBWORKSSRC)/images/* images
	mv usingVergil.html index.htm
	files="index.htm"; \
	for file in $$files; \
	do \
		echo "$$file"; \
		sed -e 's@background="images/backgrnd.gif"@@g' \
			-e 's@face="Verdana, Arial, Helvetica, sans-serif"@@g' \
			-e 's@"Stop button@"Stop" button@g' \
			-e 's@expressions.html@expressions.htm@g' \
		< $$file > /tmp/usingVergil.tmp; \
		mv /tmp/usingVergil.tmp $$file; \
	done

include $(ROOT)/mk/ptno-compile.mk
