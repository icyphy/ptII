# Makefile for util/java2d classes - Ptolemy II specific version of Diva
#
# @Version: $Id: makefile 62518 2011-12-07 02:35:10Z cxh $
# @Authors: Christopher Hylands (makefile only)
#
# @Copyright (c) 2003-2006 The Regents of the University of California.
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

ME =		diva/util/java2d

# PTSVG_DIR is set to svg in $PTII/mk/ptII.mk by configure
# if Batik SVG jar files were found
DIRS =		$(PTSVG_DIR) test

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	java2d
PTCLASSJAR =	$(PTPACKAGE).jar

# FIXME: since svg is optional, we can't easily include
# it here.
# Include the .class files from these jars in PTCLASSALLJAR
#PTCLASSALLJARS =
#	#svg/svg.jar
#
#PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	AbstractPaintedGraphic.java \
	PaintedGraphic.java \
	PaintedImage.java \
	PaintedList.java \
	PaintedObject.java \
	PaintedPath.java \
	PaintedShape.java \
	PaintedString.java \
	Polygon2D.java \
	PolygonIterator.java \
	Polyline2D.java \
	PolylineIterator.java \
	ShapeUtilities.java

EXTRA_SRCS =	$(JSRCS) \
	package.html

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	svg \
	$(PTCLASSJAR) \
	$(PTCLASSALLJAR) \
	'Polygon2D$$Double.class' \
	'Polygon2D$$Float.class' \
	'Polyline2D$$Double.class' \
	'Polyline2D$$Float.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
