# Makefile for JNI Interface
#
# @Version: $Id$
# @Author: Christopher Hylands (makefile only)
#
# @Copyright (c) 2003 The Regents of the University of California.
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

ME =		jni

DIRS = 		doc demo test #dll

# Root of Ptolemy II directory
ROOT =		..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	jni
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

# Include these jars in PTAUXALLJAR
PTAUXALLJARS = \
		demo/demo.jar \
		doc/doc.jar

PTAUXALLJAR =	$(PTPACKAGE).jar

OTHER_FILES_TO_BE_JARED = \
	dll.gif \
	jni.xml

# Keep this list alphabetized.
JSRCS = \
	Argument.java \
	ArgumentConfigurer.java \
	ArgumentConfigurerDialog.java \
	ArgumentDialogFactory.java \
	GenericJNIActor.java \
	JNIUtilities.java \
	ThalesGraphFrame.java \
	ThalesGraphTableau.java \
	VergilApplication.java

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = doc test

OPTIONAL_FILES = demo \
	'ArgumentDialogFactory$$1.class' \
	'ThalesGraphFrame$$DebugMenuListener.class' \
	'ThalesGraphFrame$$JNIMenuListener.class' \
	'ThalesGraphTableau$$Factory.class' \
	'VergilApplication$$1.class'


JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: all jars

# Run all the tests that do not take much time.
fast_tests:
	$(MAKE) PTCOPERNICUS_DIR= tests 


# Include rules for directories that contain only subdirectories.
include $(ROOT)/mk/ptcommon.mk
