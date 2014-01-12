# Makefile for yield return
#
# @Version: $Id$
# @Author: Christopher Brooks
#
# @Copyright (c) 2012-2013 The Regents of the University of California.
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

ME = 		net/jimblackler/Utils

DIRS =

# Root of Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	Utils
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	CollectionAbortedException.java \
	Collector.java \
	ReferenceYieldAdapter.java \
	ResultHandler.java \
	ThreadedYieldAdapter.java \
	YieldAdapter.java \
	YieldAdapterIterable.java \
	YieldAdapterIterator.java

OTHER_FILES_TO_BE_JARED = \
	jimblacklerUtils-license.htm

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)


EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include $(DIRS) here, we don't want make sources to run make demo
MISC_FILES =	#$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
# driver is in OPTIONAL_FILES because we don't ship it.
OPTIONAL_FILES = \
	'ReferenceYieldAdapter$$1.class' \
	'ReferenceYieldAdapter$$2$$1.class' \
	'ReferenceYieldAdapter$$2.class' \
	'ThreadedYieldAdapter$$1$$1$$1.class' \
	'ThreadedYieldAdapter$$1$$1.class' \
	'ThreadedYieldAdapter$$1$$2.class' \
	'ThreadedYieldAdapter$$1.class' \
	'ThreadedYieldAdapter$$AbortedMessage.class' \
	'ThreadedYieldAdapter$$IllegalActionMessage.class' \
	'ThreadedYieldAdapter$$EndMessage.class' \
	'ThreadedYieldAdapter$$Message.class' \
	'ThreadedYieldAdapter$$StopMessage.class' \
	'ThreadedYieldAdapter$$ValueMessage.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)
docs: javadocs

# Include the rest of the rules
include $(ROOT)/mk/ptcommon.mk

