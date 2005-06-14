# Makefile for GR domain Volume Rendering actors
#
# @Authors: Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2005 The Regents of the University of California.
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

ME =		ptolemy/domains/gr/lib/vr

# Rule for index.xml uses this
TITLE = 	"3D Volume Rendering"

# $PTII/configure looks for Quicktime for Java  and sets PTQUICKTIME_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTQUICKTIME_DIR is empty.
DIRS =	$(PTQUICKTIME_DIR)

# Root of the Java directory
ROOT =		../../../../..

# PTVAR_JAR is set by configure in $PTII/mk/ptII.mk to $PTII/lib/ptvr.jar
# IMAGEJ_JAR is set by configure in $PTII/mk/ptII.mk to $PTII/vendors/ImageJ/ij.jar
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(PTVR_JAR)$(CLASSPATHSEPARATOR)$(IMAGEJ_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	vr
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =	$(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS =

# Don't include nrl.jar
#		nrl/nrl.jar

#PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	DICOMReader.java \
	GRGeometry.java \
	GRShadedShapea.java \
	GRTexture2D.java \
	GRTexture2Da.java \
	GRTexture3D.java \
	GRVolume.java

OTHER_FILES_TO_BE_JARED = \
	vr-copyright.htm

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = 	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	index.xml

JCLASS = $(JSRCS:%.java=%.class)

all: jclass index.xml
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
