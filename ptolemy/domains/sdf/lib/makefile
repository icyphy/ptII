# Makefile for SDF domain actors library
#
# @Authors: Christopher Hylands
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

ME =		ptolemy/domains/sdf/lib

# Rule for index.xml uses this
TITLE = 	"Synchronous dataflow actors"

DIRS = 		vq test

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	lib
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		vq/vq.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	ArrayToSequence.java \
	Autocorrelation.java \
	BitsToInt.java \
	Chop.java \
	CountTrues.java \
	DelayLine.java \
        DotProduct.java \
	DoubleToMatrix.java \
	DownSample.java \
	FIR.java \
	FFT.java \
	IFFT.java \
	IntToBits.java \
	LineCoder.java \
	LMSAdaptive.java \
	MatrixToDouble.java \
	MatrixToSequence.java \
	RaisedCosine.java \
	Repeat.java \
	SDFConverter.java \
	SDFTransformer.java \
	SampleDelay.java \
	SequenceToArray.java \
	SequenceToMatrix.java \
	UpSample.java \
	VariableFIR.java \
	VariableLattice.java \
	VariableRecursiveLattice.java

OTHER_FILES_TO_BE_JARED = \
	MaximumEntropySpectrum.xml \
	Periodogram.xml \
	SmoothedPeriodogram.xml \
	SmoothedSpectrum.xml \
	Spectrum.xml \
	SpectrumPlot.xml \
	sdfentities.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = 	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	index.xml \
	'Autocorrelation$$1.class' \
	'Autocorrelation$$FunctionTerm.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass index.xml
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk







