# Makefile for GR domain actors library
#
# @Authors: Christopher Hylands (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 1998-2003 The Regents of the University of California.
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

ME =		ptolemy/domains/gr/lib

# Rule for index.xml uses this
TITLE = 	"3D actors"

# $PTII/configure looks for Quicktime for Java  and sets PTQUICKTIME_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTQUICKTIME_DIR is empty.
DIRS =	$(PTQUICKTIME_DIR)

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	lib
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =	$(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS =

# Don't include nrl.jar
#		nrl/nrl.jar

#PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	Box3D.java \
	CircularSweep3D.java \
	Cone3D.java \
	Cylinder3D.java \
	Ellipse2D.java \
	GRShadedShape.java \
	GRShape2D.java \
	GRTransform.java \
	GRTransform2D.java \
	IconViewScreen3D.java \
	Image2D.java \
	KeyInput3D.java \
	Line2D.java \
	MouseInput3D.java \
	PolyCylinder3D.java \
	Rectangle2D.java \
	RectangularFigure2D.java \
	Rotate2D.java \
	Rotate3D.java \
	Scale2D.java \
	Scale3D.java \
	Sphere3D.java \
	TextString3D.java \
	Torus3D.java \
	Translate2D.java \
	Translate3D.java \
	ViewScreen.java \
	ViewScreen2D.java


OTHER_FILES_TO_BE_JARED = \
	grentities.xml \
	java3d-copyright.htm

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = 	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'KeyInput3D$$_React.class' \
	'MouseInput3D$$_React.class' \
	'KeyInput3D$$1.class' \
	'KeyInput3D$$React.class' \
	'MouseInput3D$$1.class' \
	'MouseInput3D$$React.class' \
	'ViewScreen$$MouseRotateView.class' \
	doc \
	index.xml

JCLASS = $(JSRCS:%.java=%.class)

all: jclass index.xml
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
