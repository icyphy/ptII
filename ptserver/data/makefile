# Makefile for ptserver data - a system that allows execution of models on handheld devices
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

ME =		ptserver/data

DIRS =		handler

# Root of Ptolemy II directory
ROOT =		../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

CLASSPATH =	$(ROOT)

# Used to build jar files
PTPACKAGE = 	data
PTCLASSJAR =

# All the .class files necessary to run a Ptolemy II domain applet
PTCLASSALLJARS = \
	handler/handler.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

OTHER_FILES_TO_BE_JARED = \
	TokenHandlers.properties

EXTRA_SRCS = $(OTHER_FILES_TO_BE_JARED)

# Keep this list alphabetized.
JSRCS = \
	AttributeChangeToken.java \
	ByteArrayToken.java \
	CommunicationToken.java \
	PingToken.java \
	PongToken.java \
	RemoteEventToken.java \
	TokenParser.java \
	Tokenizer.java

OTHER_FILES_TO_BE_JARED = \
	TokenHandlers.properties

SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = \
	handler

OPTIONAL_FILES = \
	'RemoteEventToken$$EventType.class' \
	'ServerEventToken$$EventType.class' \
	'TokenParser$$HandlerData.class'


JCLASS = $(JSRCS:%.java=%.class)

EXTRA_SRCS =	$(JSRCS)

all: jclass $(EXTRA_SRCS)
install: subinstall jars

# Include rules for directories that contain only subdirectories.
include $(ROOT)/mk/ptcommon.mk
