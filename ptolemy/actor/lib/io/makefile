# Makefile for io (a.k.a. I/O) Java classes.
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2001 The Regents of the University of California.
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

# This directory contains io (a.k.a I/O) classes.

ME =            ptolemy/actor/lib/io

DIRS =

# Root of the Java directory
ROOT =          ../../../..

 JINI_JAR_PATH = $(ROOT)/vendors/sun/jini/lib
 JINI_JAR =      $(JINI_JAR_PATH)/jini-jspaces.jar


# COMMAPI_DIR is set in $PTII/mk/ptII.mk by running configure
CLASSPATH =     $(ROOT)$(CLASSPATHSEPARATOR)$(COMMAPI_DIR)/comm.jar$(CLASSPATHSEPARATOR)$(JINI_JAR)

# Get configuration info
CONFIG =        $(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE =     io
PTDIST =        $(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = DatagramSender.java \
        DatagramReceiver.java \
        SerialComm.java \
        RxDatagram.java \
        TxDatagram.java

EXTRA_SRCS =    $(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =    $(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
        doc \
	'TxDatagram$$UpdateThread.class'

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass jars


#demo: $(JCLASS)
#	# Download the nqc program
#	nqc -d tank.nqc
#	# Start it
#	nqc -run
#	# Start up Ptolemy
#	#CLASSPATH="$(CLASSPATH)" $(JAVA) ptolemy.apps.lego.BrownianMotion
#	$(PTII)/bin/ptolemy BrownianMotion.xml

##############
# MoML rules
.SUFFIXES: .xml .class
.class.xml:
	rm -f `basename $< .class`.xml
	CLASSPATH="$(CLASSPATH)" $(JAVA) \
	        ptolemy.apps.lego.`basename $< .class` \
		-dumpMoML `basename $< .class`.xml

moml: BrownianMotion.xml TiltDriver.xml

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
