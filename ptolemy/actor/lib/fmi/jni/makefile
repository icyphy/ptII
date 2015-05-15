# Makefile for Functional Mock-up Interface actor using JNI instead of JNA
#
# @Authors: Christopher Brooks, based on a file by Thomas M. Parks
#
# @Version: $Id: makefile 72323 2015-05-14 01:02:27Z cxh $
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

ME =		ptolemy/actor/lib/fmi/jni

DIRS =

# Root of the Java directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(JNA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	jni
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS =

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't set MISC_FILES to $(DIRS) or else 'make sources' will try to invoke the demos.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	doc

JCLASS = $(JSRCS:%.java=%.class)

FMUIMPORTJNI=FMUImportJNI

PTLIB = $(PTJNI_SHAREDLIBRARY_PREFIX)$(FMUIMPORTJNI).$(PTJNI_SHAREDLIBRARY_SUFFIX)

JNISRCS= jniToFmu.c

KRUFT=$(PTLIB)


all: jclass $(PTLIB)
install: jclass jars $(PTLIB)
	cp $(PTLIB) $(ROOT)/lib

jclass: $(PTLIB)

jni: $(PTLIB)

#jniTofmu.c has the definition of runNativeFMU in it, so we don't need to run this.
#$(JNISRCS): $(FMUIMPORTJNI).h
#	javah -classpath $(CLASSPATH) -o $(FMUIMPORTJNI).h ptolemy.actor.lib.fmi.$(FMUIMPORTJNI)

$(PTLIB): $(JNISRCS)
	$(PTCC) $(PTJNI_NO_CYGWIN) \
	 	-I../fmus/template/fmiv20/ \
		"-I$(PTJAVA_HOME)/../include" \
		"-I$(PTJAVA_HOME)/../include/$(PTJNI_ARCHITECTURE)" \
		$(PTJNI_SHAREDLIBRARY_CFLAG) $(PTJNI_SHAREDLIBRARY_LDFLAG) \
		-shared -o $@ \
		-Wall -O3 \
		$(JNISRCS)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk



