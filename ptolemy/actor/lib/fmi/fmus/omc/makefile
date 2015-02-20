# Makefile for Java Ptolemy II Functional Mock-up Interface (FMI) FMU files
#
# @Author: Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2014 The Regents of the University of California.
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

ME =		ptolemy/actor/lib/fmi/fmus/omc

# Co-Simulation FMUS.
FMUS_CS =	BouncingBall HeatConductorComplex

# Sparse FMI FMUS
FMUS_SFMI =	HeatConductor Influenza Linsys Linsys2 Test1

# Model Exchange FMUS.
FMUS_ME =	$(FMUS_SFMI)

# All FMUs.
FMUS = $(FMUS_CS) $(FMUS_ME)

DIRS =		$(FMUS) test


# Root of the Ptolemy II directory
ROOT =		../../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	fmus
# PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	Influenza/Influenza.jar \
	Test1/Test1.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS =

OTHER_FILES_TO_BE_JARED = \
	fmus.mk \
	README.txt

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc

JCLASS = $(JSRCS:%.java=%.class)

all: suball 
install: subinstall jars

# Update the .fmu files in the test/auto/directory.
update:
	@if [ "x$(FMUS)" != "x" ]; then \
		set $(FMUS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making update in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) update ;\
			) \
		    fi ; \
		done ; \
	fi

# Update sfmi_runtime.h and sfmi_runtime.cpp.
SFMI_RUNTIME=$(HOME)/src/fmu/sparse_fmi/runtime
update_sfmi:
	@if [ "x$(FMUS_SFMI)" != "x" ]; then \
		set $(FMUS_SFMI); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo "$$x: Copying sfmi_runtime.h and sfmi_runtime.cpp from $(SFMI_RUNTIME)"; \
			cp $(SFMI_RUNTIME)/sfmi_runtime.h src/sources/; \
			cp $(SFMI_RUNTIME)/sfmi_runtime.cpp src/sources/; \
			echo "$$x: Copying ../template/sources/fmu.mk"; \
			cp ../template/sources/fmu.mk src/sources; \
			) \
		    fi ; \
		done ; \
	fi


# Update fmu.mk, build_fmu and build_fmu.bat from the files in the template/ directory.
update_files:
	@if [ "x$(FMUS_CS)" != "x" ]; then \
		set $(FMUS_CS); \
		for x do \
			echo "copying ../template/sources/fmu.mk to $$x/src/sources"; \
			cp ../template/sources/fmu.mk $$x/src/sources/; \
			echo "copying ../template/sources/build_fmu to $$x/src/sources"; \
			cp ../template/sources/build_fmu $$x/src/sources/; \
			echo "copying ../template/sources/build_fmu.bat to $$x/src/sources"; \
			cp ../template/sources/build_fmu.bat $$x/src/sources/; \
		done ; \
	fi

	@if [ "x$(FMUS_ME)" != "x" ]; then \
		set $(FMUS_ME); \
		for x do \
			echo "copying ../template/sources/fmu.mk to $$x/src/sources and updating for ME"; \
			sed 's@#include modelExchange.mk@include modelExchange.mk@'../template/sources/fmu.mk > $$x/src/sources/fmu.mk; \
			echo "copying ../template/sources/build_fmu to $$x/src/sources and updating for ME"; \
			sed -e 's@include="-DFMI_COSIMULATION -I."@#include="-DFMI_COSIMULATION -I."@' \
				-e 's@#include="-I."@include="-I."@'../template/sources/build_fmu > $$x/src/sources/build_fmu.mk; \
			echo "copying ../template/sources/build_fmu.bat to $$x/src/sources and updating for ME"; \
			sed -e 's@FMI for Co-Simulation 1.0@FMI for Model Exchange 1.0@' \
				-e 's@/DFMI_COSIMULATION@@'../template/sources/build_fmu.bat > $$x/src/sources/build_fmu.bat; \
			echo "copyng ../template/sources/fmuTemplate.c to $$x/src/sources"; \
			cp ../template/sources/fmuTemplate.c $$x/src/sources/fmuTemplate.c; \
		done ; \
	fi

fmucheck:
	@if [ "x$(FMUS_CS)" != "x" ]; then \
		set $(FMUS_CS); \
		for x do \
			echo "Running fmucheck on $$x/$$x.fmu"; \
			(cd $$x; fmuCheck $$x.fmu); \
		done ; \
	fi
	@if [ "x$(FMUS_ME)" != "x" ]; then \
		set $(FMUS_ME); \
		for x do \
			echo $$x/$$x.fmu; \
			(cd $$x; fmuCheck $$x.fmu); \
		done ; \
	fi

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
