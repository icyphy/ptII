# Makefile for TerraSwarm Accessors
#
# @Version: $Id$
# @Author: Christopher Brooks
#
# @Copyright (c) 2015 The Regents of the University of California.
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

DIRS = demo test

# Root of Ptolemy II directory
ROOT =		../../..

JSRCS = 

# $(ME) is used in ptdir.mk
ME =		org/terraswarm/accessor

# AccessorOne.java depends on JavaScript.java, which uses these
# libraries.  If we run 'make fast', and JavaScript.class does not
# exist, then we will need these jars in the classpath.
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(PTOAUTHCLIENT_JAR)$(CLASSPATHSEPARATOR)$(PTOAUTHCOMMON_JAR)$(CLASSPATHSEPARATOR)$(PTSOCKETIO_JAR)$(CLASSPATHSEPARATOR)$(PTRTOOL_JARS)

# True source directory
VPATH =		$(ROOT)/$(ME)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	accessor
PTCLASSJAR =	$(PTPACKAGE).jar
#PTCLASSALLJAR = $(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS =

JSRCS = \
	AccessorIconLoader.java \
	AccessorLibrary.java \
	JSAccessor.java

OTHER_FILES_TO_BE_JARED = \
	accessors \
	JSAccessorIcon.xml \
	XMLJSToMoML.xslt

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

JCLASS = $(JSRCS:%.java=%.class)

all: jclass accessors
install: jclass accessors jars

accessors:
	if [ ! -d accessors ] ; then \
		svn co https://repo.eecs.berkeley.edu/svn/projects/terraswarm/accessors/trunk/accessors; \
	else \
		(cd accessors; svn update); \
	fi

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	'AccessorIconLoader$$1.class' \
	'JSAccessor$$1.class' \
	'JSAccessor$$2.class' \
        'JSAccessor$$ActionableAttribute.class'

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
