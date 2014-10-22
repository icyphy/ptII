# Makefile for Ptolemy II Continuous Actors
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1997-2014 The Regents of the University of California.
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

ME =		ptolemy/domains/continuous/lib

DIRS =		test

# Root of the Ptolemy II directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	lib
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	BandlimitedNoise.java \
	Derivative.java \
	DifferentialSystem.java \
	DiscreteClock.java \
	EventSource.java \
	Integrator.java \
	LevelCrossingDetector.java \
	LinearStateSpace.java \
	ContinuousMerge.java \
	ContinuousTimeDelay.java \
	ContinuousTransferFunction.java \
	Noise.java \
	PeriodicSampler.java \
	Waveform.java \
	ZeroOrderHold.java

OTHER_FILES_TO_BE_JARED = \
	BandlimitedNoiseIcon.xml \
	ContinuousClock.xml \
	ContinuousClockIcon.xml \
	ContinuousSinewave.xml \
	ContinuousSinewaveIcon.xml \
	ContinuousSpectrum.xml \
	ContinuousTimeDelayIcon.xml \
	ct2continuous \
	continuousentities.xml \
	DCMotor.xml \
	DCMotorIcon.xml \
	DCMotor.png \
	EventSource.java \
	integral.gif \
	IntegratorIcon.xml \
	LevelCrossingDetectorIcon.xml \
	NoiseIcon.xml \
	PWM.xml \
	PWMIcon.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'Integrator$$FunctionTerm.class'

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass $(PTCLASSJAR)


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
