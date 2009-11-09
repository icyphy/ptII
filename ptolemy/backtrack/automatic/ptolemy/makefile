ME = ptolemy/backtrack/automatic/ptolemy
DIRS =
ROOT = ../../../..
CLASSPATH = $(ROOT)
CONFIG = $(ROOT)/mk/ptII.mk
include $(CONFIG)
PTPACKAGE = ptolemy
PTDIST = $(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = $(PTPACKAGE).jar
JSRCS = \
    ./actor/lib/GradientAdaptiveLattice.java \
    ./actor/lib/OrderedMerge.java \
    ./actor/lib/Accumulator.java \
    ./actor/lib/Interpolator.java \
    ./actor/lib/SequentialClock.java \
    ./actor/lib/Recorder.java \
    ./actor/lib/BooleanSwitch.java \
    ./actor/lib/Multiplexor.java \
    ./actor/lib/Sequence.java \
    ./actor/lib/Select.java \
    ./actor/lib/DoubleReader.java \
    ./actor/lib/RecursiveLattice.java \
    ./actor/lib/Pulse.java \
    ./actor/lib/Commutator.java \
    ./actor/lib/Sequencer.java \
    ./actor/lib/LinearDifferenceEquationSystem.java \
    ./actor/lib/ComputeHistogram.java \
    ./actor/lib/Switch.java \
    ./actor/lib/Gaussian.java \
    ./actor/lib/ArrayPeakSearch.java \
    ./actor/lib/IIR.java \
    ./actor/lib/PhaseUnwrap.java \
    ./actor/lib/Distributor.java \
    ./actor/lib/Ramp.java \
    ./actor/lib/Differential.java \
    ./actor/lib/Counter.java \
    ./actor/lib/Average.java \
    ./actor/lib/BooleanSelect.java \
    ./actor/lib/DB.java \
    ./actor/lib/PoissonClock.java \
    ./actor/lib/RandomSource.java \
    ./math/Interpolation.java \
    ./domains/sdf/lib/Autocorrelation.java \
    ./domains/sdf/lib/FFT.java \
    ./domains/sdf/lib/Chop.java \
    ./domains/sdf/lib/DelayLine.java \
    ./domains/sdf/lib/IFFT.java \

OTHER_FILES_TO_BE_JARED = 
EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)
MISC_FILES = $(DIRS)
OPTIONAL_FILES =
JCLASS = $(JSRCS:%.java=%.class)
all: jclass
install: jclass $(PTCLASSJAR)
include $(ROOT)/mk/ptcommon.mk
