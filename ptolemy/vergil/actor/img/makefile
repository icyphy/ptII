# Makefile for the Ptolemy II vergil/actor/img directory
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

ME =		ptolemy/vergil/actor/img

# Sub directories to run make in.
DIRS =

# Root of Ptolemy II directory
ROOT =		../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	img
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =	$(PTPACKAGE).jar


OTHER_FILES_TO_BE_JARED = \
	multi_in.gif \
	multi_in_o.gif \
	multi_in_on.gif \
	multi_in_ov.gif \
	multi_inout.gif \
	multi_inout_o.gif \
	multi_inout_on.gif \
	multi_inout_ov.gif \
	multi_out.gif \
	multi_out_o.gif \
	multi_out_on.gif \
	multi_out_ov.gif \
	relation.gif \
	relation_o.gif \
	relation_on.gif \
	relation_ov.gif \
	single_in.gif \
	single_in_o.gif \
	single_in_on.gif \
	single_in_ov.gif \
	single_inout.gif \
	single_inout_o.gif \
	single_inout_on.gif \
	single_inout_ov.gif \
	single_out.gif \
	single_out_o.gif \
	single_out_on.gif \
	single_out_ov.gif

EXTRA_SRCS = \
	$(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

include $(ROOT)/mk/ptno-compile.mk
