# Makefile for the Ptolemy II plotter documentation
#
# Version: $Id$
# Makefile Author: Christopher Hylands
#
# Copyright (c) 1997-2000 The Regents of the University of California.
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

ME = 		ptolemy/plot/doc

# Root of Ptolemy II directory
ROOT =		../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

EXTRA_SRCS = \
	acks.htm \
	body.htm \
	changes.htm \
	index.htm \
	install.htm \
	main.htm \
	plot.pdf \
	ptplotfaq.htm \
	thirdparty.htm \
	toc.htm \
	troubleshooting.htm

# Files or directories that are present, but that 'make checkjunk'
# should not complain about
MISC_FILES =	download img 

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

# Sub directories to run make in.
DIRS =

ALLJSRCS = 	*.java

all:

# We don't include common.mk since we don't use the compiler here
include $(ROOT)/mk/ptno-compile.mk
