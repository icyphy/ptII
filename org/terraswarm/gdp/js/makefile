# A JavaScript interface to the TerraSwarm Global Data Plane
#
# @Version: $Id: makefile 70349 2014-10-12 22:45:57Z cxh $
# @Author: Christopher Brooks
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
##########################################################################

DIRS = 

# Root of Ptolemy II directory
ROOT =		../../../..

# $(ME) is used in ptdir.mk
ME =		org.terraswarm.gdp.js

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(JNA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	js
PTCLASSJAR =	$(PTPACKAGE).jar
#PTCLASSALLJAR = $(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \

JSRCS =

OTHER_FILES_TO_BE_JARED = \
	simpleReader.js

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

JCLASS = $(JSRCS:%.java=%.class)

all: jclass 
	@echo "To run a simpleReader demo, try 'make run'"
install: jclass jars

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES = #src

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(DIRS)

JCLASS = $(JSRCS:%.java=%.class)

KRUFT = node_modules

NODE = node

# Node modules necessary to run the JavaScript interface.
NODE_MODULES = node_modules/ffi node_modules/ref node_modules/ref-array

run: $(NODE_MODULES)
	@echo "This test reads the last item in the gcl, so we prime the gcl."
	cp gcl/* /var/tmp/gcl
	@echo "Make sure that ../src/gdp/gdpd/gdpd & is running in the background."
	$(NODE) simpleReader.js

node_modules/ffi:
	npm install ffi
node_modules/ref:
	npm install ref
node_modules/ref-array:
	npm install ref-array

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
