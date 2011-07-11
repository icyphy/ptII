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
    ./actor/lib/Accumulator.java \
    ./actor/lib/ArrayPeakSearch.java \
    ./actor/lib/Average.java \
    ./actor/lib/BooleanSelect.java \
    ./actor/lib/BooleanSwitch.java \
    ./actor/lib/Commutator.java \
    ./actor/lib/ComputeHistogram.java \
    ./actor/lib/Counter.java \
    ./actor/lib/DB.java \
    ./actor/lib/Differential.java \
    ./actor/lib/Distributor.java \
    ./actor/lib/DoubleReader.java \
    ./actor/lib/Gaussian.java \
    ./actor/lib/GradientAdaptiveLattice.java \
    ./actor/lib/IIR.java \
    ./actor/lib/Interpolator.java \
    ./actor/lib/LinearDifferenceEquationSystem.java \
    ./actor/lib/Multiplexor.java \
    ./actor/lib/OrderedMerge.java \
    ./actor/lib/PhaseUnwrap.java \
    ./actor/lib/PoissonClock.java \
    ./actor/lib/Pulse.java \
    ./actor/lib/Ramp.java \
    ./actor/lib/RandomSource.java \
    ./actor/lib/Recorder.java \
    ./actor/lib/RecursiveLattice.java \
    ./actor/lib/Select.java \
    ./actor/lib/Sequence.java \
    ./actor/lib/Sequencer.java \
    ./actor/lib/SequentialClock.java \
    ./actor/lib/Switch.java \
    ./domains/sdf/lib/Autocorrelation.java \
    ./domains/sdf/lib/Chop.java \
    ./domains/sdf/lib/DelayLine.java \
    ./domains/sdf/lib/FFT.java \
    ./domains/sdf/lib/IFFT.java \
    ./math/Interpolation.java \

OTHER_FILES_TO_BE_JARED = 
EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)
MISC_FILES = $(DIRS)
OPTIONAL_FILES =
JCLASS = $(JSRCS:%.java=%.class)
all: jclass
install: jclass $(PTCLASSJAR)
include $(ROOT)/mk/ptcommon.mk
