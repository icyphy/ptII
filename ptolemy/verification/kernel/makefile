# Makefile for the Ptolemy Classic style code generation
#
# @Authors: Chihhong Patrick Cheng, Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2008-2009 The Regents of the University of California.
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

ME =		ptolemy/verification/kernel

DIRS =		maude

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	kernel
PTCLASSJAR =

PTCLASSALLJAR = $(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
# If you change this, change DIRS above
# Because of size issues, we don't include demo/demo.jar in verification.jar 
PTCLASSALLJARS = \
	maude/maude.jar

JSRCS = \
	MathematicalModelConverter.java \
	SMVUtility.java \
	REDUtility.java

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'MathematicalModelConverter$$1.class' \
	'MathematicalModelConverter$$FormulaType$$1.class' \
	'MathematicalModelConverter$$FormulaType.class' \
	'MathematicalModelConverter$$ModelType$$1.class' \
	'MathematicalModelConverter$$ModelType$$2.class' \
	'MathematicalModelConverter$$ModelType$$3.class' \
	'MathematicalModelConverter$$ModelType.class' \
	'MathematicalModelConverter$$OutputType$$1.class' \
	'MathematicalModelConverter$$OutputType$$2.class' \
	'MathematicalModelConverter$$OutputType.class' \
	'REDUtility$$1.class' \
	'REDUtility$$REDModuleNameInitialBean.class' \
	'REDUtility$$REDSingleEntityBean.class' \
	'REDUtility$$REDTransitionBean.class' \
	'REDUtility$$VariableInfo.class' \
	'SMVUtility$$1.class' \
	'SMVUtility$$VariableInfo.class' \
	'SMVUtility$$VariableTransitionInfo.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
