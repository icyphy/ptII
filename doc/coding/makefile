# Makefile for Ptolemy II coding standards documentation
#
# Version: $Id$
# Makefile Author: Christopher Hylands
#
# Copyright (c) 1997-2003 The Regents of the University of California.
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

ME =		doc/coding

DIRS =

# Root of Ptolemy II directory
ROOT =		../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

OTHER_FILES_TO_BE_JARED = \
	debugging.htm

EXTRA_SRCS = \
	$(OTHER_FILES_TO_BE_JARED) \
	addinganactor.htm \
	applets.htm \
	body.htm \
	buttons.htm \
	debugging.htm \
	develsetup.htm \
	eclipse.htm \
	index.htm \
	javalinks.htm \
	main.htm \
	packages.htm \
	performance.htm \
	releasemgt.htm \
	reviews.htm \
	style.css \
	style.htm \
	sunlicense.htm \
	tcljava.htm \
	testing.htm \
	toc.htm

PTAUXALLJAR = codingConfig.jar

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) templates sftwareprac

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

all: $(EXTRA_SRCS)
install: all jars

# We don't include common.mk since we don't use the compiler here
include $(ROOT)/mk/ptno-compile.mk
