# Makefile for Java Ptolemy Codegen C Targets Luminary Domains Ptides
#
# @Authors: Jeff C. Jensen, based on a file by Man-Kit Leung, based on a file by Christopher Brooks
#
# $Id$
#
# @Copyright (c) 2009-2010 The Regents of the University of California.
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
#                       PT_COPYRIGHT_VERSION_2
#                       COPYRIGHTENDKEY

ME =		ptolemy/cg/adapter/generic/program/procedural/c/luminary/adapters/ptolemy/domains/ptides/lib/luminary

DIRS =		

# Root of the Java directory
ROOT =		../../../../../../../../../../../../../..


CLASSPATH = $(ROOT)

# Get configuration info
CONFIG =    $(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE =     luminary

PTCLASSJAR =	$(PTPACKAGE).jar

PTCLASSALLJARS = 

JSRCS = \
	DropSensorInputHandler.java \
	EncoderHandler.java \
	GPInputHandler.java \
	GPOutputSetup.java \
	MotorSetup.java \
	SongWrapper.java \
	SpeakerOutputDevice.java \
	TimeTriggeredEncoderHandler.java

OTHER_FILES_TO_BE_JARED = \
	DropSensorInputHandler.c \
	EncoderHandler.c \
	GPInputHandler.c \
	GPOutputSetup.c \
	MotorSetup.c \
	SongWrapper.c \
	SpeakerOutputDevice.c \
	TimeTriggeredEncoderHandler.c

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =    $(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
    doc

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
