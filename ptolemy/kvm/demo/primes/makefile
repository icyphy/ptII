# Makefile for Java Ptolemy PalmOS Primes demonstration
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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

ME =		ptolemy/kvm/demo/primes

# Root of the Java directory
ROOT =		../../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(KVM_CLASSES)$(CLASSPATHSEPARATOR)$(KVM_DIR)/api/classes

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

DIRS = 		test

# Used to build jar files
#PTPACKAGE = 	util
#PTDIST =	$(PTPACKAGE)$(PTVERSION)
#PTCLASSJAR = 	$(PTPACKAGE).jar


# Keep this list alphabetized.
JSRCS = \
	Channel.java \
	Filter.java \
	InputChannel.java \
	OutputChannel.java \
	Primes.java \
	PrimesText.java \
	PrimesTextField.java \
	Ramp.java \
	Sift.java


EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \

JCLASS = $(JSRCS:%.java=%.class)

all: jclass Primes.prc PrimesTextField.prc
	@echo "To run the demos, run 'make demo'"

install: jclass subinstall $(PTCLASSJAR)

demo: jclass
	$(MAKE) compile_kvm preverify build_prc

demo_PrimesText: jclass
	CLASSPATH="$(CLASSPATH)" $(JAVA) ptolemy.kvm.demo.primes.PrimesText


depend:
	@echo "no dependencies in this directory"

# Makefile variables necessary for KVM

# Classpath to the source system we are generating code for.
SOURCE_SYSTEM_CLASS =	 ptolemy.kvm.demo.primes.Primes

# Number of iterations.
ITERATIONS =	10

# Output package name for generated code, which also
# determines the directory relative to PTII where the
# code will appear.
OUTPKG =	ptolemy.kvm.demo.primes

# Location of the OUTPKG directory.
OUTPKG_DIR =	$(ME)

# The relative path from OUTPKG to $PTII.
OUTPKG_ROOT =	$(ROOT)

# The class that contains the main() method
OUTPKG_MAIN_CLASS =	Primes

# Get rules for KVM
include $(ROOT)/mk/kvm.mk

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
