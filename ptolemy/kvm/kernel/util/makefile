# Makefile for the Java classes used to implement the Ptolemy kernel
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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

ME = 		ptolemy/kvm/kernel/util

DIRS = 		test

# Root of the Ptolemy II directory
ROOT =		../../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(KVM_CLASSES)$(CLASSPATHSEPARATOR)$(KVM_DIR)/api/classes$(CLASSPATHSEPARATOR)$(ROOT)/ptolemy/kvm

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

JFLAGS =	-bootclasspath "$(CLASSPATH)"

# Used to build jar files
PTPACKAGE = 	util
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	Attribute.java \
	CrossRefList.java \
	KernelException.java \
	IllegalActionException.java \
	InternalErrorException.java \
	InvalidStateException.java \
	Nameable.java \
	NamedList.java \
	NamedObj.java \
	NameDuplicationException.java \
	NoSuchItemException.java \
	PtolemyThread.java \
	Workspace.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'CrossRefList$$CrossRef.class' \
	'CrossRefList$$CrossRefEnumeration.class' \
	'NamedList$$NamedListEnumeration.class' \
	'Workspace$$ReadDepth.class' \
	$(PTCLASSJAR)

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)


depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
