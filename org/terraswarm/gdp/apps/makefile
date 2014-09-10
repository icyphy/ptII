# Makefile org.terraswarm Global Data Plane applications
#
# @Version: $Id: makefile 69602 2014-07-30 14:20:15Z cxh $
# @Author: Christopher Brooks (makefile only)
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
ME =		org.terraswarm.gdp.apps

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(JNA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	apps
#PTCLASSJAR =	$(PTPACKAGE).jar
#PTCLASSALLJAR = $(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS =

JSRCS = \
	WriterTest.java

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

all: suball
install: subinstall

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(DIRS)

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

run: WriterTest.class
	DYLD_LIBRARY_PATH=${PTII}/org/terraswarm/gdp/src/gdp/libs:/usr/local/lib LD_LIBRARY_PATH=${PTII}/org/terraswarm/gdp/src/gdp/libs:/usr/local/lib $(JAVA) -Djna.debug_load=true -Djna.dump_memory=true -classpath $(CLASSPATH) org.terraswarm.gdp.apps.WriterTest -D gdp.api=100

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
