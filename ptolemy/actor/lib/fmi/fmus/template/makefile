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

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS =	$(JSRCS)

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
FMUSDK=$(HOME)/src/fmi/fmusdk2
MODELS=bouncingBall dq inc values vanDerPol
updateFromFmuSdk:
	set $(MODELS); \
	for x do \
		echo "Creating $${x}20";
		cd $(ROOT)/ptolemy/actor/lib/fmi/fmus/; $(ROOT)/ptolemy/actor/lib/fmi/fmus/template/mkfmudir $${x}20; 
		cp $(FMUSDK)/$$x/_main.html $${x}20/src/documentation; \
		if [ -f $(FMUSDK)/$${x}/*.PNG ]; then \
			cp $(FMUSDK)/$${x}/*.PNG $${x}20/src/; \
		fi; \
		if [ -f $(FMUSDK)/$${x}/*.png ]; then \
			cp $(FMUSDK)/$${x}/*.png $${x}20/src/; \
		fi; \
		cp $(FMUSDK)/$${x}/$${x}.c $${x}20/src/sources; \
		cp $(FMUSDK)/$${x}/modelDescription_cs.c $${x}20/src/modelDescription.xml; \
		echo "Creating $${x}ME20";
		cd $(ROOT)/ptolemy/actor/lib/fmi/fmus/; $(ROOT)/ptolemy/actor/lib/fmi/fmus/template/mkfmudir $${x}ME20; 
		cp $(FMUSDK)/$$x/_main.html $${x}ME20/src/documentation; \
		if [ -f $(FMUSDK)/$${x}/*.PNG ]; then \
			cp $(FMUSDK)/$${x}/*.PNG $${x}ME20/src/; \
		fi; \
		if [ -f $(FMUSDK)/$${x}/*.png ]; then \
			cp $(FMUSDK)/$${x}/*.png $${x}ME20/src/; \
		fi; \
		cp $(FMUSDK)/$${x}/$${x}.c $${x}ME20/src/sources; \
		cp $(FMUSDK)/$${x}/modelDescription_me.c $${x}ME20/src/modelDescription.xml; \
	done;

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
