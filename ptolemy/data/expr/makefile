# Makefile for Java Ptolemy Expression Parser classes
#
# @Authors: Christopher Hylands, Neil Smyth, based on a file by Thomas M. Parks
#
# $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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

# This directory contains classes used for the Ptolemy II parser, PtParser.
# The parser is generated using JavaCC and JJTree. It creates a syntax
# tree of Node objects. The node classes used by PtrParser when generating
# the tree are the ASTPt*.java files.

# Root of the Ptolemy II directory
ROOT =		../../..


# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

DIRS = 		test # doc

CLASSPATH = ../../..$(CLASSPATHSEPARATOR)$(JAVACC_DIR)/JavaCC.zip

# Used to build zip files
PTPACKAGE = 	ptolemy.data.expr
PTPACKAGE_ROOTDIR = $(CLASSPATH)
PTPACKAGE_DIR = ptolemy/data/expr
PTVERSION =	1.0
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSZIP =		$(PTPACKAGE_DIR)/$(PTPACKAGE).zip
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	ASTPtBitwiseNode.java \
	ASTPtFunctionNode.java \
	ASTPtFunctionalIfNode.java \
	ASTPtLeafNode.java \
	ASTPtLogicalNode.java \
	ASTPtMethodCallNode.java \
	ASTPtProductNode.java \
	ASTPtRelationalNode.java \
	ASTPtRootNode.java \
	ASTPtSumNode.java \
	ASTPtUnaryNode.java \
	Parameter.java \
	ParameterEvent.java \
	ParameterListener.java \
	UtilityFunctions.java

EXTRA_SRCS =	PtParser.jjt $(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) doc

# OPTIONAL_JSRCS files are created by javacc.
# javadoc generates html for files in $(OPTIONAL_JSRCS)
OPTIONAL_JSRCS = \
	ASCII_CharStream.java \
	JJTPtParserState.java \
	ParseException.java \
	Node.java \
	PtParserConstants.java \
	PtParserTokenManager.java \
	PtParserTreeConstants.java \
	SimpleNode.java \
	Token.java \
	TokenMgrError.java

# OPTIONAL_JSRCS depends on PtParser.java, but we want javadoc
# to generate docs for PtParser, so we modify the javadoc
# rule to also operate on DERIVED_JSRCS
DERIVED_JSRCS = PtParser.java

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =  \
	$(OPTIONAL_JSRCS) \
	message.txt \
	JJPtParserCalls.class \
	PtParser.java \
	PtParser.jj \
	'PtParser$$JJCalls.class message.txt'

JCLASS =  \
	$(JSRCS:%.java=%.class) \
	PtParser.class \
	$(OPTIONAL_JSRCS:%.java=%.class)


all:  $(OPTIONAL_JSRCS) $(JCLASS)
install: PtParser javadocs $(PTCLASSJAR)
docs: javadocs

depend:
	@echo "no dependencies in this directory"

$(OPTIONAL_JSRCS): PtParser.java

PtParser.java: PtParser.jj
	@echo "This command runs JavaCC, which can be downloaded from"
	@echo "http://www.sun.com/suntest"
	@echo "You only need install JavaCC if you modify PtParser.jjt"
	@echo "PtParser.java is derived from PtParser.jj, which is in turn"
	@echo "derived from PtParser.jjt"
	@echo "If you don't have JavaCC, you can workaround this with"
	@echo "   touch PtParser.jj PtParser.java"
	CLASSPATH="$(CLASSPATH)" $(JAVACC) PtParser.jj

PtParser.jj: $(EXTRA_SRCS)
	@echo "This command runs JJTree, which is part of JavaCC"
	@echo "JavaCC can be downloaded from http://www.sun.com/suntest"
	@echo "You only need install JavaCC if you modify PtParser.jjt"
	@echo "PtParser.java is derived from PtParser.jj, which is in turn"
	@echo "derived from PtParser.jjt"
	@echo "If you don't have JavaCC, you can workaround this with"
	@echo "   touch PtParser.jj PtParser.java"
	CLASSPATH="$(CLASSPATH)" $(JJTREE) PtParser.jjt

# This rule is useful for cvs committing the derived files
# For example:
# cvs commit -m "Updated PtParser." `make echo_OPTIONAL_JSRCS`
echo_OPTIONAL_JSRCS:
	@echo $(OPTIONAL_JSRCS) PtParser.java PtParser.jj

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
