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
demo: 
	@echo "Run 'make demo_dataflow' or 'make demo_components'"

##################################################
# Steer By Wire (sbw) demo using MDL2XML and XML2ECSL
demo_dataflow: all sbw_ECSL.moml
	@echo "# Double Click on the Generate Giotto Icon and "
	@echo "# save the Giotto file as $(PTPACKAGE).giotto"
	@echo "# Exit vergil, then run \"make demo_gdk\""
	$(PTII)/bin/vergil sbw_ECSL.moml

# Script that converts from ECSL_DP to MoML
ECSL2MOML =	../ptolemy/ecsl2moml
sbw_ECSL.moml: sbw_ECSL.xml $(ECSL2MOML)
	$(ECSL2MOML) $<

sbw_ECSL.xml: sbw_data_flow_12_1.xml
	../XML2ECSL/bin/Xml2Ecsl_d $< $@

sbw_data_flow_12_1.xml: sbw_data_flow_12_1.mdl
	../MDL2XML/bin/MDL2XML $<

##################################################
# Steer By Wire (sbw) demo using xml file generated
# by GME
demo_components: sbw_components.moml
	@echo "# Double Click on the Generate Giotto Icon and "
	@echo "# save the Giotto file as $(PTPACKAGE).giotto"
	@echo "# Exit vergil, then run \"make demo_gdk\""
	$(PTII)/bin/vergil $<


sbw_components.moml: sbw_components.xml $(ECSL2MOML)
	$(ECSL2MOML) $<

# UDM_PATH should be set when UDM is installed
# Usually, UDM_PATH is something like C:\Program Files\ISIS\UDM\
#
ECSL_DP.xsd:
	"$(UDM_PATH)/bin/udm.exe" ECSL_DP.xml

sbw_components.xml: ECSL_DP.xsd
	@echo "If this fails, make sure the current directory is in the path"
	"$(UDM_PATH)/bin/udmcopy" sbw.mga sbw_components.xml ECSL_DP.xml



# Get the rules for codegen.  Run "make demo_gdk" to run Giotto.
include $(ROOT)/ptolemy/domains/giotto/kernel/giotto.mk

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk


