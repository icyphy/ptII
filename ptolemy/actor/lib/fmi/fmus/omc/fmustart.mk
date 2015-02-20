# Stub makefile for fmus.  We split fmus.mk up so as to support different builds.
#
# @Author: Christopher Brooks (makefile only)
#
# @Version: $Id: fmus.mk 71624 2015-02-19 00:50:20Z mwetter@lbl.gov $
#
# @Copyright (c) 2013-2014 The Regents of the University of California.
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

# This file is shared between OpenModelica Sparse FMI (sfmi) and
# OpenModelica Cosimulation (cs) fmusfmi.mk and fmics.mk include this
# file and then define a rule to build the .fmu file.

DIRS =

# Root of the Ptolemy II directory
ROOT =		$(PTII)

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	$(FMU_NAME)
PTCLASSJAR =	$(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
# PTCLASSALLJARS =
# PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS =

OTHER_FILES_TO_BE_JARED = \
	$(OTHER_OTHER_FILES_TO_BE_JARED) \
	$(FMU_NAME).fmu

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

KRUFT = $(FMU_NAME).fmu

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)


# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	src

JCLASS = $(JSRCS:%.java=%.class)


#	src/model.png
#	src/documentation/* 

all: jclass $(FMU_NAME).fmu

install: jclass $(FMU_NAME).fmu jars
	@echo "Optionally run 'make update' to update the test/auto directory."


# Test the FMU by running fmucheck.
fmuCheck: $(FMU_NAME).fmu
	if [ -f fmuCheck/input.csv ]; then \
		fmuCheck -i fmuCheck/input.csv -o fmuCheck/result.csv -h 1 -s 10 $(FMU_NAME).fmu; \
	else \
		fmuCheck -l 5 $(FMU_NAME).fmu; \
	fi
# We don't check in the fmu because it will be different on each platform
# Instead, run make update to update the test directory.
# The JModelica Tests are in a separate directory.
update: $(FMU_NAME).fmu
	if [  -f ../test/auto/$(FMU_NAME).fmu ]; then  \
	    mv $(FMU_NAME).fmu $(FMU_NAME).new.fmu; \
	    cp ../test/auto/$(FMU_NAME).fmu .; \
	    echo "Sleeping, then touching $(FMU_NAME).new.fmu to be sure it is the most recent fmu."; \
	    sleep 1; \
	    touch $(FMU_NAME).new.fmu; \
	    $(ROOT)/bin/updateFmu $(FMU_NAME).fmu; \
	    rm $(FMU_NAME).new.fmu; \
        fi 
	cp $(FMU_NAME).fmu ../test/auto/$(FMU_NAME).fmu

test:
	$(JAVA) -classpath $(ROOT)$(CLASSPATHSEPARATOR)$(JNA_JAR) org.ptolemy.fmi.driver.FMUModelExchange $(FMU_NAME).fmu  1.0 0.1 true

