# Makefile for ptdb - A model database for Ptolemy, testdata directory
#
# @Authors: Christopher Brooks (makefile only), based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2010 The Regents of the University of California.
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

ME = 		ptdb/config/database/testdata

DIRS =

# Root of the Ptolemy II directory
ROOT =		../../../..

CLASSPATH = 	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	testdata
PTCLASSJAR =	$(PTPACKAGE).jar

# All the .class files necessary to run a Ptolemy II domain applet
#PTCLASSALLJARS =
#PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS =

OTHER_FILES_TO_BE_JARED = \
	create_attributes_file.txt \
	create_models.txt \
	create_reference_file.txt \
	create_script.txt \
	remove_models.txt \
	remove.txt \
	update_script.txt

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	test img

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \

JCLASS = $(JSRCS:%.java=%.class)

all: Testing.dbxml
install: all jars

# Initialize the test database
initialize:
	(cd ../..; rm ptdb-params.properties; $(MAKE) ptdb-params.properties)
	-echo "quit" | "$(PTDBXML_LIB)/../bin/dbxml" -s remove.txt
	echo "quit" | "$(PTDBXML_LIB)/../bin/dbxml" -s create_script.txt

Testing.dbxml: 
	if [ ! -f $@ ]; then \
		echo "quit" | "$(PTDBXML_LIB)/../bin/dbxml" -s create_script.txt; \
	fi

KRUFT = Testing.dbxml \
	log.* \
	__db.* \
	*.dbxml

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
