# Makefile for Ptolemy II JHDL code generation
#
# @Authors: Steve Neuendorffer, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2001-2003 The Regents of the University of California.
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

ME =		ptolemy/copernicus/jhdl/soot

# Root of the Java directory
ROOT =		../../../..

# $PTII/configure looks for Soot and JHDL and sets
# variables in $PTII/mk/ptII.mk
CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)$(CLASSPATHSEPARATOR)$(JHDL_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Keep this list alphabetized.
JSRCS = \
	AbstractCompoundExpression.java \
	BinaryMux.java \
	BooleanNotCompactor.java \
	BooleanNotExpr.java \
	CompoundAndExpression.java \
	CompoundBooleanExpression.java \
	CompoundOrExpression.java \
	ConditionalControlCompactor.java \
	ControlSootDFGBuilder.java \
	DirectedAcyclicCFG.java \
	DominatorCFG.java \
	DominatorHashMap.java \
	IntervalBlockDirectedGraph.java \
	IntervalChain.java \
	SootASTException.java \
	SootASTVisitor.java \
	SootBlockDirectedGraph.java \
	SootDFGBuilder.java \
	ValueMap.java

# should include: (move from above directory)
# - ConditionalControlCompactor.java
# - BooleanNotCompactor.java

OTHER_FILES_TO_BE_JARED =

#	compileCommandTemplate.txt
#	runCommandTemplate.txt

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'Main$$IgnoreAllApplicationClasses.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To run the demo, run 'make demo'"

install: all

test1: all
	java ptolemy.copernicus.jhdl.CircuitGenerator ptolemy.copernicus.jhdl.test.hwgentest method1

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

