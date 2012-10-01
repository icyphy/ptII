# Makefile for ptserver data handler
#
# @Version: $Id$
# @Author: Christopher Brooks (makefile only)
#
# @Copyright (c) 2011-2012 The Regents of the University of California.
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
##########################################################################

ME =		ptserver/data/handler

DIRS =

# Root of Ptolemy II directory
ROOT =		../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

CLASSPATH =	$(ROOT)

# Used to build jar files
PTPACKAGE = 	handler
PTCLASSJAR =	$(PTPACKAGE).jar

# All the .class files necessary to run a Ptolemy II domain applet
#PTCLASSALLJARS =
#	handler/handler.jar

#PTCLASSALLJAR = $(PTPACKAGE).jar

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS = $(OTHER_FILES_TO_BE_JARED)

# Keep this list alphabetized.
JSRCS = \
	ArrayTokenHandler.java \
	AttributeChangeTokenHandler.java \
	BooleanTokenHandler.java \
	ByteArrayTokenHandler.java \
	CommunicationTokenHandler.java \
	ComplexMatrixTokenHandler.java \
	ComplexTokenHandler.java \
	DoubleMatrixTokenHandler.java \
	DoubleTokenHandler.java \
	FloatTokenHandler.java \
	IntMatrixTokenHandler.java \
	IntTokenHandler.java \
	LongMatrixTokenHandler.java \
	LongTokenHandler.java \
	PingPongTokenHandler.java \
	RecordTokenHandler.java \
	RemoteEventTokenHandler.java \
	ShortTokenHandler.java \
	StringTokenHandler.java \
	TokenHandler.java \
	UnionTokenHandler.java \
	UnsignedByteTokenHandler.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass $(EXTRA_SRCS)
install: subinstall jars

# Include rules for directories that contain only subdirectories.
include $(ROOT)/mk/ptcommon.mk
