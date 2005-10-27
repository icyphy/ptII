# Makefile for the PtinyOS domain nc2moml utility.
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998-2005 The Regents of the University of California.
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

ME =		ptolemy/domains/ptinyos/util/nc2moml

DIRS =

# Root of the Java directory
ROOT =		../../../../..

PTINYOS_DIR =   $(ROOT)/ptolemy/domains/ptinyos

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(PTINYOS_DIR)/lib/jdom.jar$(CLASSPATHSEPARATOR)$(PTINYOS_DIR)/lib/nesc-dump.jar

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	nc2moml
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	MoMLLib.java \
	NC2MoML.java

OTHER_FILES_TO_BE_MADE_EXECUTABLE = \
	nc2moml \
	nc2moml-get-args \
	nc2moml-main \
	nc2moml-run-ncc

OTHER_FILES_TO_BE_JARED = \
	$(OTHER_FILES_TO_BE_MADE_EXECUTABLE) \
	index.htm

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'MoMLLib$$1.class' \
	'MoMLLib$$2.class' \
	'MoMLLib$$3.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	chmod a+x $(OTHER_FILES_TO_BE_MADE_EXECUTABLE)
	@echo "Run \"make install\" to convert nc files to moml (take about 20 minutes)"
	@echo "Run \"make test\" for a quick test"


install: jclass
	@echo "Warning: nc2moml takes about 20 minutes to run" 
	./nc2moml

test: jclass
	@echo "Converting .nc files to .moml files for a simple demo"
	./nc2moml -v tos/lib/Counters
	./nc2moml -v tos/system
	./nc2moml -v tos/sensorboards/micasb
	"$(PTII)/bin/vergil" -viptos ../../demo/SenseToLedsInWireless/SenseToLedsInWireless.xml 

include $(ROOT)/mk/ptcommon.mk

