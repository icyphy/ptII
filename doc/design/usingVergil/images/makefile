# Makefile for the Ptolemy II doc/design/usingVergil/images directory
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

ME =		doc/design/usingVergil/images

# Sub directories to run make in.
DIRS =

# Root of Ptolemy II directory
ROOT =		../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	images
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =	$(PTPACKAGE).jar


OTHER_FILES_TO_BE_JARED = \
        navidx.gif \
	navnext.gif \
	navprev.gif \
	navtoc.gif \
	usingVergil10.gif \
	usingVergil11.gif \
	usingVergil12.gif \
	usingVergil13.gif \
	usingVergil14.gif \
	usingVergil15.gif \
	usingVergil16.gif \
	usingVergil17.gif \
	usingVergil18.gif \
	usingVergil19.gif \
	usingVergil2.gif \
	usingVergil20.gif \
	usingVergil21.gif \
	usingVergil22.gif \
	usingVergil23.gif \
	usingVergil24.gif \
	usingVergil25.gif \
	usingVergil26.gif \
	usingVergil27.gif \
	usingVergil28.gif \
	usingVergil29.gif \
	usingVergil3.gif \
	usingVergil30.gif \
	usingVergil31.gif \
	usingVergil32.gif \
	usingVergil33.gif \
	usingVergil34.gif \
	usingVergil35.gif \
	usingVergil36.gif \
	usingVergil37.gif \
	usingVergil38.gif \
	usingVergil39.gif \
	usingVergil4.gif \
	usingVergil40.gif \
	usingVergil41.gif \
	usingVergil42.gif \
	usingVergil43.gif \
	usingVergil44.gif \
	usingVergil45.gif \
	usingVergil46.gif \
	usingVergil47.gif \
	usingVergil48.gif \
	usingVergil49.gif \
	usingVergil5.gif \
	usingVergil50.gif \
	usingVergil51.gif \
	usingVergil52.gif \
	usingVergil53.gif \
	usingVergil54.gif \
	usingVergil55.gif \
	usingVergil56.gif \
	usingVergil57.gif \
	usingVergil58.gif \
	usingVergil59.gif \
	usingVergil6.gif \
	usingVergil60.gif \
	usingVergil61.gif \
	usingVergil62.gif \
	usingVergil63.gif \
	usingVergil64.gif \
	usingVergil65.gif \
	usingVergil66.gif \
	usingVergil67.gif \
	usingVergil68.gif \
	usingVergil69.gif \
	usingVergil7.gif \
	usingVergil70.gif \
	usingVergil71.gif \
	usingVergil72.gif \
	usingVergil73.gif \
	usingVergil74.gif \
	usingVergil75.gif \
	usingVergil76.gif \
	usingVergil77.gif \
	usingVergil78.gif \
	usingVergil79.gif \
	usingVergil80.gif \
	usingVergil81.gif \
	usingVergil82.gif \
	usingVergil83.gif \
	usingVergil84.gif \
	usingVergil85.gif \
	usingVergil86.gif \
	usingVergil87.gif \
	usingVergil88.gif \
	usingVergil89.gif \
	usingVergil8.gif \
	usingVergil9.gif \
	usingVergila.gif

EXTRA_SRCS = \
	$(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

install: jars

include $(ROOT)/mk/ptno-compile.mk
