# Makefile for Steer By Wire (SBW) demo
#
# @Authors: Christopher Brooks, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2005 The Regents of the University of California.
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

ME =		vendors/ecsl_dp/sbw

DIRS = 		# test

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	sbw
PTCLASSJAR =    $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS =

OTHER_FILES_TO_BE_JARED = \
	sbw_data_flow_12_1.mdl

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) \
	ECSL_DP-meta.xme \
	README.txt \
        sbw_edp.xme

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =


JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

##################################################
# Steer By Wire (sbw) demo
demo: all sbw_ECSL.moml
	$(PTII)/bin/vergil sbw_ECSL.moml

sbw_ECSL.moml: sbw_ECSL.xml ecsl2moml
	../ptolemy/ecsl2moml $<

sbw_ECSL.xml: sbw_data_flow_12_1.xml
	../XML2ECSL/bin/Xml2Ecsl_d $< $@

sbw_data_flow_12_1.xml: sbw_data_flow_12_1.mdl
	../MDL2XML/bin/MDL2XMLd $<

# Get the rules for codegen.  Run "make demo_gdk" to run Giotto.
include $(ROOT)/ptolemy/domains/giotto/kernel/giotto.mk

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk








