# Makefile for Ptolemy II jni demo
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

ME =		jni/demo/jni

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	jni
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \

MODELS= \
	meaningOfLife.xml \
	testDeux.xml \
	testQuatre.xml \
	testTrois.xml

C_FILES = \
	meaningOfLife.c \
	testDeux.c \
	testTrois.c

H_FILES = $(JSRCS:%.c=%.h)

OTHER_FILES_TO_BE_JARED = \
	$(MODELS) \
	$(C_FILES) \
	$(H_FILES) \
	README.htm

EXTRA_SRCS = \
	$(JSRCS) \
	$(OTHER_FILES_TO_BE_JARED) \

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

# KRUFT is used by make clean
KRUFT = *.o *.obj *.dll *.so

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: libs jclass $(PTCLASSJAR)
	@echo "To run all the demos, run 'make demo'"

libs:
	$(MAKE) SHAREDBASE=meaningOfLife shared
	$(MAKE) SHAREDBASE=testDeux shared
	$(MAKE) SHAREDBASE=testTrois shared


install: all

# Run the demo in all its different formats
demo: demo_vergil


demo_vergil:
	CLASSPATH=.; \
	PATH="`pwd`:`pwd`/jni/meaningOfLife:`pwd`/jni/testDeux:`pwd`/jni/testTrois:`pwd`jni/jnimeaningOfLifeImeaningOfLife:$(PATH)"; export PATH; \
	LD_LIBRARY_PATH="`pwd`:`pwd`/jni/meaningOfLife:`pwd`/jni/testDeux:`pwd`/jni/testTrois:`pwd`jni/jnimeaningOfLifeImeaningOfLife:/usr/local/lib:$(LD_LIBRARY_PATH)"; export LD_LIBRARY_PATH; \
	"$(PTII)/bin/vergil" -jni \
		$(MODELS)

# PTCC is set in $PTII/mk/ptII.mk
# See also ptolemy/matlab/makefile
SHARED_LIBRARY = $(PTJNI_SHAREDLIBRARY_PREFIX)$(SHAREDBASE).$(PTJNI_SHAREDLIBRARY_SUFFIX)

shared: $(SHARED_LIBRARY)
$(SHARED_LIBRARY): $(SHAREDBASE).c $(SHAREDBASE).h makefile
	$(PTCC) -shared $(PTJNI_SHAREDLIBRARY_CFLAG) \
		-o $@ \
		$(SHAREDBASE).c

include $(ROOT)/mk/ptcommon.mk
