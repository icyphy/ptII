# Ptolemy makefile for jgoodies
#
# @Version: $Id$
# @Author: Christopher Brooks
#
# @Copyright (c) 2007 The Regents of the University of California.
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

ME = 		com/jgoodies/forms/layout

DIRS =

# Root of Ptolemy II directory
ROOT =		../../../..

CLASSPATH = 	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	layout
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS = \
	BoundedSize.java \
	CellConstraints.java \
	ColumnSpec.java \
	ConstantSize.java \
	FormLayout.java \
	FormSpec.java \
	RowSpec.java \
	Size.java \
	Sizes.java

EXTRA_SRCS =	$(JSRCS)

# Files or directories that are present, but that 'make checkjunk'
# should not complain about
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'CellConstraints$$1.class' \
	'CellConstraints$$Alignment.class' \
	'ConstantSize$$1.class' \
	'ConstantSize$$Unit.class' \
	'FormLayout$$1.class' \
	'FormLayout$$CachingMeasure.class' \
	'FormLayout$$ComponentSizeCache.class' \
	'FormLayout$$LayoutInfo.class' \
	'FormLayout$$Measure.class' \
	'FormLayout$$MinimumHeightMeasure.class' \
	'FormLayout$$MinimumWidthMeasure.class' \
	'FormLayout$$PreferredHeightMeasure.class' \
	'FormLayout$$PreferredWidthMeasure.class' \
	'FormSpec$$1.class' \
	'FormSpec$$DefaultAlignment.class' \
	'Sizes$$1.class' \
	'Sizes$$ComponentSize.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
