# Makefile for the Ptolemy II ptolemy/vergil/ptolemy/img directory
#
# Author: Christopher Hylands (makefile only)
# Version: $Id$
#
# Copyright (c) 2001-2009 The Regents of the University of California.
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

ME =		ptolemy/vergil/basic/img

# Sub directories to run make in.
DIRS =

# Root of Ptolemy II directory
ROOT =		../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	img
PTCLASSJAR =	$(PTPACKAGE).jar


OTHER_FILES_TO_BE_JARED = \
	fullscreen.gif \
	fullscreen_o.gif \
	fullscreen_on.gif \
	fullscreen_ov.gif \
	pause.gif \
	pause_o.gif \
	pause_on.gif \
	pause_ov.gif \
	print.gif \
	print_o.gif \
	print_on.gif \
	print_ov.gif \
	run.gif \
	run_o.gif \
	run_on.gif \
	run_ov.gif \
	save.gif \
	save_o.gif \
	save_on.gif \
	save_ov.gif \
	stop.gif \
	stop_o.gif \
	stop_on.gif \
	stop_ov.gif \
	up.gif \
	up_o.gif \
	up_on.gif \
	up_ov.gif \
	zoomfit.gif \
	zoomfit_o.gif \
	zoomfit_on.gif \
	zoomfit_ov.gif \
	zoomin.gif \
	zoomin_o.gif \
	zoomin_on.gif \
	zoomin_ov.gif \
	zoomout.gif \
	zoomout_o.gif \
	zoomout_on.gif \
	zoomout_ov.gif \
	zoomreset.gif \
	zoomreset_o.gif \
	zoomreset_on.gif \
	zoomreset_ov.gif

EXTRA_SRCS = \
	$(OTHER_FILES_TO_BE_JARED)


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

all:

install: jars

include $(ROOT)/mk/ptno-compile.mk
