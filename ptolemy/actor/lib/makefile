# Makefile for Java Ptolemy polymorphic actor classes
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2003 The Regents of the University of California.
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

ME =		ptolemy/actor/lib

# $PTII/configure looks for Java Advanced Imaging and sets PTJAI_DIR in
# $PTII/mk/ptII.mk if was found.  Otherwise, PTJAI_DIR is empty.
#
# $PTII/configure looks for Java Media Framework and sets PTJMF_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJMF_DIR is empty.
#
# $PTII/configure looks for the Joystick interface  and sets PTJOYSTICK_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJOYSTICK_DIR is empty.
#
# $PTII/configure looks for Jini and sets PTJSPACES_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJSPACES_DIR is empty.
#
# $PTII/configure looks for Jython and sets PTJYTHON_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJYTHON_DIR is empty.
#
# $PTII/configure looks for JXTA and sets PTJXTA_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJXTA_DIR is empty.
#
# $PTII/configure looks for X10 and sets PTX10_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTX10_DIR is empty.

DIRS = 		conversions gui logic hoc image javasound net io xslt \
		test comm security string $(PTJAI_DIR) \
		$(PTJMF_DIR) $(PTJOYSTICK_DIR) \
		$(PTJYTHON_DIR) $(PTJXTA_DIR) $(PTX10_DIR) #$(PTJSPACES_DIR)

# Root of the Java directory
ROOT =		../../..

# Do not add diva.jar to the CLASSPATH in actor/lib/makefile
# instead, move any actors that require diva.jar into actor/lib/gui -cxh
CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	lib
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	comm/comm.jar \
	conversions/conversions.jar \
	gui/gui.jar \
	hoc/hoc.jar \
	image/image.jar \
	io/io.jar \
	logic/logic.jar \
	javasound/javasound.jar \
	net/net.jar \
	security/security.jar \
	string/string.jar \
	xslt/xslt.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	AddSubtract.java \
	AbsoluteValue.java \
	Accumulator.java \
	ArrayAppend.java \
	ArrayAverage.java \
	ArrayElement.java \
	ArrayExtract.java \
	ArrayLength.java \
	ArrayLevelCrossing.java \
	ArrayMaximum.java \
	ArrayMinimum.java \
	ArrayPeakSearch.java \
	ArraySort.java \
	ArrayToElements.java \
	Average.java \
	Bernoulli.java \
	BooleanMultiplexor.java \
	BooleanSelect.java \
	BooleanSwitch.java \
	BusAssembler.java \
	BusDisassembler.java \
	ClassWrapper.java \
	Clock.java \
	Commutator.java \
	ComputeHistogram.java \
	Const.java \
	Counter.java \
	CurrentTime.java \
	Differential.java \
	DoubleReader.java \
	DB.java \
	Discard.java \
	DiscreteRandomSource.java \
	Distributor.java \
	ElementsToArray.java \
	Exec.java \
	Exit.java \
	Expression.java \
	FileWriter.java \
	Gaussian.java \
	GradientAdaptiveLattice.java \
	IIR.java \
	Interpolator.java \
	Lattice.java \
	LevinsonDurbin.java \
	Limiter.java \
	LinearDifferenceEquationSystem.java \
	LookupTable.java \
	MathFunction.java \
	MaxIndex.java \
	Maximum.java \
	Minimum.java \
	MonitorValue.java \
	Multiplexor.java \
	MultiplyDivide.java \
	NonStrictTest.java \
	OrderedMerge.java \
	PhaseUnwrap.java \
	PoissonClock.java \
	Pulse.java \
	Quantizer.java \
	Ramp.java \
	RandomSource.java \
	Reader.java \
	RecordAssembler.java \
	RecordDisassembler.java \
	RecordUpdater.java \
	Recorder.java \
	RecursiveLattice.java \
	Remainder.java \
	Rician.java \
	Scale.java \
	Select.java \
	SequenceActor.java \
	Sequencer.java \
	SequenceSource.java \
	SequentialClock.java \
	SetVariable.java \
	Sink.java \
	Sleep.java \
	Source.java \
	Stop.java \
	Switch.java \
	Synchronizer.java \
	Test.java \
	ThrowException.java \
	ThrowModelError.java \
	TimedActor.java \
	TimedSource.java \
	Transformer.java \
	TrigFunction.java \
	TriggeredClock.java\
	TrigFunction.java \
	TypeTest.java \
	Uniform.java \
	URLReader.java \
	URLDirectoryReader.java \
	VariableClock.java \
	VariableSleep.java \
	VectorAssembler.java \
	VectorDisassembler.java \
	WallClockTime.java \
	Writer.java

OTHER_FILES_TO_BE_JARED = \
	PowerEstimate.xml \
	Sinewave.xml \
	TimedSine.xml \
	aggregators.xml \
	array.xml \
	booleanflowcontrol.xml \
	busses.xml \
	communications.xml \
	esoteric.xml \
	executioncontrol.xml \
	filtering.xml \
	flowcontrol.xml \
	genericsinks.xml \
	genericsources.xml \
	math.xml \
	matrix.xml \
	random.xml \
	realtime.xml \
	sequencecontrol.xml \
	sequencesinks.xml \
	sequencesources.xml \
	signalprocessing.xml \
	sinks.xml \
	sources.xml \
	spectrum.xml \
	statistical.xml \
	test.xml \
	timedsinks.xml \
	timedsources.xml \

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	comm \
	doc \
	javasound \
	jspaces \
	'AbsoluteValue$$1.class' \
	'AbsoluteValue$$FunctionTerm.class' \
	'Expression$$1.class' \
	'Expression$$OutputTypeFunction.class' \
	'Expression$$VariableScope.class' \
	'RecordUpdater$$1.class' \
	'RecordUpdater$$FunctionTerm.class' \
	'Scale$$1.class' \
	'Scale$$PortParamFunction.class' \
	'Scale$$PortParameterFunction.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
