# Makefile for the Java classes used to implement the Ptolemy math
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

ME =		ptolemy/math

DIRS = 		test

# Root of the Java directory
ROOT =		../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	math
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS =	Complex.java \
	ComplexArrayMath.java \
        ComplexBinaryOperation.java \
        ComplexMatrixMath.java \
        ComplexUnaryOperation.java \
	DoubleArrayMath.java \
	DoubleArrayStat.java \
	DoubleBinaryOperation.java \
	DoubleMatrixMath.java \
	DoubleUnaryOperation.java \
	ExtendedMath.java \
	FixPoint.java \
	FixPointQuantization.java \
	FloatArrayMath.java \
	FloatBinaryOperation.java \
	FloatMatrixMath.java \
	FloatUnaryOperation.java \
	Fraction.java \
	IntegerArrayMath.java \
	IntegerBinaryOperation.java \
	IntegerMatrixMath.java \
	IntegerUnaryOperation.java \
	Interpolation.java \
	LongArrayMath.java \
	LongBinaryOperation.java \
	LongMatrixMath.java \
	LongUnaryOperation.java \
	Overflow.java \
	Precision.java \
	Quantization.java \
	Quantizer.java \
	Rounding.java \
	SignalProcessing.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	$(PTCLASSJAR) \
	'ArrayStringFormat$$ArrayStringFormatBase.class' \
	'ArrayStringFormat$$ExprArrayStringFormat.class' \
	'ArrayStringFormat$$JavaArrayStringFormat.class' \
	'FixPoint$$1.class' \
	'FixPoint$$Error.class' \
	'FixPoint$$FixValue.class' \
	'Overflow$$1.class' \
	'Overflow$$General.class' \
	'Overflow$$Grow.class' \
	'Overflow$$Modulo.class' \
	'Overflow$$Saturate.class' \
	'Overflow$$ToZero.class' \
	'Overflow$$Trap.class' \
	'Overflow$$Unknown.class' \
	'Rounding$$1.class' \
	'Rounding$$General.class' \
	'Rounding$$RoundCeiling.class' \
	'Rounding$$RoundDown.class' \
	'Rounding$$RoundFloor.class' \
	'Rounding$$RoundHalfCeiling.class' \
	'Rounding$$RoundHalfDown.class' \
	'Rounding$$RoundHalfEven.class' \
	'Rounding$$RoundHalfFloor.class' \
	'Rounding$$RoundHalfUp.class' \
	'Rounding$$RoundUnnecessary.class' \
	'Rounding$$RoundUp.class' \
	'Rounding$$Unknown.class' \
	'SignalProcessing$$GaussianSampleGenerator.class' \
	'SignalProcessing$$PolynomialSampleGenerator.class' \
	'SignalProcessing$$RaisedCosineSampleGenerator.class' \
	'SignalProcessing$$SawtoothSampleGenerator.class' \
	'SignalProcessing$$SincSampleGenerator.class' \
	'SignalProcessing$$SinusoidSampleGenerator.class' \
	'SignalProcessing$$SqrtRaisedCosineSampleGenerator.class'


JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass $(PTCLASSJAR)


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
