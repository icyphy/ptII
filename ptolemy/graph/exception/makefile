# Makefile for the Ptolemy Java graph exception classes.
#
# @Authors: Mingyung Ko, based on a file by Thomas M. Parks and Christopher Hylands
# @Version: $Id$
#
# Copyright (c) 2002 The University of Maryland.
# All rights reserved.
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the above
# copyright notice and the following two paragraphs appear in all copies
# of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.

ME =        ptolemy/graph/exception

# Root of the Java directory
ROOT =      ../../..

CLASSPATH = $(ROOT)

# Get configuration info
CONFIG =    $(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE =     exception
PTCLASSJAR =    $(PTPACKAGE).jar

JSRCS = \
    GraphActionException.java \
    GraphConstructionException.java \
    GraphElementException.java \
    GraphException.java \
    GraphStateException.java \
    GraphTopologyException.java \
    GraphWeightException.java

EXTRA_SRCS =    $(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
    doc

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
