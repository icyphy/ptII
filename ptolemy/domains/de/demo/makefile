# Makefile for Ptolemy II Continuous Time domain demos
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: @(#)makefile	1.8 08/11/98
#
# @Copyright (c) 1998 The Regents of the University of California.
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

# Note that the Java classes contained in this directory were written
# by Sun.  These classes came from tcljava, see license.terms for more info.

# Root of the Java directory
ROOT =		../../../..

ME =		ptolemy/domains/ct/demo

# Get configuration info
CONFIG =	$(ROOT)/mk/tycho.mk
include $(CONFIG)

CLASSPATH = ../../../..

TCL_SRCS =

JSRCS = \

# Non-graphical Java demos written in Tcl
# Since there are no demos, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a demo, be sure to add
# $(JSIMPLE_TESTS) to EXTRA_SRCS
JSIMPLE_TESTS = \
	dummy.tcl

# Graphical Java demos that use Tcl.
JGRAPHICAL_TESTS = \
	ex.tcl \
	AdjustRamp.tcl \
	AdjustSecOrder.tcl \
	Coyote1.tcl \
	Exponential.tcl \
	SecOrder.tcl \
	Ramp.tcl 

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) $(JGRAPHICAL_TESTS) #$(JSIMPLE_TESTS) 

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	#doc

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =	#codeDoc

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass
	@echo "To run all the demos, run 'make demo'"

install: all

demo: demo_jgraphical

# These demos do not require a graphics terminal, but do use TclBlend and Java
demo_jgraphical: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl
	CLASSPATH=$(CLASSPATH)$(AUXCLASSPATH) $(JTCLSH) alljtests.tcl

# This is a graphical test
Exponential: $(EXTRA_SRCS) jclass Exponential.tcl
	CLASSPATH=$(CLASSPATH)$(AUXCLASSPATH) $(JTCLSH) Exponential.tcl

docs: javadocs

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/tycommon.mk



