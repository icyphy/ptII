# Makefile for Ptolemy II JXTA eptolemy demo
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1999-2005 The Regents of the University of California.
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

ME =		ptolemy/moml/jxta/demo/EPtolemyB

# Root of the Java directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR).

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	EPtolemyB
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \

OTHER_FILES_TO_BE_JARED = \
	EPtolemy.properties \
	PtolemyActorExchangeGroup.adv \
	actors.xml \
	discoveredActors.xml \
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
all: Gaussian.class jclass $(PTCLASSJAR)
	@echo "The EPtolemyB directory is used by the EPtolemyA demo"
	@echo "To run the demo, run 'make demo'"

install: all

Gaussian.class: $(ROOT)/ptolemy/actor/lib/Gaussian.class
	cp $(ROOT)/ptolemy/actor/lib/Gaussian.class .

# Build Guassian.class if it does not yet exist.  The nightly build
# requires this 
$(ROOT)/ptolemy/actor/lib/Gaussian.class: $(ROOT)/ptolemy/actor/lib/Gaussian.java
	(cd $(ROOT)/ptolemy/actor/lib; $(MAKE) Gaussian.class)

demo:
	@echo "Don't forget to run"
	@echo "   cd ../EPtolemyA; make demo"
	@echo "in another window"
	safePTII=`echo "$(PTII)" | sed 's@\\\@/@g'`; \
	"$(ROOT)/bin/moml" "$$safePTII/ptolemy/configs/jxta/configuration.xml" \
		"$$safePTII/ptolemy/configs/full/configuration.xml"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
