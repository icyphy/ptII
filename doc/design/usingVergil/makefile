# Makefile for the Ptolemy II doc/design/usingVergil directory
#
# Author: Christopher Hylands (makefile only)
# Version: $Id$
#
# Copyright (c) 2001 The Regents of the University of California.
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
DIRS =

# Root of Ptolemy II directory
ROOT =		../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	usingVergil
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =	$(PTPACKAGE).jar


OTHER_FILES_TO_BE_JARED = \
	index-1.gif \
	index-10.gif \
	index-11.gif \
	index-12.gif \
	index-13.gif \
	index-14.gif \
	index-15.gif \
	index-16.gif \
	index-17.gif \
	index-18.gif \
	index-19.gif \
	index-2.gif \
	index-20.gif \
	index-21.gif \
	index-22.gif \
	index-23.gif \
	index-24.gif \
	index-25.gif \
	index-26.gif \
	index-27.gif \
	index-28.gif \
	index-29.gif \
	index-3.gif \
	index-30.gif \
	index-31.gif \
	index-32.gif \
	index-33.gif \
	index-34.gif \
	index-35.gif \
	index-36.gif \
	index-37.gif \
	index-38.gif \
	index-39.gif \
	index-4.gif \
	index-40.gif \
	index-41.gif \
	index-42.gif \
	index-43.gif \
	index-44.gif \
	index-45.gif \
	index-46.gif \
	index-47.gif \
	index-48.gif \
	index-49.gif \
	index-5.gif \
	index-50.gif \
	index-51.gif \
	index-52.gif \
	index-53.gif \
	index-54.gif \
	index-55.gif \
	index-56.gif \
	index-57.gif \
	index-58.gif \
	index-59.gif \
	index-6.gif \
	index-60.gif \
	index-61.gif \
	index-62.gif \
	index-63.gif \
	index-64.gif \
	index-7.gif \
	index-8.gif \
	index-9.gif \
	index.css \
	index.htm


EXTRA_SRCS = \
	$(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =


include $(ROOT)/mk/ptno-compile.mk
