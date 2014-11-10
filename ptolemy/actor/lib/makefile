# Makefile for Java Ptolemy polymorphic actor classes
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2014 The Regents of the University of California.
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

# $PTII/configure looks for Colt and sets PTCOLT_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTCOLT_DIR is empty.

# $PTII/configure looks for ojdbc6.jar and sets PTDATABASE_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTDATABASE_DIR is empty.
#
# $PTII/configure looks for jxl.jar and sets PTEXCEL_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTEXCEL_DIR is empty.
#
# $PTII/configure looks for jna.jar and sets PTFMI_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTFMI_DIR is empty.
#
# $PTII/configure looks for Java Advanced Imaging and sets PTJAI_DIR in
# $PTII/mk/ptII.mk if was found.  Otherwise, PTJAI_DIR is empty.
#
# $PTII/configure looks for JavaMail sets PTJAVAMAIL_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJAVAMAIL_DIR is empty.

# $PTII/configure looks for JavaScript js.jar and  sets PTJAVASCRIPT_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJAVASCRIPT_DIR is empty.

# $PTII/configure looks for Java Media Framework and sets PTJMF_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJMF_DIR is empty.
#
# $PTII/configure looks for the Joystick interface  and sets PTJOYSTICK_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJOYSTICK_DIR is empty.
#
# $PTII/configure looks for Jython and sets PTJYTHON_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJYTHON_DIR is empty.
#
# $PTII/configure looks for Open Computer Vision and sets PTOPENCV_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTOPENCV_DIR is empty.
#
# $PTII/configure looks for the Vertx jars in $PTII/lib and sets PTVERTX_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTVERTX_DIR is empty.
#
DIRS = 		aspect conversions gui logic hoc image test \
		javasound jopio net io xslt \
		test comm security string tutorial vhdl video \
		$(PTCOLT_DIR) $(PTDATABASE_DIR) $(PTFMI_DIR) \
		$(PTEXCEL_DIR) $(PTJAI_DIR) $(PTJAVAMAIL_DIR) \
		$(PTJAVASCRIPT) \
		$(PTJMF_DIR) $(PTJOYSTICK_DIR) \
		$(PTJYTHON_DIR) $(PTOPENCV_DIR) \
		$(PTVERTX_DIR)

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

# Include these jars in PTAUXALLJAR
# plotmlapplet pulls in the com/microstar/xml and util/ classes
PTAUXALLJARS = \
	comm/comm.jar \
	conversions/conversions.jar \
	hoc/hoc.jar \
	io/io.jar \
	image/image.jar \
	logic/logic.jar \
	string/string.jar

# Common actors for Ptolemy Kore
PTAUXALLJAR =	libKore.jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	$(PTAUXALLJARS) \
	fmi/fmi.jar \
	gui/gui.jar \
	javasound/javasound.jar \
	jopio/jopio.jar \
	net/net.jar \
	security/security.jar \
	vhdl/vhdl.jar \
	video/video.jar \
	xslt/xslt.jar

PTCLASSALLJAR = $(PTPACKAGE).jar


