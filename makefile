# Ptolemy II makefile
#
# @Version: $Id$
#
# Copyright (c) 1995-2000 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the above
# copyright notice and the following two paragraphs appear in all copies
# of this software.
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
#						PT_COPYRIGHT_VERSION_2
#						COPYRIGHTENDKEY
##########################################################################

ME =		ptII

# Order matters here.
# Go into util first so we get the latest version of the testsuite
# Go into com before compiling ptolemy so we get JLex
DIRS = util com ptolemy bin doc

# Root of Ptolemy II directory
ROOT =		.

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

EXTRA_SRCS = \
	README \
	copyright.txt \
	configure.in \
	configure

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = \
	$(DIRS) \
	bin \
	config \
	lib \
	mk

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	adm \
	config.log \
	config.status \
	config.cache \
	confTest.class \
	logs \
	public_html \
	tcl \
	vendors 

# Files to be removed by 'make clean'
KRUFT =

# Files to be removed by 'make distclean'
DISTCLEAN_STUFF = \
	mk/ptII.mk config.log config.status config.cache

all: mk/ptII.mk suball
install: subinstall

# Glimpse is a tool that prepares an index of a directory tree.
# glimpse is not included with Ptolemy II, see http://glimpse.cs.arizona.edu
GLIMPSEINDEX =	/usr/sww/bin/glimpseindex
glimpse: .glimpse_exclude
	@echo "Saving .glimpse_exclude, removing the .glimpse* files"
	rm -f glimpse_exclude
	cp .glimpse_exclude glimpse_exclude
	rm -f .glimpse*
	cp  glimpse_exclude .glimpse_exclude
	$(GLIMPSEINDEX) -H `pwd` `pwd`
	chmod a+r .glimpse_*
	rm -f glimpse_exclude

# Generate ptII.mk by running configure
mk/ptII.mk: configure mk/ptII.mk.in
	./configure

configure: configure.in
	autoconf

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
