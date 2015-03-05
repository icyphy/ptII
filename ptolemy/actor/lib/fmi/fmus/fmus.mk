# Makefile stub used to create fmus
#
# @Author: Christopher Brooks (makefile only)
#
# @Version: $Id$
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

# The fmus in the fmus/ directory should have a makefile that looks like
# FMU_NAME = dqME1
# include ../fmus.mk


DIRS =

# Root of the Ptolemy II directory
ROOT =		../../../../../..

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
	$(FMU_NAME).fmu

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)


# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	fmuCheck \
	src

JCLASS = $(JSRCS:%.java=%.class)

FMU_SRCS = \
	src/documentation/* \
	src/modelDescription.xml \
	src/model.png \
	src/sources/* \

KRUFT = src/binaries $(FMU_NAME).fmu

all: jclass $(FMU_NAME).fmu


install: jclass $(FMU_NAME).fmu jars
	@echo "Optionally run 'make update' to update the test/auto directory."


$(FMU_NAME).fmu: $(FMU_SRCS)
	(cd src/sources; $(MAKE))

# Test the FMU by running fmucheck.
fmuCheck: $(FMU_NAME).fmu
	if [ -f fmuCheck/input.csv ]; then \
		fmuCheck -i fmuCheck/input.csv -o fmuCheck/result.csv -h 1 -s 10 $(FMU_NAME).fmu; \
	else \
		fmuCheck -h 1 -s 10 $(FMU_NAME).fmu; \
	fi

test_me:
	$(JAVA) -classpath $(ROOT)$(CLASSPATHSEPARATOR)$(JNA_JAR) org.ptolemy.fmi.driver.FMUModelExchange $(FMU_NAME).fmu  1.0 0.1 true

test_cs:
	$(JAVA) -classpath $(ROOT)$(CLASSPATHSEPARATOR)$(JNA_JAR) org.ptolemy.fmi.driver.FMUCoSimulation $(FMU_NAME).fmu  1.0 0.1 true

# We don't check in the fmu because it will be different on each platform
# Instead, run make update to update the test directory.
update: $(FMU_NAME).fmu
	if [  -f ../../test/auto/$(FMU_NAME).fmu ]; then  \
	    echo "Updating ../../test/auto/$(FMU_NAME).fmu"; \
	    mv $(FMU_NAME).fmu $(FMU_NAME).new.fmu; \
	    cp ../../test/auto/$(FMU_NAME).fmu .; \
	    echo "Sleeping, then touching $(FMU_NAME).new.fmu to be sure it is the most recent fmu."; \
	    sleep 1; \
	    touch $(FMU_NAME).new.fmu; \
	    $(ROOT)/bin/updateFmu $(FMU_NAME).fmu; \
	    rm $(FMU_NAME).new.fmu; \
        fi 
	cp $(FMU_NAME).fmu ../../test/auto/$(FMU_NAME).fmu

# Check for memory leaks
VALGRIND = valgrind
valgrind:
	if [ -f fmuCheck/input.csv ]; then \
		$(VALGRIND) fmuCheck -i fmuCheck/input.csv -o fmuCheck/result.csv -h 1 -s 10 $(FMU_NAME).fmu; \
	else \
		$(VALGRIND) fmuCheck -h 1 -s 10 $(FMU_NAME).fmu; \
	fi

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
