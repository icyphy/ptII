# Makefile for classes that support unit systems
#
# @Authors: Christopher Hylands, Neil Smyth based on a file by Thomas M. Parks
#
# $Id$
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

ME =		ptolemy/data/unit

DIRS = 		test demo 

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	unit
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS = \
	BaseUnit.java \
	Bindings.java \
	EquationVisitor.java \
	ExpandPortNames.java \
	Unit.java \
	UnitCategory.java \
	UnitConstraint.java \
	UnitConstraints.java \
	UnitEquation.java \
	UnitExpr.java \
	UnitLibrary.java \
	UnitPresentation.java \
	UnitAttribute.java \
	UnitSystem.java \
	UnitTerm.java \
	UnitUtilities.java

OTHER_FILES_TO_BE_JARED = \
	unitSystems.xml \
	BasicUnits.xml \
	CGSUnitBase.xml \
	ElectronicUnitBase.xml \
	SI.xml \
	UnitConversions.xml

EXTRA_SRCS =	UParser.jj $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# if $(DIRS) includes demo, then do not set MISC_FILES= $(DIRS) 
MISC_FILES = test

# OPTIONAL_JSRCS files are created by javacc.
# javadoc generates html for files in $(OPTIONAL_JSRCS)
#	ASCII_CharStream.java 
OPTIONAL_JSRCS = \
	ParseException.java \
	SimpleCharStream.java \
	Token.java \
	TokenMgrError.java \
	UParserConstants.java \
	UParserTokenManager.java


# OPTIONAL_JSRCS depends on UParser.java, but we want javadoc
# to generate docs for UParser, so we modify the javadoc
# rule to also operate on DERIVED_JSRCS
DERIVED_JSRCS = UParser.java


# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	doc \
	$(OPTIONAL_JSRCS) \
	UParser.java \
	'Solver$$1.class' \
	'Solver$$Index.class' \
	'UParser$$1.class' \
	'UParser$$JJCalls.class' \
	'UParser$$LookaheadSuccess.class' \
	'UnitLibrary$$UnitNameExprPair.class'

JCLASS =  \
	$(JSRCS:%.java=%.class) \
	UParser.class \
	$(OPTIONAL_JSRCS:%.java=%.class)


all: $(OPTIONAL_JSRCS) $(JCLASS)
install: jclass $(PTCLASSJAR)

UParser.java: UParser.jj
	@echo "This command runs JavaCC, which can be downloaded from"
	@echo "http://www.experimentalstuff.com/Technologies/JavaCC/"
	@echo "See \$$PTII/doc/install.htm for details."
	@echo "You only need install JavaCC if you modify UParser.jj"
	@echo "UParser.java is derived from UParser.jj"
	@echo "If you don't have JavaCC, you can workaround this with"
	@echo "   touch UParser.jj UParser.java"
	CLASSPATH="$(CLASSPATH)" 
	$(JAVACC) UParser.jj

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
