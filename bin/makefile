# Makefile for scripts that invoke Ptolemy II Java classes
#
# @Author: Christopher Hyland
#
# @Version: $Id$
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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

ME =		bin

DIRS =

# Root of the Java directory
ROOT =		..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Bourne Shell scripts for Unix and Cygwin
SHELL_FILES = \
	histogram \
	ptinvoke \
	ptjacl \
	ptjaclapp \
	ptjini \
	ptplot \
	ptolemy \
	pxgraph \
	vergil 

# DOS batch files
BAT_FILES = \
	histogram.bat \
	ptplot.bat \
	ptolemy.bat \
	pxgraph.bat \
	vergil.bat 

EXTRA_SRCS = \
	ptinvoke.in \
	ptjacl.in \
	$(BAT_FILES)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = $(SHELL_FILES)

all: ptinvoke
	-chmod a+x ptinvoke
	rm -f histogram
	ln -s ptinvoke histogram
	rm -f ptjacl
	ln -s ptinvoke ptjacl
	rm -f ptjaclapp
	ln -s ptinvoke ptjaclapp
	rm -f ptjini
	ln -s ptinvoke ptjini
	rm -f ptolemy
	ln -s ptinvoke ptolemy
	rm -f ptplot
	ln -s ptinvoke ptplot
	rm -f pxgraph
	ln -s ptinvoke pxgraph
	rm -f vergil
	ln -s ptinvoke vergil

install: all
	-chmod a+x $(SHELL_FILES) $(BAT_FILES)

depend:
	@echo "no dependencies in this directory"

demo: all

ptinvoke: ptinvoke.in ../config.status
	(cd ..; ./config.status)
# zip might not get the permissions of configure right, so we run /bin/sh
../config.status: ../configure
	(cd ..; /bin/sh ./configure)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
