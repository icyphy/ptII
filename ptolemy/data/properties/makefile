# Makefile for Java Ptolemy Data Properties
#
# @Authors: Christopher Brooks, based on a file by Thomas M. Parks
#
# $Id$
#
# @Copyright (c) 2007 The Regents of the University of California.
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

ME =		ptolemy/data/properties

DIRS =      actor configuredSolvers demo gui lattice test token util 

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	properties
PTCLASSJAR =

PTCLASSALLJAR = $(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	actor/actor.jar \
	configuredSolvers/configuredSolvers.jar \
	demo/demo.jar \
	gui/gui.jar \
	lattice/lattice.jar \
	token/token.jar \
	util/properties.jar

JSRCS = \
	AnalyzerAttribute.java \
	AnalyzeModel.java \
	AnnotationAttribute.java \
	ModelWriter.java \
	ParseTreeAnnotationEvaluator.java \
	ParseTreeASTNodeHelperCollector.java \
	Property.java \
	Propertyable.java \
	PropertyAttribute.java \
	PropertyFailedRegressionTestException.java \
	PropertyHelper.java \
	PropertyableNamedObj.java \
	PropertyableObject.java \
	PropertyRemover.java \
	PropertyResolutionException.java \
	PropertySolverBase.java \
	PropertySolver.java \
	SharedUtilities.java

OTHER_FILES_TO_BE_JARED = \
	AnalyzeModelIcon.xml \
	EventLibrary.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo here, otherwise make sources tries to run demos
MISC_FILES =	actor configuredSolvers gui lattice test token util 

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	doc \
	'PropertyHelper$$Channel.class' \
	'PropertyHighlighter$$1.class' \
	'PropertyHighlighter$$ClearDisplay.class' \
	'PropertyHighlighter$$ClearProperty.class' \
	'PropertyHighlighter$$ConfigureHighlightAction.class' \
	'PropertyHighlighter$$EditHighlightDialog.class' \
	'PropertyHighlighter$$HighlightProperty$$1.class' \
	'PropertyHighlighter$$HighlightProperty.class' \
	'PropertyHighlighter$$HighlighterController.class' \
	'PropertyHighlighter$$ShowProperty$$1.class' \
	'PropertyHighlighter$$ShowProperty.class' \
	'PropertyRemover$$1.class' \
	'PropertySolver$$1.class'

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass jars
#$(EXTRA_SRCS) jclass 
propertyTestReports:: test_properties_reports

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
