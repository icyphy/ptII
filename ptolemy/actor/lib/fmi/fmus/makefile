# Makefile for Java Ptolemy II Functional Mock-up Interface (FMI) FMU files
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

ME =		ptolemy/actor/lib/fmi/fmus

# Co-simulation FMUS.

# Co-simulation FMUS from FMUSDK2.0.3.  These fmus should not be modified, instead copy them and change the copy.
FMUS_1_0_CS = bouncingBall dq inc values vanDerPol

# Co-simulation FMUs that have a modelDescription.xml file that has fmiVersion < 2.0.  Typically, this is 1.0 and 1.5.
# 1.5 is the Ptolemy-specific prototypes.
FMUS_PRE_2_0_CS =	$(FMUS_1_0_CS) ballDynamics helloWorld levelStairs onOffSupervisor \
		stairsA stairsB stepCounter1 stepCounter stepCounterExtendedFMI stepCounterPeriodInName 

# Co-simulation FMUs that have have fmiVersion >= 2.0
# New Co-simulation FMUS should be added here.
FMUS_2_0_CS =	bouncingBall20 bouncingBall20RC1 \
		dq20 dq20RC1 heaterFlowResistance \
		inc20 inc20RC1 inc20RC1pt \
		output20RC1pt scale20RC1pt \
		scale20RC1 stairsA20RC1F pumpConstantSpeed tankOpen \
		values20 values20RC1 \
		vanDerPol20 vanDerPol20RC1 floor20pt\
		inc20pt out20pt fmuA20pt scale20pt fmuB20pt fmuC20pt \
		fmuD20pt

FMUS_CS =	$(FMUS_PRE_2_0_CS) $(FMUS_2_0_CS)

## Model Exchange FMUS.

# FMUs that have a modelDescription.xml file that has fmiVersion < 2.0.  Typically, this is 1.0 and 1.5.
# 1.5 is the Ptolemy-specific prototypes.
FMUS_1_0_ME = bouncingBallME1 dqME1 incME1 valuesME1 vanDerPolME1 
FMUS_PRE_2_0_ME = heaterFlowResistance pumpConstantSpeed tankOpen 

# FMUs that have have fmiVersion >= 2.0
# New Model Exchange FMUS should be added here.
FMUS_2_0_ME = bouncingBallME20 dqME20 helloWorldME2 incME20 valuesME20 vanDerPolME20

FMUS_ME =	$(FMUS_1_0_ME) $(FMUS_PRE_2_0_ME) $(FMUS_2_0_ME)


# All FMUs.
FMUS = $(FMUS_CS) $(FMUS_ME)

# FMI 2.0 FMUS
FMUS_2_0 = $(FMUS_2_0_CS) $(FMUS_2_0_ME)


# PTJMODELICA_DIR is set to jmodelica if $PTII/configure find jmi.h
DIRS =		fmusim2 template win32 $(FMUS) $(PTJMODELICA_DIR)


# Root of the Ptolemy II directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	fmus
# PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	bouncingBallME1/bouncingBallME1.jar \
	dqME1/dqME1.jar \
	helloWorldME2/helloWorldME2.jar \
	incME1/incME1.jar \
	tankOpen/tankOpen.jar \
	pumpConstantSpeed/pumpConstantSpeed.jar \
	heaterFlowResistance/heaterFlowResistance.jar \
	valuesME1/valuesME1.jar \
	vanDerPolME1/vanDerPolME1.jar \
	ballDynamics/ballDynamics.jar \
	helloWorld/helloWorld.jar \
	levelStairs/levelStairs.jar \
	onOffSupervisor/onOffSupervisor.jar \
	scale20RC1/scale20RC1.jar \
	stairsA/stairsA.jar \
	stairsB/stairsB.jar \
	stairsA20RC1F/stairsA20RC1F.jar \
	stepCounter/stepCounter.jar \
	stepCounter1/stepCounter1.jar \
	stepCounterExtendedFMI/stepCounterExtendedFMI.jar \
	stepCounterPeriodInName/stepCounterPeriodInName.jar \
	template/template.jar \
	win32/win32.jar

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
	doc \
	omc \
	tulip

JCLASS = $(JSRCS:%.java=%.class)

all: suball 
install: subinstall jars

# Update the .fmu files in the ../test/auto/ directory.
# This run unzips the .fmu files in ../test/auto/ and updates any changed files.
#
# If any of the non-shared library files change, then remove the .fmu file
# from test/auto/ and run 'make update' for each platform
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

update_1_0_CS:
	@if [ "x$(FMUS_1_0_CS)" != "x" ]; then \
		set $(FMUS_1_0_CS); \
		for x do \
			(cd $$x; make update;) \
		done ; \
	fi

