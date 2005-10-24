# Makefile for Full Screen demos
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2001-2005 The Regents of the University of California.
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

ME =		ptolemy/apps/fullscreen/sun

# Root of the Java directory
ROOT =		../../../..

CLASSPATH = 	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

DIRS =

# Keep this list alphabetized.
JSRCS = \
	CapabilitiesTest.java \
	DisplayModeTest.java \
	GraphicsConfigurationTest.java \
	MultiBufferTest.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) cg

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

JAVAC =		/cygdrive/c/j2sdk1.4.0-beta3/bin/javac
JAVA =		/cygdrive/c/j2sdk1.4.0-beta3/bin/java

all: jclass
	@echo "To run the demo, run 'make demo'"
	@echo "http://java.sun.com/docs/books/tutorial/extra/fullscreen/index.html"

install: all

demo: demo1 demo2 demo3 demo4 demo5
demo1: CapabilitiesTest.class
	$(JAVA) -classpath . CapabilitiesTest

demo2: DisplayModeTest.class
	$(JAVA) -classpath . DisplayModeTest

demo3: MultiBufferTest.class
	$(JAVA) -classpath . MultiBufferTest

demo4: GraphicsConfigurationTest.class
	$(JAVA) -classpath . GraphicsConfigurationTest

#See http://java.sun.com/products/jfc/tsc/articles/timer/
#demo5: FadeDemo.class
#	$(JAVA) -classpath . FadeDemo

# http://java.sun.com/products/java-media/2D/samples/suite/Composite/FadeAnim.java
demo6: FadeAnim.class
	$(JAVA) -classpath . FadeAnim

#See http://java.sun.com/docs/books/tutorial/2d/display/compositing.html 
demo7: Composite.class
	$(JAVA) -classpath . Composite

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
