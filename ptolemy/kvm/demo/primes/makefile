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

# Override setting in ptII.mk
# Boot files to use with kvm 
# If we compile with javac -bootclasspath $(KVM_CLASSES)
# then we can compile with an environment similar to what is on
# the Palm running KVM
#JFLAGS = -bootclasspath $(KVM_CLASSES)

all: jclass Primes.prc PrimesTextField.prc

install: jclass subinstall $(PTCLASSJAR)

Primes.prc: $(JCLASS) $(KVM_DIR)/tools/palm/src/palm/database/MakePalmApp.class
	$(JAVA) \
		-classpath $(KVM_DIR)/tools/palm/src \
		palm.database.MakePalmApp \
		-bootclasspath $(KVM_CLASSES)  \
		-classpath $(ROOT) \
		ptolemy.kvm.demo.primes.Primes

PrimesTextField.prc: $(JCLASS) $(KVM_DIR)/tools/palm/src/palm/database/MakePalmApp.class
	$(JAVA) \
		-classpath $(KVM_DIR)/tools/palm/src \
		palm.database.MakePalmApp \
		-classpath $(ROOT) \
		-bootclasspath $(KVM_CLASSES)  \
		ptolemy.kvm.demo.primes.PrimesTextField

demo: jclass
	CLASSPATH=$(CLASSPATH) $(JAVA) ptolemy.kvm.primes.PrimesText

# Run the kvm
demokvm: jclass
	CLASSPATH=$(CLASSPATH) $(JAVA) -Xbootclasspath:$(KVM_DIR)/api/classes.zip -verbose:class ptolemy.kvm.primes.PrimesText

depend:
	@echo "no dependencies in this directory"

# Get rules for KVM
include $(ROOT)/mk/kvm.mk

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
