# Makefile for the Vergil/icon package
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2000-2003 The Regents of the University of California.
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


ME =		ptolemy/vergil/icon

DIRS =

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	icon
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	AttributeValueIcon.java \
	BoxedValueIcon.java \
	BoxedValuesIcon.java \
	CopyCatIcon.java \
	EditorIcon.java \
	ResizableAttributeControllerFactory.java \
	ShapeIcon.java \
	SVGIcon.java \
	TextIcon.java \
	UpdatedValueIcon.java \
	ValueIcon.java \
	XMLIcon.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	lib \
	test \
	gifs \
	$(PTCLASSJAR) \
	'IconEditor$$1.class' \
	'IconEditor$$10.class' \
	'IconEditor$$11.class' \
	'IconEditor$$12.class' \
	'IconEditor$$13.class' \
	'IconEditor$$14.class' \
	'IconEditor$$15.class' \
	'IconEditor$$16.class' \
	'IconEditor$$17.class' \
	'IconEditor$$18.class' \
	'IconEditor$$19.class' \
	'IconEditor$$2.class' \
	'IconEditor$$20.class' \
	'IconEditor$$21.class' \
	'IconEditor$$22.class' \
	'IconEditor$$23.class' \
	'IconEditor$$24.class' \
	'IconEditor$$25.class' \
	'IconEditor$$26.class' \
	'IconEditor$$27.class' \
	'IconEditor$$3.class' \
	'IconEditor$$4.class' \
	'IconEditor$$5.class' \
	'IconEditor$$6.class' \
	'IconEditor$$7.class' \
	'IconEditor$$8.class' \
	'IconEditor$$9.class' \
	'IconEditor$$BlockIcon.class' \
	'UpdatedValueIcon$$1.class'

JCLASS = $(JSRCS:%.java=%.class)

all: \
	jclass
install: jclass $(PTCLASSJAR)

demo:
	java -Djava.compiler=NONE -classpath "$(CLASSPATH)" ptolemy.vergil.icon.IconEditor

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
