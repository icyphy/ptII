# Makefile the Ptolemy II test suite that uses Tcl Blend
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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

# Root of the Java directory
ROOT =		../..

ME =		util/testsuite

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

TCL_SRCS = \
	description.tcl \
	enums.tcl \
	init.tcl \
	jdktools.tcl \
	models.tcl \
	tclshrc.tcl \
	testDefs.tcl \
	testEnums.tcl \
	testParameters.tcl

# Shell scripts that should be made executable
SCRIPTS = \
	chkextra \
	chkjava \
	html2ps \
	jindent \
	mkreview \
	ptIIbuildfast \
	ptIIbuildtest \
	ptIIdirs \
	ptspell \
	rmtrailingline \
	rmtrailingspace

EXTRA_SRCS =	$(TCL_SRCS) $(SCRIPTS) \
	jindent.el \
	ptjavastyle.el \
	ptlocaldict \

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = README.jindent

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =	doc

# Do make sources so that we ensure that the subdirectories are checked out
all:  sources $(MISC_SRCS)
	-chmod a+x $(SCRIPTS)

install: all

# Make a distribution of jindent
JINDENT_VERSION=jindent1.0
JINDENT_DIST=$(JINDENT_VERSION).tar.gz
JINDENT_TMP=/tmp/$(JINDENT_VERSION)
jindent_dist:
	rm -rf $(JINDENT_TMP)
	mkdir -p $(JINDENT_TMP)/util/testsuite
	cp README.jindent $(JINDENT_TMP)
	cp jindent jindent.el ptjavastyle.el $(JINDENT_TMP)/util/testsuite
	chmod a+x $(JINDENT_TMP)/util/testsuite/jindent
	(cd /tmp; gtar -zcf $(JINDENT_DIST) $(JINDENT_VERSION))


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
