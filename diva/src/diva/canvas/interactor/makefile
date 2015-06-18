# Makefile for canvas/interactor classes - Ptolemy II specific version of Diva
#
# @Version: $Id: makefile 36422 2005-02-28 19:19:13Z cxh $
# @Authors: Christopher Hylands (makefile only)
#
# @Copyright (c) 2003-2005 The Regents of the University of California.
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

ME =		diva/canvas/interactor

DIRS =

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	interactor
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS = \
	AbstractInteractor.java \
	ActionInteractor.java \
	BasicGrabHandle.java \
	BasicGrabHandleFactory.java \
	BasicSelectionModel.java \
	BasicSelectionRenderer.java \
	BoundedDragInteractor.java \
	BoundsConstraint.java \
	BoundsGeometry.java \
	BoundsManipulator.java \
	CircleGeometry.java \
	CircleManipulator.java \
	CompositeInteractor.java \
	DragInteractor.java \
	Geometry.java \
	GrabHandle.java \
	GrabHandleFactory.java \
	Interactor.java \
	Manipulator.java \
	MoveHandle.java \
	PathGeometry.java \
	PathManipulator.java \
	PointConstraint.java \
	QuadrantConstraint.java \
	SelectionDragger.java \
	SelectionEvent.java \
	SelectionInteractor.java \
	SelectionListener.java \
	SelectionModel.java \
	SelectionRenderer.java \
	ShapedFigure.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	$(PTCLASSJAR) \
	$(PTCLASSALLJAR) \
	'BoundsGeometry$$1.class' \
	'BoundsGeometry$$BoundsSite.class' \
	'BoundsManipulator$$1.class' \
	'BoundsManipulator$$Resizer.class' \
	'CircleGeometry$$1.class' \
	'CircleGeometry$$CircleSite.class' \
	'CircleManipulator$$1.class' \
	'CircleManipulator$$Resizer.class' \
	'package.html PathGeometry$$1.class' \
	'PathGeometry$$CloseSegment.class' \
	'PathGeometry$$Vertex.class' \
	'PathManipulator$$1.class' \
	'PathManipulator$$Resizer.class' \
	'SelectionEvent$$1.class' \
	'SelectionEvent$$2.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
