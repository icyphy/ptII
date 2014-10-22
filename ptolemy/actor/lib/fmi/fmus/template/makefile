# Makefile FMU template files used to build fmus
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

ME =		ptolemy/actor/lib/fmi/fmus/template

DIRS =		fmiv20RC1 fmiv20RC1pt fmiv20 sources


# Root of the Ptolemy II directory
ROOT =		../../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	template
# PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	fmiv20RC1/fmiv20RC1.jar \
	fmiv20RC1pt/fmiv20RC1pt.jar \
	fmiv20/fmiv20.jar \
	sources/sources.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS =

OTHER_FILES_TO_BE_JARED = \
	mkfmudir \
	model.png

EXTRA_SRCS = $(OTHER_FILES_TO_BE_JARED)	$(JSRCS)

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

# Update the fmus from fmusdk
FMUSDK=$(HOME)/src/fmi/fmusdk2/fmu20/src/models
MODELS=bouncingBall dq inc values vanDerPol
#MODELS=bouncingBall
updateFromFmuSdk:
	set $(MODELS); \
	for x do \
		echo "Creating $${x}20"; \
		cd $(ROOT)/ptolemy/actor/lib/fmi/fmus/; ./template/mkfmudir $${x}20; \
		cp $(FMUSDK)/$$x/_main.html $${x}20/src/documentation; \
		cp $(FMUSDK)/$${x}/*.PNG $${x}20/src/; \
		cp $(FMUSDK)/$${x}/*.png $${x}20/src/; \
		sed "s/$$x/$${x}20/" < $(FMUSDK)/$${x}/$${x}.c > $${x}20/src/sources/$${x}20.c; \
		sed "s/$$x/$${x}20/" < $(FMUSDK)/$${x}/modelDescription_cs.xml > $${x}20/src/modelDescription.xml; \
		echo "Creating $${x}ME20"; \
		./template/mkfmudir $${x}ME20; \
		cp $(FMUSDK)/$$x/_main.html $${x}ME20/src/documentation; \
		cp $(FMUSDK)/$${x}/*.PNG $${x}ME20/src/; \
		cp $(FMUSDK)/$${x}/*.png $${x}ME20/src/; \
		sed "s/$$x/$${x}ME20/" < $(FMUSDK)/$${x}/$${x}.c > $${x}ME20/src/sources/$${x}ME20.c; \
		sed "s/$$x/$${x}ME20/" < $(FMUSDK)/$${x}/modelDescription_me.xml > $${x}ME20/src/modelDescription.xml; \
		sed -e 's/\(INCLUDE = -DFMI_COSIMULATION -I.\)/#\1/' \
		    -e 's/#\(INCLUDE =  -I.\)/\1/' < $${x}ME20/src/sources/fmu.mk > $${x}ME20/src/sources/fmu.mk.new; \
		mv $${x}ME20/src/sources/fmu.mk.new $${x}ME20/src/sources/fmu.mk; \
		sed -e 's/\(include="-DFMI_COSIMULATION -I."\)/#\1/' \
		    -e 's/#\(include="-I."\)/\1/' < $${x}ME20/src/sources/build_fmu > $${x}ME20/src/sources/build_fmu.new; \
		mv $${x}ME20/src/sources/build_fmu.new $${x}ME20/src/sources/build_fmu; \
		sed -e 's@\(cl /LD /wd4090 /nologo /DFMI_COSIMULATION ..\%1.c /I ..\.\)@cl /LD /wd4090 /nologo ..\%1.c /I ..\.@' < $${x}ME20/src/sources/build_fmu.bat > $${x}ME20/src/sources/build_fmu.bat.new; \
		mv $${x}ME20/src/sources/build_fmu.bat.new $${x}ME20/src/sources/build_fmu.bat; \
		cd template; \
	done;

buildFromFmuSdk:
	set $(MODELS); \
	for x do \
	    (cd ../$${x}20; make); \
	    (cd ../$${x}ME20; make); \
	done;

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
