# Makefile for Ptolemy interface to OpenModelica OMC
#
# @Version: $Id$
# @Author: Christopher Brooks (makefile only)
#
# @Copyright (c) 2012 The Regents of the University of California.
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

ME =		ptolemy/actor/lib/openmodelica/omc

# Root of the Ptolemy II directory
ROOT =		../../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	omc
PTCLASSJAR =	$(PTPACKAGE).jar

#PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	OMCLogger.java \
	OMCParser.java \
	OMCProxy.java \
	StreamReaderThread.java

# Files or directories that are present, but that 'make checkjunk'
# should not complain about
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =  corba

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'OMCLogger$$1.class' \
	'OMCLogger$$2.class' \
	'OMCProxy$$1.class' \
	'OMCProxy$$LoadResult.class' \
	'OMCProxy$$OMCThread.class' \
	'OMCProxy$$osType.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
