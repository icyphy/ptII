# Makefile for Java Ptolemy Parser classes
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

# Root of the Java directory
ROOT =		../../../..

ME =		java/pt/data/parser

# Get configuration info
CONFIG =	$(ROOT)/mk/tycho.mk
include $(CONFIG)

DIRS = 		test 

CLASSPATH = ../../..:/users/ptdesign/vendors/sun/JavaCC/JavaCC.zip

# Used to build zip files
TYPACKAGE = 	pt.data.parser
TYPACKAGE_ROOTDIR = $(CLASSPATH)
TYPACKAGE_DIR = pt/data/parser
TYVERSION =	1.0
TYDIST =	$(TYPACKAGE)$(TYVERSION)
TYZIP =		$(TYPACKAGE_DIR)/$(TYPACKAGE).zip
JARFILE = 	$(TYPACKAGE).jar

JSRCS = \
	ASTPtBitwiseOpNode.java \
	ASTPtFunctionNode.java \
	ASTPtLeafNode.java \
	ASTPtLogicalNode.java \
	ASTPtMiscNode.java \
	ASTPtProductNode.java \
	ASTPtRelationalNode.java \
	ASTPtRootNode.java \
	ASTPtSumNode.java \
	ASTPtUnaryNode.java 

EXTRA_SRCS =   PtParser.jjt $(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	# doc test

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =	#codeDoc

JCLASS = $(JSRCS:%.java=%.class)

# Needed for compiling under Cygwin on NT
LIB = dummylib

all: PtParser $(JCLASS) 
install: PTParser javadocs $(JARFILE)
docs: javadocs

depend:
	@echo "no dependencies in this directory"

PtParser :  sources PtParser.jjt
	rm -f PtParser.jj
	CLASSPATH=$(CLASSPATH) $(JJTREE) PtParser.jjt
	CLASSPATH=$(CLASSPATH) $(JAVACC) PtParser.jj
	CLASSPATH=$(CLASSPATH) $(JAVAC) *.java

# Get the rest of the rules
include $(ROOT)/mk/tycommon.mk

