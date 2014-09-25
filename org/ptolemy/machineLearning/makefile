# Makefile org.ptolemy machine learning sources.
#
# @Version: $Id$
# @Author: Christopher Brooks
#
# @Copyright (c) 2013-2014 The Regents of the University of California.
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

DIRS = hmm hmmAOM hsmm particleFilter

# Root of Ptolemy II directory
ROOT =		../../..

JSRCS = Algorithms.java

# $(ME) is used in ptdir.mk
ME =		org/ptolemy/machineLearning

CLASSPATH =	$(ROOT)

# True source directory
VPATH =		$(ROOT)/$(ME)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	machineLearning
#PTCLASSJAR =
PTCLASSALLJAR = $(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	hmm/hmm.jar \
	hmmAOM/hmmAOM.jar \
	hsmm/hsmm.jar \
	particleFilter/particleFilter.jar

JSRCS = \
	Algorithms.java

OTHER_FILES_TO_BE_JARED = \
	machineLearning.xml

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = $(DIRS)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
