# Makefile for the KVM specific Ptolemy II Actor gui
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1999-2000 The Regents of the University of California.
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

# Location of this directory, relative to the Ptolemy II directory
ME =		ptolemy/kvm/actor/gui

# Root of the Ptolemy II directory
ROOT =		../../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(KVM_CLASSES)$(CLASSPATHSEPARATOR)$(KVM_DIR)/api/classes

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)
JSRCS = \
	SimpleSpotlet.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS =	$(JSRCS:%.java=%.class)

BINARIES =	SimpleSpotlet.prc

# KRUFT is removed by make clean
KRUFT =		$(BINARIES) 

all: jclass $(BINARIES)

install: jclass subinstall $(PTCLASSJAR)

SimpleSpotlet.prc: SimpleSpotlet.class
	CLASSPATH="$(CLASSPATH)" java palm.database.MakePalmApp -classpath $(ROOT) ptolemy.kvm.actor.gui.SimpleSpotlet

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
