# Makefile for Java Ptolemy polymorphic javaspaces actor classes
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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

# This directory contains polymorphic actors.

ME =		ptolemy/actor/lib/jspaces

DIRS = 		util demo

# Root of the Java directory
ROOT =		../../../..

JINI_JAR_PATH = $(ROOT)/vendors/sun/jini/jini1_0_1/lib
JINI_JAR =	$(JINI_JAR_PATH)/jini-jspaces.jar

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(JINI_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	jspaces
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =	

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	util/util.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	IndexEntry.java \
	Publisher.java \
	Subscriber.java \
	TokenEntry.java 

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc 

STUB_JCLASS = \
	Subscriber_Stub.class

.PRECIOUS: $(STUB_JCLASS)

JCLASS = $(JSRCS:%.java=%.class) $(STUB_JCLASS)


all: jclass $(PTCLASSJAR)

install: all

depend:
	@echo "no dependencies in this directory"

Subscriber_Stub.class: Subscriber.java
	$(RMIC) -d $(ROOT) -classpath $(CLASSPATH) \
		ptolemy.actor.lib.$(PTPACKAGE).Subscriber

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
