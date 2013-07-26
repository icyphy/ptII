# Makefile for Java Ptolemy II Functional Mock-up Unit (FMU) vanDerPol Model Exchange
#
# @Author: Christopher Brooks (makefile only)
#
# @Version: $Id: makefile 66133 2013-04-25 21:59:22Z cxh $
#
# @Copyright (c) 2013 The Regents of the University of California.
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

ME =		ptolemy/actor/lib/fmi/fmus/vanDerPolME

DIRS =

# Root of the Ptolemy II directory
ROOT =		../../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	vanDerPolME
PTCLASSJAR =	$(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
# PTCLASSALLJARS =
# PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS =

OTHER_FILES_TO_BE_JARED = \
	vanDerPolME.fmu

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

KRUFT = vanDerPolME.fmu

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)


# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	src

JCLASS = $(JSRCS:%.java=%.class)

FMU_SRCS = \
	src/documentation/* \
	src/modelDescription.xml \
	src/model.png \
	src/sources/* \

all: jclass vanDerPolME.fmu


install: jclass vanDerPolME.fmu jars
	@echo "Optionally run 'make update' to update the test/auto directory."

vanDerPolME.fmu: $(FMU_SRCS)
	(cd src/sources; $(MAKE))

# We don't check in vanDerPolME.fmu because it will be different on each platform
# Instead, run make update to update the test directory.
update: vanDerPolME.fmu
	cp vanDerPolME.fmu ../../test/auto/vanDerPolME.fmu 

test:
	$(JAVA) -classpath $(ROOT)$(CLASSPATHSEPARATOR)$(JNA_JAR) org.ptolemy.fmi.driver.FMUModelExchange vanDerPolME.fmu

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
