# Makefile for Ptolemy II jni using gcc
#
# @Authors: Christopher Hylands (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2003 The Regents of the University of California.
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

ME =		jni/demo/HelloWorld

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	HelloWorld
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \

OTHER_FILES_TO_BE_JARED = \
	HelloWorld.c

EXTRA_SRCS = \
	$(JSRCS) \
	$(OTHER_FILES_TO_BE_JARED) \

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =

KRUFT = \
	HelloWorld.h \
	HelloWorld.dll

JCLASS = $(JSRCS:%.java=%.class)

# Under Windows, this would be HelloWorld.dll
# Under Solaris, this would be libHelloWorld.so
SHARED_LIBRARY = $(PTJNI_SHAREDLIBRARY_PREFIX)$(PTPACKAGE).$(PTJNI_SHAREDLIBRARY_SUFFIX)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass $(SHARED_LIBRARY) $(PTCLASSJAR)
	@echo "To run all the demos, run 'make demo'"

install: all

# Run the demo in all its different formats
demo: $(SHARED_LIBRARY)
	PATH="$(PTII)/$(ME)$(CLASSPATHSEPARATOR)$(PATH)" \
		LD_LIBRARY_PATH="$(PTII)/$(ME):/usr/local/lib:$(PATH)" \
		CLASSPATH="$(CLASSPATH)$(AUXCLASSPATH)" \
		"$(JAVA)" \
		jni.demo.HelloWorld.HelloWorld


#HellowWorld.class: HelloWorld.java
#	javac -classpath $(ROOT) HellowWorld.java

HelloWorld.h: HelloWorld.class
	javah -classpath $(ROOT) -o HelloWorld.h jni.demo.HelloWorld.HelloWorld

# PTCC is set in $PTII/mk/ptII.mk
# See also ptolemy/matlab/makefile and jni/JNIUtilities.java
$(SHARED_LIBRARY): HelloWorld.h HelloWorld.c
	$(PTCC) -shared \
		$(PTJNI_NO_CYGWIN) $(PTJNI_SHAREDLIBRARY_LDFLAG) \
		"-I$(PTJAVA_DIR)/include" \
		"-I$(PTJAVA_DIR)/include/$(PTJNI_ARCHITECTURE)" \
		-o $@ \
		HelloWorld.c

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
