# Makefile for Java Ptolemy polymorphic actor classes
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
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

# This directory contains polymorphic actors.

ME =		ptolemy/actor/lib/ptalon

# $PTII/configure looks for Colt and sets PTCOLT_DIR in
# $PTII/mk/ptII.mk if was found.  Otherwise, PTCOLT_DIR is empty.

# $PTII/configure looks for Java Advanced Imaging and sets PTJAI_DIR in
# $PTII/mk/ptII.mk if was found.  Otherwise, PTJAI_DIR is empty.
#
# $PTII/configure looks for Java Media Framework and sets PTJMF_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJMF_DIR is empty.
#
# $PTII/configure looks for the Joystick interface  and sets PTJOYSTICK_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJOYSTICK_DIR is empty.
#
# $PTII/configure looks for Jython and sets PTJYTHON_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJYTHON_DIR is empty.
#
# $PTII/configure looks for JXTA and sets PTJXTA_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJXTA_DIR is empty.
#
# $PTII/configure looks for X10 and sets PTX10_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTX10_DIR is empty.

DIRS = 		conversions gui logic hoc image javasound net io xslt \
		test comm security string $(PTCOLT_DIR) $(PTJAI_DIR) \
		$(PTJMF_DIR) $(PTJOYSTICK_DIR) \
		$(PTJYTHON_DIR) $(PTJXTA_DIR) $(PTX10_DIR)

# Root of the Java directory
ROOT =		../../..

# Do not add diva.jar to the CLASSPATH in actor/lib/makefile
# instead, move any actors that require diva.jar into actor/lib/gui -cxh
CLASSPATH =	$(ROOT)${CLASSPATHSEPARATOR}${ROOT}/ptolemy/actor/ptalon/antlr/antlr.jar

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Keep this list alphabetized.
JSRCS = \
	ParameterMethodList.java \
	ParameterValues.java \
	PtalonTokenTypes.java \
	PtalonLexer.java \
	PtalonRecognizer.java \
	PtalonActor.java

EXTRA_SRCS =	$(JSRCS)



# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
