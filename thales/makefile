# Makefile for Java classes Created by Thales
#
# @Version: $Id$
# @Author: Christopher Hylands
#
# This makefile has the following copyright
# @Copyright (c) 2003 The Regents of the University of California.
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

ME =		thales

DIRS = actor vergil configs 

# Root of Ptolemy II directory
ROOT =		..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	thales
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		actor/actor.jar \
		configs/configs.jar \
		vergil/vergil.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

EXTRA_SRCS =

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = actor vergil configs

OPTIONAL_FILES =

all: suball $(EXTRA_SRCS)
install: subinstall jars

# Include rules for directories that contain only subdirectories.
include $(ROOT)/mk/ptcommon.mk
