# Makefile for Ptolemy II scripting actors
#
# @Authors: Christopher Brooks, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2011-2013 The Regents of the University of California.
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
#                                               PT_COPYRIGHT_VERSION_2
#                                               COPYRIGHTENDKEY

ME =            ptolemy/actor/lib/qm

DIRS = 		test #demo

# Root of the Java directory
ROOT =          ../../../..

# JYTHON_JAR is set in $PTII/mk/ptII.mk by running configure
CLASSPATH =     $(ROOT)$(CLASSPATHSEPARATOR)

# Get configuration info
CONFIG =        $(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE =     qm
PTCLASSJAR = 	$(PTPACKAGE).jar

OTHER_FILES_TO_BE_JARED = \
	qm.xml \
	AFDXESsIcon.xml \
	AFDXSwitchIcon.xml \
	BasicSwitchIcon.xml \
	BusIcon.xml \
	CQMInputPortIcon.xml \
	CQMOutputPortIcon.xml \
	CanBusIcon.xml \
	CompositeQuantityManagerIcon.xml \
	CrossbarSwitchIcon.xml \
	TTESwitchIcon.xml

JSRCS = \
	AFDXESs.java \
	AFDXSwitch.java \
	AFDXVlCfg.java \
	AFDXVlink.java \
	AtomicQuantityManager.java \
	BasicSwitch.java \
	Bus.java \
	CanBus.java \
	CanBusPriority.java \
	CQMInputPort.java \
	CQMOutputPort.java \
	CompositeQM.java \
	CrossbarSwitch.java \
	TTESwitch.java \
	QuantityManagerListener.java \
	QuantityManagerMonitor.java

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES = test doc-files

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
        doc \
	'QuantityManagerListener$$EventType.class' \
	'QuantityManagerMonitor$$QuantityManagerEditorFactory.class' \
	'AFDXSwitch$$AFDXSwitchAttributes.class' \
	'BasicSwitch$$BasicSwitchAttributes.class' \
	'Bus$$BusAttributes.class' \
	'CompositeQM$$CQMAttributes.class' \
	'MonitoredQuantityManager$$QMAttributes.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
