# Makefile for Windows specific codegen libraries
#
# @Version: $Id$
# @Author: Christopher Brooks
#
# This directory contains the matlab engine interface
#
# @Copyright (c) 2006-2008 The Regents of the University of California 
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

ME =		ptolemy/codegen/c/lib/win

DIRS =

# Root of the Java directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	win
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS =

OTHER_FILES_TO_BE_JARED = \
	libjvm.dll.a

EXTRA_SRCS = \
	$(JSRCS) \
	$(OTHER_FILES_TO_BE_JARED) \
	README.txt

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES = \
	jvm.def

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =


JCLASS = $(JSRCS:%.java=%.class)

# 'make fast' also build PTLIB, so be sure that PTLIB is set to
# the name of the library
#PTLIB=$(PTJNI_SHAREDLIBRARY_PREFIX)ptmatlab.$(PTJNI_SHAREDLIBRARY_SUFFIX)
PTLIB=libjvm.dll.a

# make clean removes $(KRUFT)
KRUFT = 

all: jclass

jclass: $(PTLIB)


# MAJOR_OS_NAME is set by configure to the major name of the OS
# Under all versions of Windows, MAJOR_OS_NAME = Windows
#PTPACKAGE_NATIVE_JAR =	$(PTPACKAGE)$(MAJOR_OS_NAME).jar
install: jclass jars $(PTPACKAGE).jar #$(PTPACKAGE_NATIVE_JAR)

#--- Specialized part of the makefile: ---
libjvm.dll.a: jvm.def
	@echo "This is only necessary under Windows, it will fail elsewhere"
	-dlltool --input-def jvm.def --kill-at --dllname jvm.dll \
		--output-lib libjvm.dll.a

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk


