# Makefile for the ptolemy/actor/gt/controller package
#
# @Authors: Thomas Huining Feng, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2007-2009 The Regents of the University of California.
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
#
#

ME =		ptolemy/actor/gt/controller

DIRS = 		lib

# Root of the Ptolemy II directory
ROOT =		../../../..

CLASSPATH = 	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	controller
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	lib/lib.jar

PTCLASSALLJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	Clone.java \
	DesignPatternImporter.java \
	Execute.java \
	GTEvent.java \
	InitModel.java \
	InitModelWithContainer.java \
	InputModel.java \
	Match.java \
	ModelIOPort.java \
	ModelParameter.java \
	OutputModel.java \
	PersistenceAttribute.java \
	ReadModel.java \
	TestModel.java \
	Transform.java \
	TransformationAttribute.java \
	View.java \
	WriteModel.java

OTHER_FILES_TO_BE_JARED = \
	CloneIcon.xml \
	DesignPatternTransformer.xml \
	EmbeddedTransformationController.xml \
	EmbeddedTransformationControllerWithPorts.xml \
	EventLibrary.xml \
	ExecuteIcon.xml \
	GTEventIcon.xml \
	InitModelIcon.xml \
	InitModelWithContainerIcon.xml \
	InputModelIcon.xml \
	MatchIcon.xml \
	ModelBasedTransformation.xml \
	ModelUpdater.xml \
	ModelUpdaterIcon.xml \
	OutputModelIcon.xml \
	ReadModelIcon.xml \
	TestModelIcon.xml \
	TransformIcon.xml \
	TransformationController.xml \
	TransformationControllerIcon.xml \
	ViewIcon.xml \
	WriteModelIcon.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'DesignPatternImporter$$1.class' \
	'DesignPatternImporter$$MoMLContentFilter.class' \
	'Transform$$EmbeddedConfigurer.class' \
	'TransformationAttribute$$1$$1.class' \
	'TransformationAttribute$$1.class' \
	'TransformationAttribute$$TransformationListener.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
