# Makefile for the Ptolemy II design document
#
# Version: $Id$
# Makefile Author: Christopher Hylands
#
# Copyright (c) 1999-2003 The Regents of the University of California.
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

ME =		doc/design

# Sub directories to run make in.
# Don't include src directory, since we don't ship it with the release
DIRS =		usingVergil #src

# Root of Ptolemy II directory
ROOT =		../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

PTPACKAGE = 	design
PTCLASSJAR = $(PTPACKAGE).jar

OTHER_FILES_TO_BE_JARED = \
	ptIIdesign1-intro.pdf \
	ptIIdesign2-software.pdf \
	ptIIdesign3-domains.pdf

EXTRA_SRCS = \
	body.htm \
	buttons.htm \
	index.htm \
	main.htm \
	toc.htm


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	hyvisual.pdf \
	design1PDFPlaceHolder.pdf \
	design2PDFPlaceHolder.pdf \
	design3PDFPlaceHolder.pdf \
	html \
	src \
	usingVergil \
	usingVergil.pdf \
	visualsense.pdf


# Other jars to make, used by 'make jars'
OTHER_JARS = hyvisual.jar visualsense.jar

all: suball
install: all jars $(OTHER_JARS)

hyvisual.jar: hyvisual.pdf
	(cd $(ROOT); "$(JAR)" -cvf $(ME)/$@ $(ME)/hyvisual.pdf)

visualsense.jar: visualsense.pdf
	(cd $(ROOT); "$(JAR)" -cvf $(ME)/$@ $(ME)/visualsense.pdf)

# We don't include common.mk since we don't use the compiler here
include $(ROOT)/mk/ptno-compile.mk
