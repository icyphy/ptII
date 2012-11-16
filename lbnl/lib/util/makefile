# Makefile for Building Controls Virtual Test Bed
#
# @Version: $Id$
# @Author: Christopher Brooks (makefile only), based on file by Zoltan Kemenczy
#
# @Copyright (c) 2009-2012 The Regents of the University of California
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

ME =		lbnl/lib/util

DIRS =

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	lbnlutil
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS =

OTHER_FILES_TO_BE_JARED =


EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

CSRCS = \
	utilSocket.c \
	utilXml.c \

HSRCS = $(CSRCS:%.c=%.h)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES = \
	$(CSRCS) \
	$(HSRCS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTLIB)

JCLASS = $(JSRCS:%.java=%.class)

# 'make fast' also build PTLIB, so be sure that PTLIB is set to
# the name of the library
PTLIB=$(PTJNI_SHAREDLIBRARY_PREFIX)bcvtb.$(PTJNI_SHAREDLIBRARY_SUFFIX)

# Files to remove with 'make clean'
KRUFT = \
	$(PTLIB) \
	$(PTJNI_SHAREDLIBRARY_PREFIX)bcvtb.$(PTJNI_SHAREDLIBRARY_SUFFIX2) \
	$(PTJNI_SHAREDLIBRARY_PREFIX)bcvtb.so \
	*.dSYM \
	*.jar

all: jclass

jclass: $(PTLIB)


# MAJOR_OS_NAME is set by configure to the major name of the OS
# Under all versions of Windows, MAJOR_OS_NAME = Windows
PTPACKAGE_NATIVE_JAR =	$(PTPACKAGE)$(MAJOR_OS_NAME).jar
install: $(PTLIB) jclass jars $(PTPACKAGE).jar $(PTPACKAGE_NATIVE_JAR)
	@echo "'$(PTPACKAGE).jar' requires gcc, so we install it in \$$PTII/lib"
	@echo "for hosts that might not have gcc installed and for Web Start."
	cp $(PTLIB) $(ROOT)/lib
	if [ "$(PTJNI_SHAREDLIBRARY_SUFFIX)" != "$(PTJNI_SHAREDLIBRARY_SUFFIX2)" -a "$(PTJNI_SHAREDLIBRARY_SUFFIX2)" != "" ]; then \
	    echo "Copying $(PTJNI_SHAREDLIBRARY_PREFIX)bcvtb.$(PTJNI_SHAREDLIBRARY_SUFFIX2) to $(ROOT)/lib"; \
	    cp $(PTJNI_SHAREDLIBRARY_PREFIX)bcvtb.$(PTJNI_SHAREDLIBRARY_SUFFIX2) $(ROOT)/lib; \
        fi
	#cp $(PTPACKAGE).jar $(PTPACKAGE_NATIVE_JAR) $(ROOT)/lib


clean: ptmatlab_clean

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

C_DEBUG=-g
# If NDEBUG is defined, then utilSocket.c will create utilSocket.log
# and add debugging messages to it.
#C_DEFINES=-DNDEBUG
# $(PTJNI_SHAREDLIBRARY_LDFLAG) is needed for 64 bit Linux, it is set to -fPIC
# $(PTJNI_SHAREDLIBRARY_LDFLAG) is needed for 64 bit Mac OS X, it is set to -m64
$(PTLIB): $(CSRCS) $(HSRCS)
	"$(PTCC)" -shared $(PTJNI_GCC_SHARED_FLAG) \
		$(C_DEBUG) \
		$(C_DEFINES) \
		$(PTJNI_SHAREDLIBRARY_LDFLAG) \
		-o $@ \
		-I.. -I $(LIBEXPAT_INCLUDE_DIR) \
		$(CSRCS) \
		-L$(LIBEXPAT_LIB_DIR) -lexpat
	if [ "$(PTJNI_SHAREDLIBRARY_SUFFIX)" != "$(PTJNI_SHAREDLIBRARY_SUFFIX2)" -a "$(PTJNI_SHAREDLIBRARY_SUFFIX2)" != "" ]; then \
		if [ "$(PTJNI_SHAREDLIBRARY_SUFFIX2)" != "so.*" ]; then \
		    echo "Creating $(PTJNI_SHAREDLIBRARY_PREFIX)bcvtb.$(PTJNI_SHAREDLIBRARY_SUFFIX2)"; \
		    cp $(PTLIB) $(PTJNI_SHAREDLIBRARY_PREFIX)bcvtb.$(PTJNI_SHAREDLIBRARY_SUFFIX2); \
	        fi; \
		if [ "$(PTJNI_SHAREDLIBRARY_SUFFIX)" != "so" ]; then \
		    echo "Creating $(PTJNI_SHAREDLIBRARY_PREFIX)bcvtb.$(PTJNI_SHAREDLIBRARY_PREFIX)bcvtb.so"; \
		    cp $(PTLIB) $(PTJNI_SHAREDLIBRARY_PREFIX)bcvtb.so; \
                fi; \
	fi

$(PTPACKAGE_NATIVE_JAR): $(PTLIB)
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	-cp $< $(PTJAR_TMPDIR)
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" -cvf tmp.jar .)
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	rm -rf $(PTJAR_TMPDIR)


ptmatlab_clean:
	rm -f ptmatlab.h *.o $(PTLIB)

# Note: -fno-exceptions was added to remove the exception handling frames,
# which for gcc 3.2-3 (prerelease) was introducing an unresolved
# gcc_personality_v0 external, nowhere to be found within the cygwin libraries
# and object files...
