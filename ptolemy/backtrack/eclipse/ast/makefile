# Makefile for the Ptolemy backtracking package
#
# @Authors: Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2005-2013 The Regents of the University of California.
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

ME =		ptolemy/backtrack/eclipse/ast

DIRS = 		transform test

# Root of the Java directory
ROOT =		../../../..

# PTBACKTRACK_ECLIPSE_JARS is set in $PTII/mk/ptII.mk
# PTBACKTRACK_ECLIPSE_JARS always has a leading CLASSPATHSEPARATOR
CLASSPATH =	$(ROOT)$(PTBACKTRACK_ECLIPSE_JARS)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ast
PTCLASSJAR = 	$(PTPACKAGE).jar

# Don't use GNU make extensions here
#    $(wildcard *.java)
JSRCS = \
	ASTBuilder.java \
	ASTClassNotFoundException.java \
	ASTDump.java \
	ASTException.java \
	ASTFormatter.java \
	ASTIORuntimeException.java \
	ASTMalformedException.java \
	ASTResolutionException.java \
	ASTRuntimeException.java \
	LocalClassLoader.java \
	Transformer.java \
	Type.java \
	TypeAnalyzer.java \
	TypeAnalyzerState.java \
	UnknownASTException.java

OTHER_FILES_TO_BE_JARED = \
	eclipse-copyright.htm

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'LocalClassLoader$$ClassImport.class' \
	'Transformer$$InnerClassFilter.class' \
	'TypeAnalyzer$$1.class' \
	'TypeAnalyzerState$$CurrentClassElement.class' \
	'TypeAnalyzer$$TypeAndOwner.class' \

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
