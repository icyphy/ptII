# Makefile for Ptolemy II Simple Ramp demo
#
# @Authors: Christopher Hylands
#
# @Version: : ptmkmodel,v 1.7 1999/07/16 01:17:49 cxh Exp $
#
# @Copyright (c) 2000 The Regents of the University of California.
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

ME =		ptolemy/kvm/demo/ramp

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ramp
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	RampSystem.java

EXTRA_SRCS = \
	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
#OPTIONAL_FILES = 
#	

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass $(PTCLASSJAR)
	@echo "To run the demos, run 'make demo'"

install: all

# demo_interpreted and codegen rules are defined in $PTII/mk/kvm.mk
demo:
	$(MAKE) demo_interpreted
	$(MAKE) codegen

depend:
	@echo "no dependencies in this directory"

# Makefile variables necessary for KVM

# Classpath to the source system we are generating code for.
SOURCE_SYSTEM_CLASS =	 ptolemy.kvm.demo.ramp.RampSystem

# Number of iterations.
ITERATIONS =	10

# Output package name for generated code, which also
# determines the directory relative to PTII where the
# code will appear.
OUTPKG =	cg.ramp

# Location of the OUTPKG directory.
OUTPKG_DIR =	cg/ramp

# The relative path from OUTPKG to $PTII.
OUTPKG_ROOT =	../..

# Get rules for KVM
include $(ROOT)/mk/kvm.mk

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

