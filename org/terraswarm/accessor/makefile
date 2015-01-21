# Makefile for TerraSwarm Accessors
#
# @Version: $Id: makefile 70194 2014-09-25 14:25:22Z cxh $
# @Author: Christopher Brooks
#
# @Copyright (c) 2015 The Regents of the University of California.
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

# PTJAVASCRIPT is set by configure to js if js.jar is found.
# PTJJS_DIR is set by configure to jjs if Nashorn (present in Java 1.8) is found.
DIRS = $(PTJAVASCRIPT_DIR) $(PTJJS_DIR)

# Root of Ptolemy II directory
ROOT =		../../..

JSRCS = 

# $(ME) is used in ptdir.mk
ME =		org/terraswarm/accessor

CLASSPATH =	$(ROOT)

# True source directory
VPATH =		$(ROOT)/$(ME)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	accessor
PTCLASSJAR =	$(PTPACKAGE).jar
#PTCLASSALLJAR = $(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS =

JSRCS =

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = $(DIRS)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
