# Makefile for Ptalon model
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2009 The Regents of the University of California.
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

# This directory contains polymorphic actors.

ME =		ptolemy/apps/ptalon/model

DIRS =		controls #test

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)${CLASSPATHSEPARATOR}${ROOT}/ptolemy/actor/ptalon/antlr/antlr.jar

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	model
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	controls/controls.jar

# Do not include gt.jar because it is not part of Ptalon in Ptiny. 
#	gt/gt.jar \

PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	AbstractPtalonModelEvaluator.java \
	ActorInstantiator.java \
	LoopedAstReference.java \
	PtalonModel.java \
	PtalonModelEvaluator.java \
	PtalonModelScope.java \
	ReversableHashtable.java \
	TransparentRelation.java

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

ANTLR = "$(JAVA)" -classpath "$(ANTLR_DIR)/antlr.jar" antlr.Tool
# OPTIONAL_JSRCS are created by Antlr
OPTIONAL_JSRCS =

# Files or directories that are present, but that 'make checkjunk'
# should not complain about
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES = test		

OPTIONAL_FILES =  \
	doc \
	demo \
	bugs \
	controls \
	$(OPTIONAL_JSRCS) \
	'AbstractPtalonModelEvaluator$$IfTree.class' \
	'AbstractPtalonModelEvaluator$$PtalonExpressionScope.class'

JCLASS = \
	$(JSRCS:%.java=%.class) \
	$(OPTIONAL_JSRCS:%.java=%.class)

all: $(OPTIONAL_JSRCS) jclass
install: all jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