# Keep this list alphabetized.
JSRCS = \
	AddSubtract.java \
	AbsoluteValue.java \
	Accumulator.java \
	ArrayAccumulate.java \
	ArrayAppend.java \
	ArrayAverage.java \
	ArrayContains.java \
	ArrayElement.java \
	ArrayElementAsMatrix.java \
	ArrayExtract.java \
	ArrayLength.java \
	ArrayLevelCrossing.java \
	ArrayMaximum.java \
	ArrayMinimum.java \
	ArrayPeakSearch.java \
	ArrayRemoveElement.java \
	ArraySort.java \
	ArraySum.java \
	ArrayToElements.java \
	ArrayToMatrix.java \
	ArrayUpdate.java \
	Assert.java \
	Average.java \
	Bernoulli.java \
	BooleanMultiplexor.java \
	BooleanSelect.java \
	BooleanSwitch.java \
	BusAssembler.java \
	BusDisassembler.java \
	CatchExceptionAttribute.java \
	ClassWrapper.java \
	Clock.java \
	Commutator.java \
	ComputeHistogram.java \
	ConfigurationSelect.java \
	ConfigurationSwitch.java \
	Const.java \
	Counter.java \
	CurrentDate.java \
	CurrentMicrostep.java \
	CurrentTime.java \
	DateConstructor.java \
	DateElements.java \
	Default.java \
	DelayStart.java \
	Differential.java \
	DoubleReader.java \
	DB.java \
	Dictionary.java \
	Discard.java \
	DiscreteClock.java \
	DiscreteRandomSource.java \
	Distributor.java \
	ElementsToArray.java \
	ExceptionManager.java \
	ExceptionManagerModel.java \
	ExceptionSubscriber.java \
	Exec.java \
	ExecutionTime.java \
	Exit.java \
	Expression.java \
	FileWriter.java \
	Gaussian.java \
	GetCausalityInterface.java \
	GradientAdaptiveLattice.java \
	IIR.java \
	Interpolator.java \
	Lattice.java \
	LevinsonDurbin.java \
	Limiter.java \
	LimitedFiringSource.java \
	LinearDifferenceEquationSystem.java \
	LookupTable.java \
	MathFunction.java \
	MatrixToArray.java \
	MaxIndex.java \
	Maximum.java \
	MicrostepDelay.java \
	Minimum.java \
	ModifyDate.java \
	MovingAverage.java \
	MovingMaximum.java \
	Multiplexor.java \
	MultiplyDivide.java \
	NonStrictTest.java \
	OrderedMerge.java \
	OrderedRecordAssembler.java \
	Pause.java \
	PhaseUnwrap.java \
	PoissonClock.java \
	Publisher.java \
	PublisherNonStrictTest.java \
	PublisherTest.java \
	Pulse.java \
	Quantizer.java \
	Ramp.java \
	RandomInteger.java \
	RandomSource.java \
	Reader.java \
	RecordAssembler.java \
	RecordDisassembler.java \
	RecordUpdater.java \
	Recorder.java \
	RecursiveLattice.java \
	Remainder.java \
	RemoveNilTokens.java \
	ResettableTimer.java \
	Rician.java \
	RunningMaximum.java \
	RunningMinimum.java \
	Sampler.java \
	Scale.java \
	Select.java \
	Sequence.java \
	SequenceActor.java \
	Sequencer.java \
	SequenceSource.java \
	SequentialClock.java \
	SetVariable.java \
	SingleEvent.java \
	SingleTokenCommutator.java \
	Sink.java \
	Sleep.java \
	Source.java \
	Stop.java \
	StringConst.java \
	SubMatrix.java \
	Subscriber.java \
	SubscriptionAggregator.java \
	Switch.java \
	Synchronizer.java \
	SynchronizeToRealTime.java \
	Test.java \
	TestExceptionAttribute.java \
	TestExceptionHandler.java \
	ThrowException.java \
	ThrowModelError.java \
	TimeDelay.java \
	TimedSource.java \
	TopLevelCurrentTime.java \
	Transformer.java \
	Triangular.java \
	TrigFunction.java \
	TriggeredClock.java\
	TrigFunction.java \
	TypeTest.java \
	UnaryMathFunction.java \
	Uniform.java \
	UnionDisassembler.java \
	UnionMerge.java \
	URLReader.java \
	URLDirectoryReader.java \
	VariableClock.java \
	VariableSleep.java \
	VectorAssembler.java \
	VectorDisassembler.java \
	WallClockTime.java \
	Writer.java

