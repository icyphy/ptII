# Makefile for the Ptolemy II Auto-Modeling Agent (org.ptolemy.agent).
#
# @Version: $Id$
# @Author: Ptolemy II Agent contributors
#
# @Copyright (c) 2024-2026 The Regents of the University of California.
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
#                                                 PT_COPYRIGHT_VERSION_2
#                                                 COPYRIGHTENDKEY
##########################################################################

# Root of Ptolemy II directory
ROOT = ../../..

DIRS = session server tools llm agent library util test

# $(ME) is used in ptdir.mk
ME = org/ptolemy/agent

CLASSPATH = $(ROOT)

# True source directory
VPATH = $(ROOT)/$(ME)

# Get configuration info
CONFIG = $(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = agent
PTCLASSALLJAR = $(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	session/session.jar \
	server/server.jar \
	tools/tools.jar \
	llm/llm.jar \
	agent/agent-core.jar \
	library/library.jar \
	util/util.jar

JSRCS =

OTHER_FILES_TO_BE_JARED = \
	README.md

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

MISC_FILES = $(DIRS) bin demos

OPTIONAL_FILES = \
	$(DIRS) \
	bin \
	demos \
	doc

JCLASS = $(JSRCS:%.java=%.class)

all: suball
install: subinstall jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
