# Makefile for Ptolemy II shallow codegeneration 
#
# @Authors: Steve Neuendorffer, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998-2001 The Regents of the University of California.
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

ME =		ptolemy/copernicus/shallow

# Root of the Java directory
ROOT =		../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

DIRS =		test

# Keep this list alphabetized.
JSRCS = \
	CommandLineTransformer.java \
	EntitySootClass.java \
	InlineParameterTransformer.java \
	Main.java \
	MethodTools.java \
	ModelTransformer.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) cg

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	cg

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To run the demo, run 'make demo'"

install: all

depend:
	@echo "no dependencies in this directory"

demo: compileDemo runDemo
deepDemo: compileDeepDemo runDeepDemo

TARGETPACKAGE = ptolemy.copernicus.shallow.cg
SOURCEPACKAGE = ptolemy.domains.sdf.demo.OrthogonalCom
MODEL = OrthogonalCom 
TARGETCLASS = $(TARGETPACKAGE).ToplevelModel
SOURCECLASS = $(SOURCEPACKAGE).$(MODEL)
JVM_SIZE = -Xmx132m

compileDemo: $(JCLASS)
	@echo "generate code for ptolemy.domains.sdf.demo.OrthogonalCom.OrthogonalCom in cg/"
	$(JAVA) $(JVM_SIZE) \
	 -classpath "$(CLASSPATH)" \
	ptolemy.copernicus.shallow.Main $(SOURCECLASS) -d "$(ROOT)"
	@echo "Here is that javap output"
	javap -verbose -l -s -classpath $(ROOT) $(TARGETCLASS)

runDemo:
	@echo "Run the model"
	$(JAVA) \
	-Xfuture -classpath "$(CLASSPATH)" \
	ptolemy.actor.gui.CompositeActorApplication \
	-iterations 10 \
	-class $(TARGETCLASS)
runDeepDemo:
	@echo "Run the deep model"
	$(JAVA) \
	-Xfuture -classpath "$(CLASSPATH)" \
	$(TARGETPACKAGE).Main

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

