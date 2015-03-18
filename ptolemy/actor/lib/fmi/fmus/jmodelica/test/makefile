# Makefile for the Java classes used to test the functional mockup interface actors for JModelica
#
# @Authors: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2014 The Regents of the University of California.
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
ME =		ptolemy/actor/lib/fmi/fmus/jmodelica/test

# Root of the Ptolemy II directory
ROOT =		../../../../../../..

# JNA_JAR is set by $PTII/configure in $PTII/mk/ptII.mk
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(JNA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	testDefs.tcl

JSRCS =

# Non-graphical Java tests written in Tcl
JSIMPLE_TESTS =

# Graphical Java tests that use Tcl.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) #$(JGRAPHICAL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	alljtests.tcl  junit \
	auto

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS =	$(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

tests:: $(EXTRA_SRCS) jclass test_java test_jsimple

test_java: jclass

all: 
	@echo "Nothing to be done in fmus/jmodelica/test for 'make all'"
install: 
	@echo "Nothing to be done in fmus/jmodelica/test for 'make install'"

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl test_auto
	$(JTCLSH) alljtests.tcl

# Create reference csv files for fmus.
# See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/FMICertification
# fmuCheck command line arguments:
#   -f "Print all variables to the output file. Default is to only print outputs."
#   -s stopTime
#   -h stepSize
# -s and -h values are from FMUSDK-2.0.3/run_all.bat
# fmuCheck has bugs where it does not produce values for 0.8.  See https://trac.fmi-standard.org/ticket/329
FMUCHECK = fmuCheck

FMU20SIM_CS = $(HOME)/src/fmi/fmusdk2/fmu20/bin/fmusim_cs

REF_CSVS =  auto/CoupledClutches_ref.csv
# Build all the *_ref.csv files
refs: $(REF_CSVS)

clean_refs:
	rm -rf $(REF_CSVS)

# https://trac.fmi-standard.org/browser/branches/public/Test_FMUs/FMI_2.0/CoSimulation/darwin64/MapleSim/7.01/CoupledClutches 
# Stop and step are from https://trac.fmi-standard.org/browser/branches/public/Test_FMUs/FMI_2.0/CoSimulation/darwin64/MapleSim/7.01/CoupledClutches/CoupledClutches_cc.sh
auto/CoupledClutches_ref.csv:
	#$(FMU20SIM_CS) auto/CoupledClutches.fmu 1.5 0.01
	#mv result.csv $@
	rm -rf tmp
	mkdir -p tmp/resources
	$(FMUCHECK) -z tmp -f -k cs -o $@ -s 4.0 -h 0.01 auto/CoupledClutches.fmu


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
