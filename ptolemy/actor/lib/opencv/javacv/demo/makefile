# Makefile for Java Ptolemy II Open Computer Vision (OpenCV) JavaCV demos
#
# @Authors: Christopher Brooks, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2010 The Regents of the University of California.
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

ME =		ptolemy/actor/lib/opencv/javacv/demo

DIRS =

# Root of the Ptolemy II directory
ROOT =		../../../../../..

# JAVACV_JAR is set by configure if JavaCV is found.
# JavaCV may be found at http://code.google.com/p/javacv/
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(OPENCV_JAR)$(CLASSPATHSEPARATOR)$(JAVACV_JAR)$(CLASSPATHSEPARATOR)$(JNA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	demo
PTCLASSJAR =	$(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
#PTCLASSALLJARS =
#PTCLASSALLJAR = 

# Keep this list alphabetized.
JSRCS = \
	FaceDetectTest.java

OTHER_FILES_TO_BE_JARED = \
	CameraDetectView_3win.xml \
	CameraDetectView.xml \
	CameraFlipView.xml \
	CameraResizedView.xml \
	CameraSmoothView.xml \
	CameraSobelView.xml \
	CameraView.xml \
	ImageConvView.xml \
	ImageDetectView_3.xml \
	ImageDetectView.xml \
	ImageRotateView.xml

EXTRA_SRCS = \
	$(JSRCS) \
	$(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To run the demos, run 'make demo'"

install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
