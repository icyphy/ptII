# Makefile for Ptolemy II PN domain demos
#
# @Authors: Mudit Goel, Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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

ME = 		ptolemy/domains/pn/demo

DIRS =		QR

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Files that are not compiled, such as .tcl and .itcl files.
EXTRA_SRCS =	index.htm \


# Files or directories that are present, but that 'make checkjunk'
# should not complain about
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =

all: jclass
	@echo "To run all the demos, run 'make demo'"

install: all

demo: subdemo

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
