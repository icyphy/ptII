# Makefile org.terraswarm Global Data Plane sources
#
# @Version: $Id: makefile 69602 2014-07-30 14:20:15Z cxh $
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
ROOT =		../../..

# $(ME) is used in ptdir.mk
ME =		org.terraswarm.gdp

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(JNA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	gdp
#PTCLASSJAR =	$(PTPACKAGE).jar
#PTCLASSALLJAR = $(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS =

JSRCS = \
	GdpLibrary.java

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

KRUFT = .cpp *.c *.choices

JNA_JAR = jnaerator-0.11-shaded.jar

all: jclass
install: jclass jars

# Rebuild the jar file
jna: $(JNA_JAR) src/gdp
	java -jar $(JNA_JAR) -library gdp -I src/gdp src/gdp/gdp/gdp.h -o $(PTII) -package org.terraswarm.gdp  -v  -noJar -noComp

src/gdp:
	mkdir src
	cd src; git clone https://repo.eecs.berkeley.edu/git/projects/swarmlab/gdp.git

jnaerator-0.11-shaded.jar:
	# You will need a password for this, ask Eric.
	wget http://search.maven.org/remotecontent?filepath=com/nativelibs4java/jnaerator/0.11/jnaerator-0.11-shaded.jar
	mv 'remotecontent?filepath=com%2Fnativelibs4java%2Fjnaerator%2F0.11%2Fjnaerator-0.11-shaded.jar' $@





# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
