# Makefile for SDF domain VQ actors data seq missa directory
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002-2005 The Regents of the University of California.
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

ME =		ptolemy/domains/sdf/lib/vq/data/seq/missa

DIRS =

# Root of the Java directory
ROOT =		../../../../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	missa
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS =

OTHER_FILES_TO_BE_JARED = \
	missa000.qcf \
	missa001.qcf \
	missa002.qcf \
	missa003.qcf \
	missa004.qcf \
	missa005.qcf \
	missa006.qcf \
	missa007.qcf \
	missa008.qcf \
	missa009.qcf \
	missa010.qcf \
	missa011.qcf \
	missa012.qcf \
	missa013.qcf \
	missa014.qcf \
	missa015.qcf \
	missa016.qcf \
	missa017.qcf \
	missa018.qcf \
	missa019.qcf \
	missa020.qcf \
	missa021.qcf \
	missa022.qcf \
	missa023.qcf \
	missa024.qcf \
	missa025.qcf \
	missa026.qcf \
	missa027.qcf \
	missa028.qcf \
	missa029.qcf

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: all jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
