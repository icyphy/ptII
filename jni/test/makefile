# Makefile for JNI Ptolemy interface
#
# @Version: $Id$
# @Author: Christopher Hylands
#
# @Copyright (c) 2003 The Regents of the University of California
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

ME =		jni/test

DIRS =

# Root of the Java directory
ROOT =		../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)$(CLASSPATHSEPARATOR).

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	meaningOfLife.tcl

# Keep this list alphabetized.
JSRCS =

# Non-graphical Java tests written in Tcl
JSIMPLE_TESTS = \
	meaningOfLife.tcl

# Graphical Java tests that use Tcl.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) \
	alljtests.tcl \
	meaningOfLife.c \
	meaningOfLife.h \
	meaningOfLife.xml \
	testDeux.c \
	testDeux.h \
	testDeux.xml \
	testQuatre.xml \
	testTrois.c \
	testTrois.h \
	testTrois.xml 


# These files don't exist because testQuatre uses testDeux and testTrois
#	testQuatre.c
#	testQuatre.h

EXTRA_SRCS =	testDefs.tcl $(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) #$(JGRAPHICAL_TESTS)

JCLASS = $(JSRCS:%.java=%.class)

KRUFT = *.dll

tests:: $(EXTRA_SRCS) jclass test_java test_jsimple

test_java: jclass

all: 
	$(MAKE) SHAREDBASE=meaningOfLife shared
	$(MAKE) SHAREDBASE=testDeux shared
	$(MAKE) SHAREDBASE=testTrois shared

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl


# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl
	@echo "Put . in the path so we can find the shared libraries"
	@echo "Put . and /usr/local in LD_LIBRARY_PATH"
	PATH="`pwd`:$(PATH)"; export PATH; \
	LD_LIBRARY_PATH="`pwd`:/usr/local/lib:$(LD_LIBRARY_PATH)"; export LD_LIBRARY_PATH; \
	$(JTCLSH) alljtests.tcl

#	PATH="`pwd`:`pwd`/jni/meaningOfLife:`pwd`/jni/testDeux:`pwd`/jni/testTrois:`pwd`jni/jnimeaningOfLifeImeaningOfLife:$(PATH)"; export PATH; \
#	LD_LIBRARY_PATH="`pwd`:`pwd`/jni/meaningOfLife:`pwd`/jni/testDeux:`pwd`/jni/testTrois:`pwd`jni/jnimeaningOfLifeImeaningOfLife:/usr/local/lib:$(LD_LIBRARY_PATH)"; export LD_LIBRARY_PATH; \
#


# PTCC is set in $PTII/mk/ptII.mk
# See also ptolemy/matlab/makefile
SHARED_LIBRARY = $(PTJNI_SHAREDLIBRARY_PREFIX)$(SHAREDBASE).$(PTJNI_SHAREDLIBRARY_SUFFIX)

shared: $(SHARED_LIBRARY)
$(SHARED_LIBRARY): $(SHAREDBASE).c $(SHAREDBASE).h makefile
	$(PTCC) -shared $(PTJNI_SHAREDLIBRARY_LDFLAG) \
		-o $@ \
		$(SHAREDBASE).c

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
