# Makefile for Ptolemy II code generation kernle
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

ME =		ptolemy/copernicus/kernel

# Root of the Java directory
ROOT =		../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	kernel
PTCLASSJAR = 	$(PTPACKAGE).jar

DIRS = fragment

# Keep this list alphabetized.
JSRCS = \
	AliasAnalysis.java \
	CastAndInstanceofEliminator.java \
	ClassWriter.java \
	Copernicus.java \
	DependedClasses.java \
	EntitySootClass.java \
	FastForwardFlowAnalysis.java \
	InvocationBinder.java \
	GeneratorAttribute.java \
	GrimpTransformer.java \
	JimpleWriter.java \
	KernelMain.java \
	LibraryUsageReporter.java \
	MakefileWriter.java \
	NullPointerAnalysis.java \
	PtolemyUtilities.java \
	SideEffectAnalysis.java \
	SideEffectFreeInvocationRemover.java \
	SootUtilities.java \
	TransformerAdapter.java \
	UnusedFieldRemover.java \
	WatchDogTimer.java 

OTHER_FILES_TO_BE_JARED = \
	Generator.xml \
	soot-license.html

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'LibraryUsageReporter$$1.class' \
	'Copernicus$$_StreamReaderThread.class' \
	'FastForwardFlowAnalysis$$1.class' \
	'KernelMain$$_IgnoreAllApplicationClasses.class' \
	'MaybeAliasAnalysis$$1.class' \
	'MaybeAliasAnalysis$$GraphFlow.class' \
	'SideEffectAnalysis$$EffectFlow.class' \
	'WatchDogTimer$$1.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass

install: all

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

