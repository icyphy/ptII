# Makefile for Ptolemy II JXTA eptolemy demo
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1999-2002 The Regents of the University of California.
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

ME =		ptolemy/moml/jxta/demo/eptolemy

# Root of the Java directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	eptolemy
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \

OTHER_FILES_TO_BE_JARED = \
	Average.class \
	PtolemyActorExchangeGroup.adv \
	ePtolemy.properties \
	ptpipe.adv

EXTRA_SRCS = \
	$(JSRCS) \
	$(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: Average.class jclass $(PTCLASSJAR)
	@echo "To run the demo, run 'make demo'"

install: all

Average.class:
	cp $(ROOT)/ptolemy/actor/lib/Average.class .

demo:
	@echo "Note1: set the HTTP and TCP address(in the "Advanced" tab of"
	@echo " the JXTA Configuration window) to be different for these "
	@echo "two peers to make them work on one machine."
	@echo "" 
	@echo "Note2: It might take some time at the first time for the "
	@echo "configuration. For the first time, the configuration will try"
	@echo "to contact a server(hard-coded by the current JXTA "
	@echo "implementation) to get some default rendezvous address. "
	@echo "This is not a good design, but lets just bear it for "
	@echo "now. Sometimes, the server may not work, and you'll get "
	@echo "an message as: "
	@echo "  Error: cannot instantiate peer group....... "
	@echo "If so,  try it later."
	"$(ROOT)/bin/moml" $(PTII)/ptolemy/configs/p2p.xml \
		$(PTII)/ptolemy/configs/vergilConfiguration.xml &
	(cd ../eptolemy2;  
	"$(ROOT)/bin/moml" $(PTII)/ptolemy/configs/p2p.xml \
		$(PTII)/ptolemy/configs/vergilConfiguration.xml)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
