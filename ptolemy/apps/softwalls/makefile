# Makefile for softwalls actors
#
# @Version: $Id$
# @Author: Christopher Hylands
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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

ME =		ptolemy/apps/softwalls

# Alphabetical, please:
# $PTII/configure looks for various packages and sets variables
# in $PTII/mk/ptII.mk if these packages are found, otherwise
# the variables are empty
#
# PTKVM_DIR is set to kvm by configure if the kvm runtime was found
# PTJMF_DIR is set to jmf if the Java Media Framework was found.
# PTLEGO_DIR is set to lego if the commapi was found.
# PTLEJOS_DIR is set to lejos if Legos and the commapi was found.
# PTSOOT_DIR is set to soot if Soot was fount.
# PTWABA_DIR is set to waba if waba was found

DIRS =		


# Root of Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(DIVA_JAR)$(CLASSPATHSEPARATOR)$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	softwalls
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	ThreeDFunction.java \
	ImplicitSurface.java \
	AngleProcessor.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) #actors circles latex TwoDimensionalModel

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = 	

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: all $(PTCLASSJAR)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
