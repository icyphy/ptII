# Makefile for JNI Launcher
#
# @Version: $Id$
# @Author: Christopher Brooks
#

# @Copyright (c) 2006-2009 The Regents of the University of California
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA  OR RESEARCH IN MOTION
# LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL,
# OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND
# ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA OR
# RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
# SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA  AND RESEARCH IN MOTION LIMITED
# HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
##########################################################################

ME =		ptolemy/jni/launcher

DIRS =

# Root of the Java directory
ROOT =		../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	launcher
PTCLASSJAR =

# Keep this list alphabetized.
JSRCS = \
	Hello.java

CPPSRCS = \
	launcher.cpp \
	synchronizeWindowsEnvironment.cpp

HEADERS = \
	DirectoryIterator.h \
	join.h \
	synchronizeWindowsEnvironment.h \
	toString.h \
	unix_exception.h

OTHER_FILES_TO_BE_JARED = \
	launcher-copyright.htm

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED) $(CPPSRCS) $(HEADERS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =


# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	doc

JCLASS = $(JSRCS:%.java=%.class)

# 'make fast' also build PTLIB, so be sure that PTLIB is set to
# the name of the library
#PTLIB=$(PTJNI_SHAREDLIBRARY_PREFIX)ptmatlab.$(PTJNI_SHAREDLIBRARY_SUFFIX)

# make clean removes $(KRUFT)
KRUFT = launcher.exe

all: launcher
	@echo "To see a demonstration, run \"make demo\""

jclass: launcher

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

#--- Specialized part of the makefile: ---
# make fast runs "make $(PTLIB)
PTLIB = 	launcher

# Include both client and server dirs for Linux 64bit
demo: launcher jclass
	LD_LIBRARY_PATH="$(PTJAVA_HOME)/lib/$(PTJNI_LIB_ARCHITECTURE):$(PTJAVA_HOME)/lib/$(PTJNI_LIB_ARCHITECTURE)/client:$(PTJAVA_HOME)/lib/$(PTJNI_LIB_ARCHITECTURE)/server:/System/Library/Frameworks/JavaVM.framework/Libraries" \
		./launcher -Djava.class.path=$(ROOT) jni/launcher/Hello

# $(PTMATLAB_cC) is set to cl or gcc by configure in $PTII/mk/ptII.mk
#
# To test finding jvm.dll by using PTJAVA_HOME, compile
# with make TESTFLAGS=-DTESTING_PTJAVA_HOME demo
#
launcher: $(CPPSRCS) $(HEADERS)
	g++ -g -I. \
		"-I$(PTJNI_INCLUDE)" \
		"-I$(PTJAVA_HOME)/../include/$(PTJNI_ARCHITECTURE)" \
		-DPTJAVA_HOME="\"$(PTJAVA_HOME)\"" \
		$(TESTFLAGS) \
		-o launcher \
		$(CPPSRCS) $(PTJNI_DL_LIBRARY)

launcher_vergil: launcher
	LD_LIBRARY_PATH="$(PTJAVA_HOME)/lib/$(PTJNI_LIB_ARCHITECTURE):$(PTJAVA_HOME)/lib/$(PTJNI_LIB_ARCHITECTURE)/client" \
		./launcher -Djava.class.path=$(PTII) ptolemy/vergil/VergilApplication	 -jni
