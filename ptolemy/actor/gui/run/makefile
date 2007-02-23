# Makefile for Ptolemy run control panel classes
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2006 The Regents of the University of California.
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

ME =		ptolemy/actor/gui/run

# PTJACL_DIR is set to ptjacl by configure in $PTII/mk/ptII.mk if
# ptjacl.jar was found and the Ptolemy Tcl implementation is available

# $PTII/configure looks for Jython and sets PTJYTHON_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJYTHON_DIR is empty.

DIRS =

# Root of the Java directory
ROOT =		../../../..

# PlotTableauFrame.java requires diva.jar
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	run
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
# We do not include ptjacl/ptjacl.jar, it gets included separately 
# because it requires lib/ptjacl.jar
PTCLASSALLJARS = 

PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
# Except: NOTE: HTMLEffigy has to come before Configuration
# because its inner class, HTMLEffigy$Factory, is referenced.
JSRCS = \
	CustomizableRunPane.java \
	InterfaceTableau.java

# Files to be included in the jar file
OTHER_FILES_TO_BE_JARED =

# Note that PtolemyIISmallIcon.gif is also in doc/img.
# We place a duplicate copy here to make it easy to ship
# jar files that contain all the appropriate images.
EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass

TableauFrame.class: HTMLEffigy.class

install: jclass jars

demo: all demo_ptolemy demo_vergil demo_caa
demo_ptolemy: all
	$(ROOT)/bin/ptolemy $(ROOT)/ptolemy/moml/demo/modulation.xml
demo_vergil: all
	$(ROOT)/bin/vergil $(ROOT)/ptolemy/moml/demo/modulation.xml

# Demonstrate CompositeActorApplication
demo_caa: all
	CLASSPATH="$(ROOT)$(CLASSPATHSEPARATOR)" \
	 "$(JAVA)" ptolemy.actor.gui.CompositeActorApplication \
	 -class ptolemy.domains.sdf.demo.Butterfly.Butterfly

# Demonstrate MoMLSimpleApplication, which does not necessarily
# require graphics, though this demo does use the
demo_momlsimple:
	CLASSPATH="$(ROOT)$(CLASSPATHSEPARATOR)" \
	 "$(JAVA)" ptolemy.actor.gui.MoMLSimpleApplication \
		$(ROOT)/ptolemy/actor/gui/test/test.xml

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