# Update the .fmu files in the ../test/auto/directory.
update_1_0_ME:
	@if [ "x$(FMUS_1_0_ME)" != "x" ]; then \
		set $(FMUS_1_0_ME); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making update in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) clean update ;\
			) \
		    fi ; \
		done ; \
	fi


# Update fmu.mk, build_fmu and build_fmu.bat from the files in the template/ directory.
update_files:
	@if [ "x$(FMUS_CS)" != "x" ]; then \
		set $(FMUS_CS); \
		for x do \
			echo "copying template/sources/fmu.mk to $$x/src/sources"; \
			cp template/sources/fmu.mk $$x/src/sources/; \
			echo "copying template/sources/build_fmu to $$x/src/sources"; \
			cp template/sources/build_fmu $$x/src/sources/; \
			echo "copying template/sources/build_fmu.bat to $$x/src/sources"; \
			cp template/sources/build_fmu.bat $$x/src/sources/; \
		done ; \
	fi

	@if [ "x$(FMUS_ME)" != "x" ]; then \
		set $(FMUS_ME); \
		for x do \
			echo "copying template/sources/fmu.mk to $$x/src/sources and updating for ME"; \
			sed 's@#include modelExchange.mk@include modelExchange.mk@' template/sources/fmu.mk > $$x/src/sources/fmu.mk; \
			echo "copying template/sources/build_fmu to $$x/src/sources and updating for ME"; \
			sed -e 's@include="-DFMI_COSIMULATION -I."@#include="-DFMI_COSIMULATION -I."@' \
				-e 's@#include="-I."@include="-I."@' template/sources/build_fmu > $$x/src/sources/build_fmu.mk; \
			echo "copying template/sources/build_fmu.bat to $$x/src/sources and updating for ME"; \
			sed -e 's@FMI for Co-Simulation 1.0@FMI for Model Exchange 1.0@' \
				-e 's@/DFMI_COSIMULATION@@' template/sources/build_fmu.bat > $$x/src/sources/build_fmu.bat; \
			echo "copyng template/sources/fmuTemplate.c to $$x/src/sources"; \
			cp template/sources/fmuTemplate.c $$x/src/sources/fmuTemplate.c; \
		done ; \
	fi

# Update the .h files from the most recent template directory
HFILE_TEMPLATE_DIRECTORY = template/fmiv20RC1pt
#CP_OR_DIFF = diff
CP_OR_DIFF = cp
update_2_0_hfiles:
	@if [ "x$(FMUS_2_0)" != "x" ]; then \
		set $(FMUS_2_0); \
		for x do \
			echo "copying $(HFILE_TEMPLATE_DIRECTORY)/fmiFunctionTypes.h to $$x/src/sources"; \
			$(CP_OR_DIFF) $(HFILE_TEMPLATE_DIRECTORY)/fmiFunctionTypes.h $$x/src/sources; \
			echo "copying $(HFILE_TEMPLATE_DIRECTORY)/fmiFunctions.h to $$x/src/sources"; \
			$(CP_OR_DIFF) $(HFILE_TEMPLATE_DIRECTORY)/fmiFunctions.h $$x/src/sources; \
			echo "copying $(HFILE_TEMPLATE_DIRECTORY)/fmiTypesPlatform.h to $$x/src/sources"; \
			$(CP_OR_DIFF) $(HFILE_TEMPLATE_DIRECTORY)/fmiTypesPlatform.h $$x/src/sources; \
		done ; \
	fi
	echo "Be sure to update the fmus by removing the shared libraries for all platforms with: make clean_2_0; make update_2_0" 

# Remove the FMI 2.0 fmus from the test/auto directory.
# This rule is run when the .h files change
clean_2_0:
	@if [ "x$(FMUS_2_0)" != "x" ]; then \
		set $(FMUS_2_0); \
		for x do \
			echo "Removing ../test/auto/$$x.fmu"; \
			rm ../test/auto/$$x.fmu; \
		done ; \
	fi

update_2_0:
	@if [ "x$(FMUS_2_0)" != "x" ]; then \
		set $(FMUS_2_0); \
		for x do \
			(cd $$x; make update;) \
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

# Make the FMI-1.0 fmus from the FMUSDK.  This gets run rarely.
make_1_0_cs:
	@if [ "x$(FMUS_1_0_CS)" != "x" ]; then \
		set $(FMUS_1_0_CS); \
		for x do \
			echo "Running template/mkfmudir10 $$x"; \
			rm -rf $$x; template/mkfmudir10 $$x; \
		done ; \
	fi


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
