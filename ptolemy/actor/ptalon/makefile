# Makefile for Java Ptolemy polymorphic actor classes
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2009 The Regents of the University of California.
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

ME =		ptolemy/actor/ptalon

DIRS =		doc gt lib demo test

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)${CLASSPATHSEPARATOR}${ROOT}/ptolemy/actor/ptalon/antlr/antlr.jar

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ptalon
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	lib/lib.jar

# Do not include gt.jar because it is not part of Ptalon in Ptiny. 
#	gt/gt.jar \

PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	AbstractPtalonEvaluator.java \
	NamedTree.java \
	PtalonActor.java \
	PtalonAST.java \
	PtalonEvaluator.java \
	PtalonExpressionParameter.java \
	PtalonMLHandler.java \
	PtalonParameter.java \
	PtalonRuntimeException.java \
	PtalonScopeException.java \
	Tester.java

OTHER_FILES_TO_BE_JARED = \
	ptalon-copyright.html \
	ptalon.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED) \
	PtalonLexer.smap \
	PtalonPopulator.smap \
	PtalonPopulatorTokenTypes.txt \
	PtalonRecognizer.smap \
	PtalonScopeChecker.smap \
	PtalonScopeCheckerTokenTypes.txt \
	PtalonTokenTypes.txt \
	antlr \
	data.txt \
	parser.g \
	populator.g \
	scopeChecker.g \
	startantlr

ANTLR = "$(JAVA)" -classpath "$(ANTLR_DIR)/antlr.jar" antlr.Tool
# OPTIONAL_JSRCS are created by Antlr
OPTIONAL_JSRCS = \
	PtalonRecognizer.java \
	PtalonLexer.java \
	PtalonPopulator.java \
	PtalonPopulatorTokenTypes.java \
	PtalonScopeChecker.java \
	PtalonTokenTypes.java

# Files or directories that are present, but that 'make checkjunk'
# should not complain about
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =	doc gt lib test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =  \
	doc \
	demo \
	bugs \
	$(OPTIONAL_JSRCS) \
	'AbstractPtalonEvaluator$$IfTree.class' \
	'AbstractPtalonEvaluator$$PtalonExpressionScope.class' \
	'PtalonEvaluator$$ActorTree.class' \
	$(DISTCLEAN_STUFF) \
	PtalonPopulatorTokenTypes.class \
	PtalonScopeCheckerTokenTypes.class \
	PtalonTokenTypes.class


JCLASS = \
	$(JSRCS:%.java=%.class) \
	$(OPTIONAL_JSRCS:%.java=%.class)

all: $(OPTIONAL_JSRCS) jclass
install: all jars

# Derived java files
PtalonRecognizer.java: parser.g
	$(ANTLR) parser.g
PtalonLexer.java: parser.g
	$(ANTLR) parser.g
PtalonPopulator.java: populator.g
	$(ANTLR) populator.g
PtalonScopeChecker.java: scopeChecker.g
	$(ANTLR) scopeChecker.g

# This rule is useful for cvs committing the derived files
# For example:
# cvs commit -m "Updated MatrixParser." `make echo_OPTIONAL_JSRCS`
echo_OPTIONAL_JSRCS:
	@echo $(OPTIONAL_JSRCS)

# Files to be removed by make distclean
DISTCLEAN_STUFF = \
	PtalonTokenTypes.java \
	PtalonTokenTypes.txt \
	PtalonLexer.java \
	PtalonLexer.smap \
	PtalonRecognizer.java \
	PtalonRecognizer.smap \
	PtalonScopeCheckerTokenTypes.java \
	PtalonScopeCheckerTokenTypes.txt \
	PtalonScopeChecker.java \
	PtalonScopeChecker.smap \
	PtalonPopulatorTokenTypes.java \
	PtalonPopulatorTokenTypes.txt \
	PtalonPopulator.java \
	PtalonPopulator.smap

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
