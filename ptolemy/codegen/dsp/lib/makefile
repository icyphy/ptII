# Makefile for Ptolemy II Dataflow CGC actors imported from Ptolemy Classic
#
# @Authors: Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2005 The Regents of the University of California.
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
#                                               PT_COPYRIGHT_VERSION_2
#                                               COPYRIGHTENDKEY

ME =            ptolemy/codegen/dsp/lib

# Rule for index.xml uses this
TITLE = 	"Dataflow CGC dsp Actors"

DIRS =		demo test    	

# Root of the Java directory
ROOT =          ../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =        $(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE =     lib
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS =        CGCBiquad.java \
CGCDTMFPostTest.java \
CGCDirichlet.java \
CGCFFTCx.java \
CGCFIR.java \
CGCFIRFix.java \
CGCFastFIR.java \
CGCGAL.java \
CGCGGAL.java \
CGCGoertzel.java \
CGCGoertzelBase.java \
CGCGoertzelPower.java \
CGCIIR.java \
CGCLMS.java \
CGCLMSOscDet.java \
CGCParametricEq.java \
CGCRaisedCosine.java \
CGCSinc.java \
CGCWindow.java \

OTHER_FILES_TO_BE_JARED = 	lib.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

JCLASS = $(JSRCS:%.java=%.class)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =	test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =         index.xml 	demo 
all: jclass index.xml
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
