# Makefile for ECSL actor classes
#
# @Authors: Christopher Brooks, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2004-2005 The Regents of the University of California.
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

ME =		ptolemy/actor/lib/ecsl

DIRS = 		# test

# Root of the Java directory
ROOT =		../../../..

# Do not add diva.jar to the CLASSPATH in actor/lib/makefile
# instead, move any actors that require diva.jar into actor/lib/gui -cxh
CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ecsl
PTCLASSJAR =    $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	ECSLAbs.java \
	ECSLClock.java \
	ECSLConstant.java \
	ECSLDemux.java \
	ECSLDiscreteFilter.java \
	ECSLDiscretePulseGenerator.java \
	ECSLDisplay.java \
	ECSLFrom.java \
	ECSLGain.java \
	ECSLGoto.java \
	ECSLGround.java \
	ECSLIntegrator.java \
	ECSLLogic.java \
	ECSLMerge.java \
	ECSLMinMax.java \
	ECSLMultiPortSwitch.java \
	ECSLMux.java \
	ECSLProduct.java \
	ECSLRelationalOperator.java \
	ECSLSFunction.java \
	ECSLSaturate.java \
	ECSLSignum.java \
	ECSLSin.java \
	ECSLStep.java \
	ECSLSum.java \
	ECSLSwitch.java \
	ECSLTerminator.java \
	ECSLToWorkspace.java \
	ECSLUnitDelay.java \
	ECSLUtilities.java \
	ECSLZeroOrderHold.java

OTHER_FILES_TO_BE_JARED = \
	Scope.moml \
	Step.moml \
	ecsl.mk

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED) \
	ecsl2moml


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =


JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

SAXON_JAR = $(ROOT)/lib/saxon7.jar

foo: ECSLUtilities.class
	$(JAVA) -classpath "$(ROOT)$(CLASSPATHSEPARATOR)$(SAXON_JAR)" \
		ptolemy.actor.lib.ecsl.ECSLUtilities \
		$(ROOT)/ptolemy/actor/lib/ecsl/demo/cruiseControl/cruiseControlECSL.xml \
		$(ROOT)/ptolemy/actor/lib/ecsl/demo/cruiseControl/cruiseControlECSL.moml

foo2: ECSLUtilities.class
	$(JAVA) -classpath "$(ROOT)$(CLASSPATHSEPARATOR)$(SAXON_JAR)" \
		ptolemy.actor.lib.ecsl.ECSLUtilities \
		$(ROOT)/ptolemy/actor/lib/ecsl/demo/etc_control/etc_controlECSL.xml \
		$(ROOT)/ptolemy/actor/lib/ecsl/demo/etc_control/etc_controlECSL.moml

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk








