# Makefile performance tests
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2009 The Regents of the University of California.
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

# Location of this directory, relative to the Ptolemy II directory
ME =		ptolemy/actor/lib/test/performance

# Root of the Ptolemy II directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	ModularCodeGen.tcl \
	ModularCodeGen2.tcl \
	ModularCodeGenLarge.tcl \
	pubSubAggModel.tcl \
	PubSubAgg.tcl \
	PubSubLazy.tcl \
	Publisher2.tcl \
	PublisherCommon.tcl \
	run.tcl \
	testDefs.tcl

JSRCS =

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) \
	MCGBuildAndRun \
	MCGBuildAndRunLarge \
	MCGLargeBuildAndRun \
	MCGLargeStats \
	buildAndRun \
	pubSubAggModelAll \
	runAll

all: AddSubtractVsExpression

STATSFILE=statsfile

AddSubtractVsExpression:
	make runModel MODEL=AddSubtractPerformance.xml 
	make runModel MODEL=ExpressionPerformance.xml 

# To check the performance of XXX.xml, invoke make runModelMODEL=XXX.xml
runModel:
	@rm -f $(STATSFILE)
	@echo "Run $(MODEL) 5 times"
	@for repeat in 1 2 3 4 5; \
	do \
		"$(JAVA)" -classpath "$(ROOT)" \
		ptolemy.actor.gui.MoMLSimpleApplication $(MODEL) | \
		tee -a $(STATSFILE); \
	done
	@awk 'NR > 1 { sum += $$1} \
		END {print model ": Average time of 2 through " NR " runs: " (sum+0.0)/(NR-1) " ms."}' model=$(MODEL) $(STATSFILE)

MISC_FILES = \
	AddSubtractPerformance.xml \
	ExpressionPerformance.xml \
	plotPerformance \
	runModels \
	stats \


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
