# Makefile for Java Ptolemy PalmOS Hello demonstration
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

ME =		ptolemy/kvm/demo/hello

# Root of the Java directory
ROOT =		../../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(KVM_CLASSES)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

DIRS =

# Used to build jar files
#PTPACKAGE = 	util
#PTDIST =	$(PTPACKAGE)$(PTVERSION)
#PTCLASSJAR = 	$(PTPACKAGE).jar


# Keep this list alphabetized.
JSRCS = \
	Hello.java


EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \

JCLASS = $(JSRCS:%.java=%.class)

BINNAME =	Hello

all: jclass $(BINNAME).prc

install: jclass subinstall $(PTCLASSJAR)
	cp $(BINNAME).prc $(PALM_ADD_ON_DIR)

Hello.prc: Hello.class \
		$(KVM_DIR)/tools/palm/src/palm/database/MakePalmApp.class
	$(JAVA) \
	 -classpath $(KVM_DIR)/tools/palm/src \
	 palm.database.MakePalmApp \
	-v -v \
	-version "1.0" \
	-bootclasspath $(KVM_CLASSES)  \
	-classpath "$(CLASSPATH)" \
	ptolemy.kvm.demo.hello.Hello


depend:
	@echo "no dependencies in this directory"

# We could use the rules in kvm.mk to build here, but instead we
# keep it really simple and just do it.
# Get rules for KVM
include $(ROOT)/mk/kvm.mk

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
