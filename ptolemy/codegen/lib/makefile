# Makefile for Ptolemy II Dataflow CGC actors imported from Ptolemy Classic
#
# @Authors: Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2005 The Regents of the University of California.
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
#                                               PT_COPYRIGHT_VERSION_2
#                                               COPYRIGHTENDKEY

ME =            ptolemy/codegen/lib

# Rule for index.xml uses this
TITLE = 	"Dataflow CGC Actors"

DIRS =		demo test    	

# Root of the Java directory
ROOT =          ../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =        $(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE =     lib
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS =        CGCAbs.java \
CGCAdaptLinQuant.java \
CGCAdd.java \
CGCAddCx.java \
CGCAddFix.java \
CGCAddInt.java \
CGCAudioBase.java \
CGCBitsToInt.java \
CGCBlackHole.java \
CGCBusFork.java \
CGCChop.java \
CGCChopVarOffset.java \
CGCCollect.java \
CGCCommutator.java \
CGCConst.java \
CGCConstCx.java \
CGCConstFix.java \
CGCConstInt.java \
CGCCopy.java \
CGCCos.java \
CGCCxToFix.java \
CGCCxToFloat.java \
CGCCxToRect.java \
CGCDB.java \
CGCDeMux.java \
CGCDeScrambler.java \
CGCDelay.java \
CGCDistributor.java \
CGCDivByInt.java \
CGCDownSample.java \
CGCExp.java \
CGCExpr.java \
CGCFix.java \
CGCFixToCx.java \
CGCFixToFix.java \
CGCFixToFloat.java \
CGCFixToInt.java \
CGCFloatToCx.java \
CGCFloatToFix.java \
CGCFloatToInt.java \
CGCFloor.java \
CGCFork.java \
CGCGain.java \
CGCGainFix.java \
CGCGainInt.java \
CGCIIDGaussian.java \
CGCIIDUniform.java \
CGCImpulse.java \
CGCIntToBits.java \
CGCIntToFix.java \
CGCIntToFloat.java \
CGCIntegrator.java \
CGCLimit.java \
CGCLinQuantIdx.java \
CGCLog.java \
CGCLogic.java \
CGCMaxMin.java \
CGCModulo.java \
CGCModuloInt.java \
CGCMonoIn.java \
CGCMonoOut.java \
CGCMpy.java \
CGCMpyCx.java \
CGCMpyFix.java \
CGCMpyInt.java \
CGCMux.java \
CGCOrderTwoInt.java \
CGCPcwzLinear.java \
CGCPolarToRect.java \
CGCPrinter.java \
CGCQuant.java \
CGCQuantIdx.java \
CGCQuantizer.java \
CGCRamp.java \
CGCRampFix.java \
CGCRampInt.java \
CGCReciprocal.java \
CGCRect.java \
CGCRectFix.java \
CGCRectToCx.java \
CGCRectToPolar.java \
CGCRepeat.java \
CGCReverse.java \
CGCSGIAudio.java \
CGCSGIAudioIn.java \
CGCSGIAudioOut.java \
CGCScrambler.java \
CGCSelect.java \
CGCSgn.java \
CGCSin.java \
CGCSleep.java \
CGCSpread.java \
CGCSqrt.java \
CGCStereoIn.java \
CGCStereoOut.java \
CGCSub.java \
CGCSubCx.java \
CGCSubFix.java \
CGCSubInt.java \
CGCSwitch.java \
CGCTable.java \
CGCTableCx.java \
CGCTableInt.java \
CGCTest.java \
CGCThresh.java \
CGCTrainer.java \
CGCUpSample.java \
CGCWaveForm.java \
CGCXMgraph.java \
CGCXYgraph.java \
CGCXgraph.java \
CGCXscope.java

OTHER_FILES_TO_BE_JARED = 	lib.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

JCLASS = $(JSRCS:%.java=%.class)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =	test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =         index.xml 	demo 
all: jclass index.xml
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
