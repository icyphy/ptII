# Ptolemy codegeneration - save as a Java file
#
# @Authors: Shuvra S. Bhattacharyya, Christopher Hylands- based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2000-2001 The Regents of the University of California.
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

ME =		ptolemy/codegen/saveasjava

DIRS =		test

# Root of the Java directory
ROOT =		../../..

# JDK1.2.2 requires that we include $(SOOT_CLASSES) or we get:
#error: Class soot.SceneTransformer not found in nested class ptolemy.copernicus.kernel.KernelMain. _IgnoreAllApplicationClasses.
CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	saveasjava
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	GeneratorTableau.java \
	GeneratorTableauAttribute.java \
	MoMLToJava.java \
	SaveAsJava.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) \
	README.txt

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'GeneratorTableau$$1.class' \
	'GeneratorTableau$$2.class' \
	'GeneratorTableau$$3.class' \
	'GeneratorTableau$$4.class' \
	'GeneratorTableau$$5.class' \
	'GeneratorTableau$$6.class' \
	'GeneratorTableau$$Factory.class' \
	'GeneratorTableau$$GeneratorFrame.class' \
	'GeneratorTableau$$_Exec.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

depend:
	@echo "no dependencies in this directory"

demo: jclass
	CLASSPATH="$(CLASSPATH)" "$(JAVA)" ptolemy.codegen.saveasjava.MoMLToJava test/rampFileWriter.xml
	CLASSPATH="$(CLASSPATH)" "$(JAVAC)" rampFileWriter.java
	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)." "$(JAVA)" ptolemy.actor.gui.CompositeActorApplication -class rampFileWriter -iterations 5

demo_vergil: jclass
	vergil test/rampFileWriter.xml

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk



