# Makefile for the Ptolemy runtime configs
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2000-2003 The Regents of the University of California.
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

ME =		ptolemy/configs

# If you add a configuration, be sure to add the jar file
# in PTCLASSALLJARS below
DIRS =		doc dsp full hyvisual jni jxta ptiny visualsense test

# Root of the Java directory
ROOT =		../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	configs
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

JSRCS =

# These files go into PTCLASSALLJAR
# ptIIplanet.gif should be in ../../doc/img, but Web Start
#  has problems pulling in the gif if the gif is in a different jar
#  so we include it in this jar
OTHER_FILES_TO_BE_JARED = \
	README.txt \
	annotation.xml \
	automataActorLibrary.xml \
	basicActorLibrary.xml \
	basicDirectors.xml \
	basicLibrary.xml \
	basicUtilities.xml \
	basicUtilitiesFSM.xml \
	defaultFullConfiguration.xml \
	experimentalDirectors.xml \
	extendedEffigyFactories.xml \
	extendedTableauFactories.xml \
	fsmEffigyFactories.xml \
	fsmLibrary.xml \
	fsmTableauFactory.xml \
	graphEffigyFactory.xml \
	graphTableauFactory.xml \
	iaEffigyFactory.xml \
	iaTableauFactory.xml \
	ptIIplanet.gif \
	runConfiguration.xml \
	runPanelConfiguration.xml \
	runWelcomeWindow.xml \
	utilitiesLibrary.xml \
	utilityCompositeActor.xml \
	utilityIntRangeParameter.xml \
	utilityModalModel.xml \
	utilityModelReference.xml \
	utilityParameter.xml \
	utilityParameters.xml \
	utilityPortParameter.xml \
	utilityRequireVersion.xml \
	utilityRunCompositeActor.xml \
	utilityScopeExtender.xml

# Include these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		doc/doc.jar \
		dsp/dsp.jar \
		full/full.jar \
		hyvisual/hyvisual.jar \
		jni/jni.jar \
		jxta/jxta.jar \
		ptiny/ptiny.jar \
		visualsense/visualsense.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "nothing need be done for make all in this directory"

install: jclass jars
	@echo "nothing need be done for make install in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk



