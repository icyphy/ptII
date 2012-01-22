# Makefile for helper classes that match ptolemy.actor.lib classes
#
# @Version: $Id$
# @Author: Christopher Brooks
#
# @Copyright (c) 2005-2009 The Regents of the University of California
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA  OR RESEARCH IN MOTION
# LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL,
# OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND
# ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA OR
# RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA  AND RESEARCH IN MOTION LIMITED
# HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
##########################################################################

ME =		ptolemy/codegen/c/actor/lib

# PTCOLT_DIR is set to colt if colt was found
DIRS =		comm $(PTCOLT_DIR) conversions gui hoc io javasound jni \
		logic string test tutorial

# Root of the Java directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	lib
PTCLASSJAR =

PTCLASSALLJAR = $(PTPACKAGE).jar

#Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	colt/colt.jar \
	comm/comm.jar \
	conversions/conversions.jar \
	gui/gui.jar \
	hoc/hoc.jar \
	io/io.jar \
	javasound/javasound.jar \
	jni/jni.jar \
	logic/logic.jar \
	string/string.jar

# Keep this list alphabetized.
JSRCS = \
	AbsoluteValue.java \
	Accumulator.java \
	AddSubtract.java \
	ArrayAppend.java \
	ArrayAverage.java \
	ArrayElement.java \
	ArrayElementAsMatrix.java \
	ArrayExtract.java \
	ArrayLength.java \
	ArrayLevelCrossing.java \
	ArrayMaximum.java \
	ArrayMinimum.java \
	ArrayPeakSearch.java \
	ArrayToElements.java \
	Average.java \
	Bernoulli.java \
	BooleanMultiplexor.java \
	BooleanSelect.java \
	BooleanSwitch.java \
	Commutator.java \
	Const.java \
	Counter.java \
	CurrentTime.java \
	Differential.java \
	Discard.java \
	DiscreteRandomSource.java \
	Distributor.java \
	ElementsToArray.java \
	Exit.java \
	Expression.java \
	Gaussian.java \
	GradientAdaptiveLattice.java \
	Interpolator.java \
	Lattice.java \
	Limiter.java \
	LookupTable.java \
	Maximum.java \
	Minimum.java \
	MovingAverage.java \
	MultiplyDivide.java \
	NonStrictTest.java \
	OrderedMerge.java \
	Publisher.java \
	PublisherTest.java \
	Pulse.java \
	Quantizer.java \
	Ramp.java \
	RandomSource.java \
	RecursiveLattice.java \
	Remainder.java \
	Rician.java \
	Scale.java \
	Sequence.java \
	SetVariable.java \
	Sink.java \
	Sleep.java \
	Stop.java \
	StringConst.java \
	Subscriber.java \
	SubscriptionAggregator.java \
	Synchronizer.java \
	Test.java \
	ThrowException.java \
	ThrowModelError.java \
	TrigFunction.java \
	UnaryMathFunction.java \
	Uniform.java \
	VectorAssembler.java \
	VectorDisassembler.java \
	WallClockTime.java

OTHER_FILES_TO_BE_JARED = \
	AbsoluteValue.c \
	Accumulator.c \
	AddSubtract.c \
	ArrayAppend.c \
	ArrayAverage.c \
	ArrayElement.c \
	ArrayElementAsMatrix.c \
	ArrayExtract.c \
	ArrayLength.c \
	ArrayLevelCrossing.c \
	ArrayMaximum.c \
	ArrayMinimum.c \
	ArrayPeakSearch.c \
	ArrayToElements.c \
	Average.c \
	Bernoulli.c \
	BooleanMultiplexor.c \
	BooleanSelect.c \
	BooleanSwitch.c \
	Commutator.c \
	Const.c \
	Counter.c \
	CurrentTime.c \
	Differential.c \
	Discard.c \
	DiscreteRandomSource.c \
	Distributor.c \
	ElementsToArray.c \
	Exit.c \
	Expression.c \
	Gaussian.c \
	GradientAdaptiveLattice.c \
	Interpolator.c \
	Lattice.c \
	Limiter.c \
	LookupTable.c \
	Maximum.c \
	Minimum.c \
	MovingAverage.c \
	MultiplyDivide.c \
	NonStrictTest.c \
	OrderedMerge.c \
	Publisher.c \
	PublisherTest.c \
	Pulse.c \
	Quantizer.c \
	Ramp.c \
	RandomSource.c \
	RecursiveLattice.c \
	Remainder.c \
	Rician.c \
	Scale.c \
	Sequence.c \
	SetVariable.c \
	Sink.c \
	Sleep.c \
	Stop.c \
	StringConst.c \
	Subscriber.c \
	SubscriptionAggregator.c \
	Synchronizer.c \
	Test.c \
	ThrowException.c \
	ThrowModelError.c \
	TrigFunction.c \
	UnaryMathFunction.c \
	Uniform.c \
	VectorAssembler.c \
	VectorDisassembler.c \
	WallClockTime.c \
	aggregators.xml \
	array.xml \
	booleanflowcontrol.xml \
	executioncontrol.xml \
	filtering.xml \
	flowcontrol.xml \
	genericsinks.xml \
	genericsources.xml \
	math.xml \
	random.xml \
	sequencesinks.xml \
	sequencesources.xml \
	signalprocessing.xml \
	sinks.xml \
	sources.xml \
	test.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED) \
	checkGreen

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES = comm colt conversions gui hoc io javasound jni \
	logic string test tutorial

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	doc \
	'Expression$$VariableScope.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass

install: all jars $(PTPACKAGE).jar $(PTPACKAGE_NATIVE_JAR)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
