# Ptolemy code generation for JHDL demos
#
# @Authors: (makefile only) Christopher Hylands- based on a file by Thomas M. Parks
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

ME =		ptolemy/copernicus/jhdl/util

DIRS =

# Root of the Java directory
ROOT =		../../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)$(CLASSPATHSEPARATOR)$(JHDL_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	util
PTDIST =	$(PTPACKAGE)$(PTVERSION)
#PTCLASSJAR = 	$(PTPACKAGE).jar

# Alphabetical list of java source files
JSRCS =	\
	BlockGraphToDotty.java \
	DirectedGraphToDotty.java \
	GraphNode.java \
	GraphToDotty.java \
	MapList.java \
	HashListMap.java \
	IntIOPort.java \
	JHDLCircuitGraph.java \
	JHDLTestbench.java \
	JHDLUnsupportedException.java \
	Label.java \
	MuxNode.java \
	PtDirectedGraphToDotty.java \
	SuperBlock.java \
	SynthesisToDotty.java \
	UniqueVector.java \
	ModelGraph.java \
	CompositeModelGraph.java \
	ActorModelGraph.java

#	MergedControlFlowGraph.java \
#	DirectedPortGraph.java
#	PtolemyLogic.java
#	PtolemyCellToJHDLCircuit.java \
#	PtolemyAtomicCell.java \
#	PtolemyCell.java \
#	PtolemyCompositeCell.java \
#	PtolemyLogicCell.java \
#	PtolemyCompositeLogicCell.java \

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) \
	JHDLTestbench.java

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk



