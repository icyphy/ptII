# Makefile for Ptolemy II Java Advanced Imaging
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2002-2003 The Regents of the University of California.
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

ME =		ptolemy/actor/lib/jai

DIRS = 		test

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	jai
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = AdaptiveMedian.java \
	DoubleMatrixToJAI.java \
	ImageToJAI.java \
	JAIAffineTransform.java \
	JAIBandCombine.java \
	JAIBandSelect.java \
	JAIBorder.java \
	JAIBoxFilter.java \
	JAIBMPWriter.java \
	JAIConstant.java \
	JAIConvolve.java \
	JAICrop.java \
	JAIDataConvert.java \
	JAIDCT.java \
	JAIDFT.java \
	JAIEdgeDetection.java \
	JAIIDCT.java \
	JAIIDFT.java \
	JAIImageReader.java \
	JAIImageToken.java \
	JAIInvert.java \
	JAIJPEGWriter.java \
	JAILog.java \
	JAIMagnitude.java \
	JAIMedianFilter.java \
	JAIPeriodicShift.java \
	JAIPhase.java \
	JAIPolarToComplex.java \
	JAIPNGWriter.java \
	JAIPNMWriter.java \
	JAIRotate.java \
	JAIScale.java \
	JAITIFFWriter.java \
	JAIToDoubleMatrix.java \
	JAITranslate.java \
	JAITranspose.java \
	SaltAndPepper.java

OTHER_FILES_TO_BE_JARED = \
	jai.xml \
	jai-copyright.htm

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
