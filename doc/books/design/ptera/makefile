# Makefile for the Ptolemy II ptera book chapter models
#
# Version: $Id$
# Makefile Author: Christopher Brooks
#
# Copyright (c) 2010 The Regents of the University of California.
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

ME =		doc/books/design/ptera

# Sub directories to run make in.
# Don't include src directory, since we don't ship it with the release
DIRS =		test

# Root of Ptolemy II directory
ROOT =		../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

PTPACKAGE = 	ptera
PTCLASSJAR = 	$(PTPACKAGE).jar

EXAMPLE_MODELS = \
	TwoEvents.xml

Foo_EXAMPLE_MODELS = \
	TwoEvents.xml \
	MultipleEvents.xml \
	ArgAndCancel.xml \
	Simultaneous.xml \
	CarWash.xml \
	HierarchicalCarWash.xml \
	DEAndFSM.xml

OTHER_MODELS = \
	CarsAndServers.xml \
	Traffic.xml

OTHER_FILES_TO_BE_JARED = $(EXAMPLE_MODELS)

EXTRA_SRCS = $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

# To build all the jnlp files, run 'make jnlps'.
# See the jnlps rule in ptII/mk/ptcommon.mk and the book rule in ptII/mk/jnlp.mk"

# ptno-compile.mk has an "all" rule, so we define a default rule here
default:
	# The model files are copied from ptpapers/10/Ptera_Tech/models/
	#
	# See $PTII/mk/jnlp.mk for details, but the command to run is
	#   make JNLP_MODEL=ExtendedFSM JNLP_MODEL_DIRECTORY=doc/books/design/ptera KEYSTORE=/users/ptII/adm/certs/ptkeystore KEYALIAS=ptolemy STOREPASSWORD="-storepass xxxxxx" KEYPASSWORD="-keypass xxxxx" DIST_BASE=ptolemyII/ptII8.0/jnlp-books2 jnlps

# We don't include common.mk since we don't use the compiler here
include $(ROOT)/mk/ptno-compile.mk