OTHER_FILES_TO_BE_JARED = \
	AbsoluteValueIcon.xml \
	AccumulatorIcon.png \
	AccumulatorIcon.xml \
	ArrayAccumulateIcon.xml \
	ArrayAppendIcon.xml \
	ArrayAverageIcon.xml \
	ArrayContainsIcon.xml \
	ArrayElementAsMatrixIcon.xml \
	ArrayElementIcon.xml \
	ArrayExtractIcon.xml \
	ArrayLengthIcon.xml \
	ArrayLevelCrossingIcon.xml \
	ArrayMaximumIcon.xml \
	ArrayMinimumIcon.xml \
	ArraySumIcon.xml \
	ArrayPeakSearchIcon.xml \
	ArrayRemoveElementIcon.xml \
	ArraySortIcon.xml \
	ArrayUpdateIcon.xml \
	AverageIcon.xml \
	BernoulliIcon.xml \
	BooleanMultiplexorIcon.xml \
	BooleanSelectIcon.xml \
	BooleanSwitchIcon.xml \
	ChopMultirate.xml \
	CommutatorIcon.xml \
	ComputeHistogramIcon.xml \
	ConfigurationSelectIcon.xml \
	ConfigurationSwitchIcon.xml \
	CounterIcon.xml \
	CurrentDateIcon.xml \
	DateConstructorIcon.xml \
	DateElementsIcon.xml \
	DBIcon.xml \
	DefaultIcon.xml \
	Dice.gif \
	DictionaryIcon.xml \
	DifferentialIcon.xml \
	DiscardIcon.xml \
	DiscreteRandomSourceIcon.xml \
	DistributorIcon.xml \
	GaussianIcon.xml \
	IIRIcon.xml \
	InterpolatorIcon.xml \
	LimiterIcon.xml \
	LookupTableIcon.xml \
	MaximumIcon.xml \
	MicrostepDelayIcon.xml \
	MinimumIcon.xml \
	ModifyDateIcon.xml \
	MultiplexorIcon.xml \
	MovingAverageIcon.xml \
	TriangularIcon.xml \
	PowerEstimate.xml \
	PulseIcon.xml \
	QuantizerIcon.xml \
	RemainderIcon.xml \
	ResettableTimerIcon.xml \
	RunningMaximumIcon.xml \
	RunningMinimumIcon.xml \
	SelectIcon.xml \
	SequencerIcon.xml \
	Sinewave.gif \
	Sinewave.xml \
	SinewaveIcon.xml \
	SingleEventIcon.xml \
	SwitchIcon.xml \
	SynchronizeToRealTimeIcon.xml \
	TimeDelayIcon.xml \
	TimedSine.xml \
	TriggeredSinewave.xml \
	TriggeredSinewaveIcon.xml \
	UniformIcon.xml \
	aggregators.xml \
	array.xml \
	booleanflowcontrol.xml \
	busses.xml \
	communications.xml \
	datastores.xml \
	esoteric.xml \
	exception.xml \
	executioncontrol.xml \
	filtering.xml \
	flowcontrol.xml \
	genericsinks.xml \
	genericsources.xml \
	math.xml \
	matrix.xml \
	random.xml \
	RandomIntegerIcon.xml \
	randomNoColt.xml \
	realtime.xml \
	RicianIcon.xml \
	sequencecontrol.xml \
	SequenceIcon.xml \
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
	jai \
	javasound \
	jmf \
	jni \
	joystick \
	jjs \
	js \
	mail \
	opencv \
	reactable \
	vertx \
	'AbsoluteValue$$1.class' \
	'AbsoluteValue$$FunctionTerm.class' \
	'AbsoluteValue$$TypeOfAbsoluteValue.class' \
	'ArrayElement$$ArrayTypeFunction.class' \
	'Assert$$1.class' \
	'Assert$$AssertPort.class' \
	'Commutator$$WidthDependentParameter.class' \
	'Dictionary$$1.class' \
	'Dictionary$$EmptyScope.class' \
	'Distributor$$WidthDependentParameter.class' \
	'Exec$$_StreamReaderThread.class' \
	'Expression$$1.class' \
	'Expression$$OutputTypeFunction.class' \
	'Expression$$VariableScope.class' \
	'RecordDisassembler$$1.class' \
	'RecordDisassembler$$PortFunction.class' \
	'RecordUpdater$$1.class' \
	'RecordUpdater$$FunctionTerm.class' \
	'RecordUpdater$$MinimalOutputTerm.class' \
	'Scale$$1.class' \
	'Scale$$PortParamFunction.class' \
	'Scale$$PortParameterFunction.class' \
	'SetVariable$$1.class' \
	'TimeDelay$$PendingEvent.class' \
	'UnionDisassembler$$1.class' \
	'UnionDisassembler$$PortFunction.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
