# Makefile for Java Ptolemy matlab interface
# Based on standard Ptolemy makefiles
#
# @Version: $Id$
# @Author: Zoltan Kemenczy
#
# This directory contains the matlab engine interface
#
# @Copyright (c) 1997-2003 The Regents of the University of California and
# Research in Motion Limited.
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

ME =		ptolemy/matlab

DIRS =		demo test

# Root of the Java directory
ROOT =		../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	matlab
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	Engine.java \
	Expression.java

OTHER_FILES_TO_BE_JARED = \
	copyright.htm \
	matlab.xml \
	matlab.htm

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =	test \
	ptmatlab.cc

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	doc

JCLASS = $(JSRCS:%.java=%.class)

PTMATLAB_LIB=$(PTJNI_SHAREDLIBRARY_PREFIX)ptmatlab.$(PTJNI_SHAREDLIBRARY_SUFFIX)

all: jclass

jclass: $(PTMATLAB_LIB)


# MAJOR_OS_NAME is set by configure to the major name of the OS
# Under all versions of Windows, MAJOR_OS_NAME = Windows
PTPACKAGE_NATIVE_JAR =	$(PTPACKAGE)$(MAJOR_OS_NAME).jar
install: jclass jars $(PTPACKAGE).jar $(PTPACKAGE_NATIVE_JAR)
	@echo "'$(PTPACKAGE).jar' requires gcc, so we install it in \$$PTII/lib"
	@echo "for hosts that might not have gcc installed and for Web Start."
	cp $(PTPACKAGE).jar $(PTPACKAGE_NATIVE_JAR) $(ROOT)/lib

clean: ptmatlab_clean

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

#--- Specialized part of the makefile: ---

# $(PTCC) is set to gcc by configure in $PTII/mk/ptII.mk
# -fno-exceptions  - see note at bottom

$(PTMATLAB_LIB): ptmatlab.cc Engine.class
	CLASSPATH="$(CLASSPATH)" javah -jni -o ptmatlab.h ptolemy.matlab.Engine
	$(PTCC) -shared $(PTJNI_NO_CYGWIN) \
		"-I$(MATLAB_DIR)/extern/include" \
		"-I$(PTJAVA_DIR)/include" \
		"-I$(PTJAVA_DIR)/include/$(PTJNI_ARCHITECTURE)" \
		$< \
		-fno-exceptions \
		-o HelloWorldImp.dll \
		-L$(MATLAB_LIBDIR) $(PTJNI_SHAREDLIBRARY_LDFLAG) -lc -leng -lmx

# Jar file for Web Start that contains the dll
$(PTPACKAGE_NATIVE_JAR): $(PTMATLAB_LIB)
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	-cp $< $(PTJAR_TMPDIR)
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" -cvf tmp.jar .)
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	rm -rf $(PTJAR_TMPDIR)


ptmatlab_clean:
	rm -f ptmatlab.h *.o $(PTMATLAB_LIB)

# Note: -fno-exceptions was added to remove the exception handling frames,
# which for gcc 3.2-3 (prerelease) was introducing an unresolved
# gcc_personality_v0 external, nowhere to be found within the cygwin libraries
# and object files...
