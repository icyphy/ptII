# Makefile for Building Controls Virtual Test Bed C language Room demo 
#
# @Authors: Christopher Hylands (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2009 The Regents of the University of California.
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

ME =		lbnl/demo/CRoom

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	CRoom
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \

# Have meaning of life last so that make demo shows it on top.
MODELS= \
	CRoom.xml

C_FILES = \
	cclient.c

CCLIENT = cclient

#H_FILES = $(C_FILES:%.c=%.h)

OTHER_FILES_TO_BE_JARED = \
	$(MODELS) \
	$(C_FILES) \

EXTRA_SRCS = \
	$(JSRCS) \
	$(OTHER_FILES_TO_BE_JARED) \

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = 
#	$(H_FILES)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = $(CCLIENT)

JCLASS = $(JSRCS:%.java=%.class)

# KRUFT is used by make clean
KRUFT = *.o *.obj *.dll *.so simulation.log socket.cfg utilSocket.log $(CCLIENT)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: $(CCLIENT)
	@echo "To run all the demos, run 'make demo'"

install: all jars

# Run the demo in all its different formats
demo: demo_vergil

demo_vergil: $(CCLIENT)
	CLASSPATH=.; \
	PATH="`pwd`:`$(PTII)/bin/ptcygpath -u $(PTII)/lbnl/lib/util`:$(PTII)/lib:$(PATH)"; export PATH; \
	LD_LIBRARY_PATH="`pwd`:$(PTII)/lbnl/lib/util:$(PTII)/lib:$(LD_LIBRARY_PATH)"; export LD_LIBRARY_PATH; \
	DYLD_LIBRARY_PATH="`pwd`:$(PTII)/lbnl/lib/util:$(PTII)/lib:$(DYLD_LIBRARY_PATH)"; export DYLD_LIBRARY_PATH; \
	"$(PTII)/bin/vergil" \
		$(MODELS)


# Name and location of shared library.
SHAREDBASE =	bcvtb
SHARED_LIBRARY = $(PTJNI_SHAREDLIBRARY_PREFIX)$(SHAREDBASE).$(PTJNI_SHAREDLIBRARY_SUFFIX)

# PTCC is set in $PTII/mk/ptII.mk
# See also ptolemy/matlab/makefile
$(CCLIENT): cclient.c ../../lib/util/$(SHARED_LIBRARY)
	if [ `uname -s` = "Darwin" ]; then \
		RPATH=-Wl,-rpath,$(PTII)/lbnl/lib/util; \
		echo "Set RPATH to $$RPATH for use under Darwin."; \
	fi; \
	$(PTCC) -g -o $@ -I../../lib -I../../lib/util cclient.c $$RPATH -L$(prefix)/lbnl/lib/util -lbcvtb

../../lib/util/$(SHARED_LIBRARY):
	(cd ../../lib/util; $(MAKE))

include $(ROOT)/mk/ptcommon.mk
