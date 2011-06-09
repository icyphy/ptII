# Makefile for Java Ptolemy polymorphic actor classes
#
# @Authors: Shanna-Shaye Forbes. A modification of a makefile by Christopher Hylands which is
# based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2009-2010 The Regents of the University of California.
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

ME =		ptolemy/actor/lib/logic/fuzzy

DIRS = 		demo

# Root of the Java directory
ROOT =		../../../../..

# FuzzyEngine directory that contains fuzzy/FuzzyEngine.class
# http://people.clarkson.edu/~esazonov/FuzzyEngine.htm
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(PTFUZZYENGINE_DIR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	fuzzy
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	FuzzyLogic.java \
	ModelCreator.java \

OTHER_FILES_TO_BE_JARED = \
	Antenna.xml \
	FuzzyEngine-copyright.htm \
	fuzzy.xml \
	Power.xml \
	Propulsion.xml \
	TSSTPtolemyIIDocumentation.pdf \
	rules.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	demo \
	'FuzzyLogic$$FuzzyLogicVar.class' \
	'FuzzyLogic$$FuzzyParser.class' \
	'ModelCreator$$Architecture.class' \
	'ModelCreator$$Option.class'

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
