# Makefile for Building Controls Virtual Test Bed Matlab Files
#
# @Version: $Id$
# @Author: Christopher Brooks (makefile only) based on file by Zoltan Kemenczy
#
# @Copyright (c) 2009 The Regents of the University of California
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

ME =		lbnl/lib/matlab

DIRS =

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	matlab
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS =

OTHER_FILES_TO_BE_JARED =

CSRCS = \
	simulinkSocket.c

HSRCS = $(CSRCS:%.c=%.h) \
	matlabSocket.h

MSRCS = \
	bcvtb.m \
	closeIPC.m \
	compile.m \
	establishClientSocket.m \
	exchangeDoublesWithSocket.m \
	getBCVTBLibName.m \
	processError.m \
	sendClientError.m \
	slblocks.m

MEXMACISRCS = \
	closeBSDSocket.mexmaci \
	establishBSDSocket.mexmaci \
	exchangeDoublesWithBSDSocket.mexmaci

MEXMACISRCS64 = \
	closeBSDSocket.mexmaci64 \
	establishBSDSocket.mexmaci64 \
	exchangeDoublesWithBSDSocket.mexmaci64

MDLSRCS = \
	BCVTB.mdl \
	BSDSocket.mdl

EXTRA_SRCS =	$(JSRCS) $(CSRCS) $(HSRCS) $(MSRCS) \
		$(MDLSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES = \
	$(CSRCS) \
	$(HSRCS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(MEXMACISRCS) \
	$(MEXMACISRCS64)

JCLASS = $(JSRCS:%.java=%.class)

# The compile.m file generates Matlab .mex* files.
# PTMATLAB_MEXEXT is determined in $PTII/configure by running
# $(PTMATLAB_DIR)/bin/mexext
EXTRA_TARGETS = closeBSDSocket.$(PTMATLAB_MEXEXT)

# Files to remove with 'make clean'
KRUFT = *.$(PTMATLAB_MEXEXT) \
	libbcvtb_thunk_*

all: jclass $(EXTRA_TARGETS)

# FIXME: these dependencies are not necessarily correct
# ptcygpath might not be executable if we unjar'd the sources.
closeBSDSocket.$(PTMATLAB_MEXEXT): simulinkSocket.c simulinkSocket.h
	@echo "Invoking matlab to read compile.m and create $@"
	-chmod a+x $(PTII)/bin/ptcygpath
	PATH="`pwd`:`$(PTII)/bin/ptcygpath -u $(PTII)/lbnl/lib/util`:$(PTII)/lib:$(PATH)"; export PATH; \
	LD_LIBRARY_PATH="`pwd`:$(PTII)/lbnl/lib/util:$(PTII)/lib:$(LD_LIBRARY_PATH)"; export LD_LIBRARY_PATH; \
	DYLD_LIBRARY_PATH="`pwd`:$(PTII)/lbnl/lib/util:$(PTII)/lib:$(DYLD_LIBRARY_PATH)"; export DYLD_LIBRARY_PATH; \
	matlab -nodisplay -r compile

jclass: $(PTLIB)
install: $(PTLIB) jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
