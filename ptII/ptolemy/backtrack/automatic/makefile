# Makefile for Ptolemy backtracking package
#
# @Version: $Id$
# @Author: Christopher Brooks
#
# @Copyright (c) 2005-2009 The Regents of the University of California.
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
#
#
##########################################################################

###############################################################################
# WARNING: This makefile requires GNU make, it will NOT work with Solaris make#
###############################################################################

ME =		ptolemy/backtrack/automatic

DIRS =		

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)$(PTBACKTRACK_ECLIPSE_JARS)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	automatic
PTCLASSJAR =
# We use OTHER_JARS instead of PTCLASSJAR because the directories under
# this one do not follow our coding standards.
OTHER_JARS = $(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
#PTCLASSALLJARS = \
#		ptolemy/ptolemy.jar
#PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = 

OTHER_FILES_TO_BE_JARED = 

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES = \
	ptolemy \
	source.lst

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(OTHER_JARS)

# .java files that are created.  We need to list these separately so
# that "make jclass" works.  Note that if source.lst is updated, then
# this list should be updated as well.
DERIVED_JSRCS = \
	ptolemy/actor/lib/Accumulator.java \
	ptolemy/actor/lib/ArrayPeakSearch.java \
	ptolemy/actor/lib/Average.java \
	ptolemy/actor/lib/BooleanSelect.java \
	ptolemy/actor/lib/BooleanSwitch.java \
	ptolemy/actor/lib/Commutator.java \
	ptolemy/actor/lib/ComputeHistogram.java \
	ptolemy/actor/lib/Counter.java \
	ptolemy/actor/lib/DB.java \
	ptolemy/actor/lib/Differential.java \
	ptolemy/actor/lib/Distributor.java \
	ptolemy/actor/lib/DoubleReader.java \
	ptolemy/actor/lib/Gaussian.java \
	ptolemy/actor/lib/GradientAdaptiveLattice.java \
	ptolemy/actor/lib/IIR.java \
	ptolemy/actor/lib/Interpolator.java \
	ptolemy/actor/lib/LinearDifferenceEquationSystem.java \
	ptolemy/actor/lib/Multiplexor.java \
	ptolemy/actor/lib/OrderedMerge.java \
	ptolemy/actor/lib/PhaseUnwrap.java \
	ptolemy/actor/lib/PoissonClock.java \
	ptolemy/actor/lib/Pulse.java \
	ptolemy/actor/lib/Ramp.java \
	ptolemy/actor/lib/RandomSource.java \
	ptolemy/actor/lib/Recorder.java \
	ptolemy/actor/lib/RecursiveLattice.java \
	ptolemy/actor/lib/Select.java \
	ptolemy/actor/lib/Sequence.java \
	ptolemy/actor/lib/Sequencer.java \
	ptolemy/actor/lib/SequentialClock.java \
	ptolemy/actor/lib/Switch.java \
	ptolemy/domains/sdf/lib/Autocorrelation.java \
	ptolemy/domains/sdf/lib/Chop.java \
	ptolemy/domains/sdf/lib/DelayLine.java \
	ptolemy/domains/sdf/lib/FFT.java \
	ptolemy/domains/sdf/lib/IFFT.java \
	ptolemy/math/Interpolation.java 
	
JCLASS = $(JSRCS:%.java=%.class) $(DERIVED_JSRCS:%.java=%.class)

# Backtrack.tcl runs "make backtrack" so that we can run the
# tests from inside Eclipse
all: transform build

# FIXME: Running make install reruns the transformer and recompiles
# The dependencies should be set up so that the transform step is run
# only if necessary
install: all jars $(PTPACKAGE).jar

# 'make fast' looks for EXTRA_TARGETS.
#  See $PTII/mk/ptcommon.mk for the definition of 'make fast'
EXTRA_TARGETS = transform build

transform:
	if [ "$(PTBACKTRACK_ECLIPSE_DIR)" = "eclipse" ]; then \
		echo "Transform the source files in source.lst."; \
		echo "Remove prexisting .java files in ./ptolemy/"; \
		find ptolemy . -name "*.java" -print -exec rm {} \; ; \
		rm -f ./ptolemy/configs/output.xml; \
		"$(JAVA)" \
			-classpath "$(CLASSPATH)$(CLASSPATHSEPARATOR)$(PTBACKTRACK_JARS)$(AUXCLASSPATH)" \
			ptolemy.backtrack.eclipse.ast.Transformer \
			-classpath "$(AUXCLASSPATH)" \
			-prefix ptolemy.backtrack.automatic \
			-output "$(ROOT)" \
			-config ptolemy/configs/output.xml \
			@source.lst; \
	else \
		echo "Eclipse not found by configure, not transforming files list in source.lst."; \
		if [ ! -d ptolemy ]; then \
			mkdir ptolemy; \
		fi; \
	fi

# Don't use $(MAKE) -C here, -C is a gnu make extension
build: ptolemy_makefile
	@if [ "$(PTBACKTRACK_ECLIPSE_DIR)" = "eclipse" ]; then \
		echo "Running $(MAKE) in ptolemy/"; \
		(cd ptolemy; $(MAKE)); \
	else \
		if [ -f ptolemy/makefile ]; then \
		    echo "Eclipse is not present, but ptolemy/makefile is, so running $(MAKE) in ptolemy/"; \
		    (cd ptolemy; $(MAKE)); \
                else \
		    echo "Eclipse not found by configure, ptolemy/makefile not found, not building in ptolemy/"; \
                fi \
	fi

ptolemy_makefile:
	@echo "Generating ptolemy/makefile..."
	@echo "ME = $(ME)/ptolemy" > ptolemy/makefile
	@echo "DIRS =" >> ptolemy/makefile
	@echo "ROOT = $(ROOT)/.." >> ptolemy/makefile
	@echo "CLASSPATH = \$$(ROOT)" >> ptolemy/makefile
	@echo "CONFIG = \$$(ROOT)/mk/ptII.mk" >> ptolemy/makefile
	@echo "include \$$(CONFIG)" >> ptolemy/makefile
	@echo "PTPACKAGE = ptolemy" >> ptolemy/makefile
	@echo "PTDIST = \$$(PTPACKAGE)\$$(PTVERSION)" >> ptolemy/makefile
	@echo "PTCLASSJAR = \$$(PTPACKAGE).jar" >> ptolemy/makefile
	@echo "JSRCS = \\" >> ptolemy/makefile
	@for filename in `cd ptolemy && find . -name "*.java"| sort`; do \
	  echo "    $$filename \\" >> ptolemy/makefile; \
	done
	@echo >> ptolemy/makefile
	@echo "OTHER_FILES_TO_BE_JARED = $(shell find . -name \"*.class\")" >> ptolemy/makefile
	@echo "EXTRA_SRCS = \$$(JSRCS) \$$(OTHER_FILES_TO_BE_JARED)" >> ptolemy/makefile
	@echo "MISC_FILES = \$$(DIRS)" >> ptolemy/makefile
	@echo "OPTIONAL_FILES =" >> ptolemy/makefile
	@echo "JCLASS = \$$(JSRCS:%.java=%.class)" >> ptolemy/makefile
	@echo "all: jclass" >> ptolemy/makefile
	@echo "install: jclass \$$(PTCLASSJAR)" >> ptolemy/makefile
	@echo "include \$$(ROOT)/mk/ptcommon.mk" >> ptolemy/makefile

automatic.jar: $(JSRCS) $(JCLASS) $(OTHER_FILES_TO_BE_JARED) all
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	# Copy any class files from this directory
	mkdir -p $(PTJAR_TMPDIR)/$(ME)
	"$(JAR)" -c `find . -name "*.xml" -o -name "*.class"` | (cd $(PTJAR_TMPDIR)/$(ME); "$(JAR)" -x)
	-cp $(OTHER_FILES_TO_BE_JARED) $(PTJAR_TMPDIR)/$(ME)
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" $(JAR_FLAGS) -cvf tmp.jar .)
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	$(JAR_INDEX)
	rm -rf $(PTJAR_TMPDIR)

KRUFT = automatic.jar */*/*/*.class */*/*/*/*.class

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

