# Makefile for Ptolemy II Corba Actor classes
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

# Note: To compile the CorbaActor.idl, use:
# prompt> idltojava -p ptolemy.actor.corba -j ../../.. CorbaActor.idl
#
# idltojava can be downloaded from:
#
# http://developer.java.sun.com/developer/earlyAccess/jdk12/idltojava.html
#
# idltojava takes the CorbaActor.idl and generate all the files in the
# ptolemy/actor/corba/util directory.
#
# Currently, all generated java files in the util directory are checked in,
# and the command need to be run only if the CorbaActor.idl is changed.
# Then the newly generated java files need to be checked in again.
# This may not be the right way to do it...



ME =		ptolemy/actor/corba

DIRS = 		util

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)
# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	corba
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS =  \
	util/util.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	CorbaActorClient.java \
	RemoteCompositeActor.java

EXTRA_SRCS =	$(JSRCS) \
	CorbaActor.idl

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = 	

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars


depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
