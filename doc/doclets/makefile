# Makefile for the doclets
#
# @Authors: Christopher Hylands Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2004-2012 The Regents of the University of California.
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

ME =		doc/doclets


DIRS =

# Root of the Java directory
ROOT =		../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(TOOLS_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)


# Used to build jar files
PTPACKAGE = 	doclets
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	PtDoclet.java \
	PtIndexer.java \
	RatingTaglet.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include DIRS here, since demo is in DIRS, so 'make sources' will
# try to run 'make demo', which is wrong.
MISC_FILES =	#$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

#PTDOCPACKAGES = $(ALLPACKAGES)
# Comment out the above line and uncomment this line for testing.
PTDOCPACKAGES = ptolemy.actor.lib

# The ptdoc doclet (does not work yet) 
PTDOCLET = -doclet doc.doclets.PtDoclet
PTDOCFLAGS = -d doc/doclets/ptdoc
demo: ptdoc/index.html
ptdoc/index.html: PtDoclet.class
	if [ ! -d ptdoc ]; then mkdir -p ptdoc; fi
	-(cd $(ROOT); CLASSPATH=".$(CLASSPATHSEPARATOR)$(TOOLS_JAR)" \
		"$(JAVADOC)" \
			$(PTDOCLET) \
			$(PTDOCFLAGS) \
			$(PTDOCPACKAGES) )



# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
