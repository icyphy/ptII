# Makefile for Ptolemy II - HLA/CERTI demos
#
# @Authors: Christopher Brooks
#
# @Version: $Id: makefile 77282 2017-11-27 17:10:54Z cxh $
#
# @Copyright (c) 2017-2018 The Regents of the University of California.
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
#                       PT_COPYRIGHT_VERSION_2
#                       COPYRIGHTENDKEY

ME =	org/hlacerti/demo/MicrostepReset

DIRS =	MicrostepResetNER \
	MicrostepResetNERbis \
	MicrostepResetTAR \
	MicrostepResetTAR1 \
	MicrostepResetTARmicroStepDelay \
	MicrostepResetTARmicroStepDelayBoth \
	MicrostepResetTARmicroStepDelayRAV \
	MicrostepResetTARnoCornerCase1 \
	MicrostepResetTARnoInternalEvent \
	MicrostepResetTARtimeDelayRAV \
	TimeDataResolutionSeveralRAVinsideResolNoIntEvent

# Root of the Java directory
ROOT =      ../../../..

CLASSPATH = $(ROOT)

# Get configuration info
CONFIG =    $(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE =     MicrostepReset
#PTCLASSJAR =    $(PTPACKAGE).jar
PTCLASSALLJAR= $(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	MicrostepResetNER/MicrostepResetNER.jar \
	MicrostepResetNERbis/MicrostepResetNERbis.jar \
	MicrostepResetTAR/MicrostepResetTAR.jar \
	MicrostepResetTARmicroStepDelay/MicrostepResetTARmicroStepDelay.jar \
	MicrostepResetTARmicroStepDelayBoth/MicrostepResetTARmicroStepDelayBoth.jar \
	MicrostepResetTARmicroStepDelayRAV/MicrostepResetTARmicroStepDelayRAV.jar \
	MicrostepResetTARnoCornerCase1/MicrostepResetTARnoCornerCase1.jar \
	MicrostepResetTARnoInternalEvent/MicrostepResetTARnoInternalEvent.jar \
	MicrostepResetTARtimeDelayRAV/MicrostepResetTARtimeDelayRAV.jar \
	TimeDataResolutionSeveralRAVinsideResolNoIntEvent/TimeDataResolutionSeveralRAVinsideResolNoIntEvent.jar

JSRCS = 

OTHER_FILES_TO_BE_JARED = \
	MicrostepReset.xml

EXTRA_SRCS = \
    $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass $(PTCLASSJAR)
	@echo "To run all the demos, run 'make demo'"


install: all jars

demo: demo_ptolemy demo_vergil

demo_ptolemy:
	"$(PTII)/bin/ptolemy" Billard.xml

demo_vergil:
	"$(PTII)/bin/vergil" Billard.xml

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
