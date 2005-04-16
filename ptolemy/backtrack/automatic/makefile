# Makefile for Ptolemy backtracking package
#
# @Version: $Id$
# @Author: Christopher Brooks
#
# @Copyright (c) 2005 The Regents of the University of California
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA  OR RESEARCH IN MOTION
# LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL,
# OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND
# ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA OR
# RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA  AND RESEARCH IN MOTION LIMITED
# HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
##########################################################################

ME =		ptolemy/backtrack/automatic

DIRS =		

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Keep this list alphabetized.
JSRCS = 

OTHER_FILES_TO_BE_JARED = 

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) #$(JGRAPHICAL_TESTS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

# Backtrack.tcl runs "make backtrack" so that we can run the
# tests from inside Eclipse
all: transform build

transform:
	@echo Transform the source files in "source.lst".
	@rm -rf ptolemy
	@"$(JAVA)" \
	  -classpath "$(CLASSPATH)$(CLASSPATHSEPARATOR)$(PTBACKTRACK_JARS)$(AUXCLASSPATH)" \
	  ptolemy.backtrack.ast.Transformer \
	  -prefix ptolemy.backtrack.automatic \
	  -output "$(ROOT)" \
	  -config ptolemy/configs/output.xml \
	  @source.lst

build: ptolemy_makefile
	$(MAKE) -C ptolemy

ptolemy_makefile:
	@echo "Generating ptolemy/makefile..."
	@echo "ME = $(ME)/ptolemy" > ptolemy/makefile
	@echo "DIRS =" >> ptolemy/makefile
	@echo "ROOT = $(ROOT)/.." >> ptolemy/makefile
	@echo "CLASSPATH = \$$(ROOT)" >> ptolemy/makefile
	@echo "CONFIG = \$$(ROOT)/mk/ptII.mk" >> ptolemy/makefile
	@echo "include \$$(CONFIG)" >> ptolemy/makefile
	@echo "PTPACKAGE = ptolemy" >> ptolemy/makefile
	@echo "PTDIST = \$$(PTPACKAGE)\$$(PTVERSION)" >> ptolemy/makefile
	@echo "PTCLASSJAR = \$$(PTPACKAGE).jar" >> ptolemy/makefile
	@echo "JSRCS = \\" >> ptolemy/makefile
	@for filename in `cd ptolemy && find . -name "*.java"`; do \
	  echo "    "$$filename \\ >> ptolemy/makefile; \
	done
	@echo >> ptolemy/makefile
	@echo "OTHER_FILES_TO_BE_JARED =" >> ptolemy/makefile
	@echo "EXTRA_SRCS = \$$(JSRCS) \$$(OTHER_FILES_TO_BE_JARED)" >> ptolemy/makefile
	@echo "MISC_FILES = \$$(DIRS)" >> ptolemy/makefile
	@echo "OPTIONAL_FILES =" >> ptolemy/makefile
	@echo "JCLASS = \$$(JSRCS:%.java=%.class)" >> ptolemy/makefile
	@echo "all: jclass" >> ptolemy/makefile
	@echo "install: jclass \$$(PTCLASSJAR)" >> ptolemy/makefile
	@echo "include \$$(ROOT)/mk/ptcommon.mk" >> ptolemy/makefile

# Remove the ptolemy directory 
# It is generated during the testing, and should not be preserved.
KRUFT = ptolemy

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

