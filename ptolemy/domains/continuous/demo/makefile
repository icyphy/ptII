# Makefile for Ptolemy II Continuous domain demos
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks,
#
# @Version: $Id$
#
# @Copyright (c) 1998-2012 The Regents of the University of California.
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

ME =		ptolemy/domains/continuous/demo

# Root of the Java directory
ROOT =		../../../..

DIRS =		BouncingBall Brownian CarTracking \
		CartPendulum ComparedClocks ContinuousInsideDE \
		Derivative Friction FurutaPendulum \
		HierarchicalExecution HybridPlant Hysteresis \
		LevelCrossingDetector LevelCrossingDetectorDetectsGlitches \
		Lorenz NewtonsCradle Noise Poisson Pendulum3D SampledClocks Satellite \
		SigmaDelta SinuBouncingBall Sinusoid SquareWave \
		Starmac StickyMasses Switch \
		Thermostat Transmission TwoMachineInfiniteBusControl V2V

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	demo
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	BouncingBall/BouncingBall.jar \
	Brownian/Brownian.jar \
	CarTracking/CarTracking.jar \
	CartPendulum/CartPendulum.jar \
	ComparedClocks/ComparedClocks.jar \
	ContinuousInsideDE/ContinuousInsideDE.jar \
	Derivative/Derivative.jar \
	Friction/Friction.jar \
	FurutaPendulum/FurutaPendulum.jar \
	HierarchicalExecution/HierarchicalExecution.jar \
	HybridPlant/HybridPlant.jar \
	Hysteresis/Hysteresis.jar \
	LevelCrossingDetector/LevelCrossingDetector.jar \
	LevelCrossingDetectorDetectsGlitches/LevelCrossingDetectorDetectsGlitches.jar \
	Lorenz/Lorenz.jar \
	NewtonsCradle/NewtonsCradle.jar \
	Noise/Noise.jar \
	Pendulum3D/Pendulum3D.jar \
	Poisson/Poisson.jar \
	SampledClocks/SampledClocks.jar \
	Satellite/Satellite.jar \
	SigmaDelta/SigmaDelta.jar \
	SinuBouncingBall/SinuBouncingBall.jar \
	Sinusoid/Sinusoid.jar \
	SquareWave/SquareWave.jar \
	Starmac/Starmac.jar \
	StickyMasses/StickyMasses.jar \
	Switch/Switch.jar \
	Thermostat/Thermostat.jar \
	Transmission/Transmission.jar \
	TwoMachineInfiniteBusControl/TwoMachineInfiniteBusControl.jar \
	V2V/V2V.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Files that are not compiled, such as .tcl and .itcl files.
EXTRA_SRCS =	index.htm

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = 

all: jclass
	@echo "To run all the demos, run 'make demo'"

install: jclass jars

demo: subdemo

echoCTDemos:
	@echo "Brownian/*.xml CartPendulum/*.xml ComparedClocks/*.xml HierarchicalExecution/*.xml HybridPlant/*.xml Hysteresis/*.xml Pendulum3D/*.xml Poisson/*.xml SampledClocks/*.xml Satellite/*.xml SigmaDelta/*.xml SinuBouncingBall/*.xml SquareWave/*.xml Starmac/*.xml StickyMasses/*.xml Thermostat/*.xml Transmission/*.xml V2V/*.xml"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
