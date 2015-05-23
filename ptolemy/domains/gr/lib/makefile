# Makefile for GR domain actors library
#
# @Authors: Christopher Hylands (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 1998-2009 The Regents of the University of California.
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

# $PTII/configure looks for Quicktime for Java  and sets PTQUICKTIME_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTQUICKTIME_DIR is empty.
DIRS =	$(PTQUICKTIME_DIR)

# Root of the Java directory
ROOT =		../../../..

# JAVA3D_JARS is the jar files to use with Java 3D.  Usually, this is
# empty, because most installations have Java 3D in the jdk
# jre/lib/ext directory, but some do not. If --with-java3D was used
# with configure, then JAVA3D_JARS will be set to include the jars.
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)$(CLASSPATHSEPARATOR)$(JAVA3D_JARS)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	lib
PTCLASSJAR =	$(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS =

# Don't include nrl.jar
#		nrl/nrl.jar

#PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	Box2D.java \
	Box3D.java \
	CircularSweep3D.java \
	Cone3D.java \
	Cylinder3D.java \
	Ellipse2D.java \
	GRShadedShape.java \
	GRShape2D.java \
	GRTransform.java \
	GRTransform2D.java \
	FigureInteractor.java \
	Image2D.java \
	KeyInput3D.java \
	KeyInput3DProbe.java \
	Line2D.java \
	MouseInput3D.java \
	MovableViewScreen3D.java \
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
	ViewLayers3D.java \
	ViewScreen3D.java \
	ViewScreen2D.java


OTHER_FILES_TO_BE_JARED = \
	Box3D.gif \
	Box3DIcon.xml \
	CircularSweep3D.gif \
	CircularSweep3DIcon.xml \
	Cone3D.gif \
	Cone3DIcon.xml \
	Cylinder3D.gif \
	Cylinder3DIcon.xml \
	grentities.xml \
	java3d-copyright.htm \
	PolyCylinder3D.gif \
	PolyCylinder3DIcon.xml \
	Sphere3D.gif \
	Sphere3DIcon.xml \
	ThreeAxisRotator3D.xml \
	ThreeAxisRotator3DIcon.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = 	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	quicktime \
	'IconViewScreen3D$$CapturingCanvas3D.class' \
	'ViewScreen2D$$ViewScreen2DListener.class'\
	'ViewScreen3D$$MouseRotateView.class' \
	'KeyInput3D$$_React.class' \
	'MouseInput3D$$_React.class' \
	'KeyInput3D$$1.class' \
	'KeyInput3D$$React.class' \
	'MouseInput3D$$1.class' \
	'MouseInput3D$$React.class' \
	'ViewScreen$$MouseRotateView.class' \
	'ViewLayers3D$MouseRotateView.class' \
	doc

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
