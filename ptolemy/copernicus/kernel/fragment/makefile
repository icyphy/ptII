# Makefile for Ptolemy II code generation kernle
#
# @Authors: Steve Neuendorffer, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2001-2003 The Regents of the University of California.
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

ME =		ptolemy/copernicus/kernel/fragment

# Root of the Java directory
ROOT =		../../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	fragment
PTCLASSJAR = 	$(PTPACKAGE).jar

DIRS = test

# Keep this list alphabetized.
JSRCS = 

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS =    JavaFragmentParser.jj $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

DERIVED_JSRCS = JavaFragmentParser.java

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc 

JCLASS = $(JSRCS:%.java=%.class) \
	JavaFragmentParser.class

all: jclass

install: all

JavaFragmentParser.java: JavaFragmentParser.jj
	@echo "This command runs JavaCC, which can be downloaded from"
	@echo "https://javacc.dev.java.net/"
	@echo "See \$$PTII/doc/install.htm for details."
	@echo "You only need install JavaCC if you modify JavaFragmentParser.jjt"
	@echo "JavaFragmentParser.java is derived from JavaFragmentParser.jj, which is in turn"
	@echo "derived from JavaFragmentParser.jjt"
	@echo "If you don't have JavaCC, you can workaround this with"
	@echo "   touch JavaFragmentParser.jj JavaFragmentParser.java"
	CLASSPATH="$(CLASSPATH)" $(JAVACC) JavaFragmentParser.jj

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

