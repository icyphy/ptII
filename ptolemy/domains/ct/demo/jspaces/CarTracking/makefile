# Makefile for Ptolemy II Continuous Time domain demos
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

ME =		ptolemy/domains/ct/demo/CarTracking

# Root of the Java directory
ROOT =		../../../../../..

# JINI_JARS is defined in $PTII/mk/ptII.mk, which is created by $PTII/configure
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(JINI_JARS)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	CarTracking
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	BangBangController.java \
	CarInformationPublisher.java \
	CarInformationSubscriber.java \
	CTSubscriber.java \
	DERealTimeSubscriber.java \
	SimpleModalController.java \
	RealTimePublisher.java \
	TrackingAlarm.java

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) 

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = \
	carmodel.xml \
	LeadingCar.xml \
	FollowingCar.xml \
	Monitor.xml

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	$(PTCLASSJAR)

STUB_JCLASS = \
	CarInformationSubscriber_Stub.class \
	CTSubscriber_Stub.class \
	DERealTimeSubscriber_Stub.class

.PRECIOUS: $(STUB_JCLASS)

JCLASS = $(JSRCS:%.java=%.class) $(STUB_JCLASS)


# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass $(PTCLASSJAR)
	@echo "To run all the demos, run 'make demo'"

install: all

depend:
	@echo "no dependencies in this directory"

# The -d $(ROOT) is necessary for the rmic that ships with JDK1.3

CarInformationSubscriber_Stub.class: CarInformationSubscriber.java
	$(RMIC) -d $(ROOT) -classpath "$(CLASSPATH)" \
		ptolemy.domains.ct.demo.$(PTPACKAGE).CarInformationSubscriber

CTSubscriber_Stub.class: CTSubscriber.java
	$(RMIC) -d $(ROOT) -classpath "$(CLASSPATH)" \
		ptolemy.domains.ct.demo.$(PTPACKAGE).CTSubscriber

DERealTimeSubscriber_Stub.class: DERealTimeSubscriber.java
	$(RMIC) -d $(ROOT) -classpath "$(CLASSPATH)" \
		ptolemy.domains.ct.demo.$(PTPACKAGE).DERealTimeSubscriber

demo:
	@echo "Before running this demo the first time, run 'make startjini'"
	$(MAKE) runLeadingCar &
	$(MAKE) runFollowingCar &
	$(MAKE) runMonitor

runLeadingCar:
	$(PTII)/bin/ptjini LeadingCar.xml

runFollowingCar:
	$(PTII)/bin/ptjini FollowingCar.xml

runMonitor:
	$(PTII)/bin/ptjini Monitor.xml

# Start up Jini
# The first option argument is the Jini home directory,
# the second optional argument is the temporary directory to use
startjini:
	$(PTII)/bin/startjini $(JINI_DIR)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
