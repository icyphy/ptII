# Makefile for caltrop classes
#
# @Authors: Christopher Hylands (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2003-2005 The Regents of the University of California.
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

ME =		ptolemy/caltrop

DIRS = 		actors ddi util demo test

# Root of the Ptolemy II directory
ROOT =		../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(ROOT)/lib/ptCal.jar

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Handle assertions with -source 1.4
JFLAGS =	$(JDEBUG) $(JOPTIMIZE) -source 1.4

# Used to build jar files
PTPACKAGE = 	caltrop
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		actors/actors.jar \
		ddi/ddi.jar \
		util/util.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	CalIOException.java \
	FunctionCallException.java \
	PtolemyPlatform.java

OTHER_FILES_TO_BE_JARED = \
	cup-copyright.htm \
	saxon-copyright.htm

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =	actors ddi util test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	doc \
	$(PTCLASSALLJAR) \
	'PtolemyPlatform$$10.class' \
	'PtolemyPlatform$$11.class' \
	'PtolemyPlatform$$12.class' \
	'PtolemyPlatform$$13.class' \
	'PtolemyPlatform$$14.class' \
	'PtolemyPlatform$$15.class' \
	'PtolemyPlatform$$16.class' \
	'PtolemyPlatform$$17.class' \
	'PtolemyPlatform$$18.class' \
	'PtolemyPlatform$$19.class' \
	'PtolemyPlatform$$1.class' \
	'PtolemyPlatform$$20.class' \
	'PtolemyPlatform$$21.class' \
	'PtolemyPlatform$$22.class' \
	'PtolemyPlatform$$23.class' \
	'PtolemyPlatform$$24.class' \
	'PtolemyPlatform$$25.class' \
	'PtolemyPlatform$$26.class' \
	'PtolemyPlatform$$27.class' \
	'PtolemyPlatform$$28.class' \
	'PtolemyPlatform$$29.class' \
	'PtolemyPlatform$$2.class' \
	'PtolemyPlatform$$30.class' \
	'PtolemyPlatform$$31.class' \
	'PtolemyPlatform$$3.class' \
	'PtolemyPlatform$$4.class' \
	'PtolemyPlatform$$5.class' \
	'PtolemyPlatform$$6.class' \
	'PtolemyPlatform$$7.class' \
	'PtolemyPlatform$$8.class' \
	'PtolemyPlatform$$9.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
