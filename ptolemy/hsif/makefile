# Makefile for HSIF -> MoML demonstrations
#
# @Authors: Christopher Hylands (makefile only)
#
# @Version $Id$
#
# @Copyright (c) 2002-2003 The Regents of the University of California.
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


ME =		ptolemy/hsif

DIRS =		demo xsl test

# Root of the Java directory
ROOT =		../..

CLASSPATH =	$(ROOT)
# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	hsif
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

PTCLASSALLJARS = \
	xsl/xsl.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	HSIFEffigyFactory.java \
	HSIFUtilities.java

# make clean will remove these derived files
KRUFT = \
	data/Thermostat_mid_g.xml \
	data/SwimmingPool_mid_g.xml \
	data/Thermostat_mid_sg.xml \
	data/SwimmingPool_mid_sg.xml \
	data/Thermostat_mid_l.xml \
	data/SwimmingPool_mid_l.xml \
	data/Thermostat_mid_sl.xml \
	data/SwimmingPool_mid_sl.xml \
	data/Thermostat_mid.xml \
	data/SwimmingPool_mid.xml


# Files to be included in the jar file
OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =  xsl test data

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo

# AUXCLASSPATH
AUXCLASSPATH = ;../../lib/saxon7.jar

# Command to run the Saxon transformer.
# Use AUXCLASSPATH in XSL_TRANSFORM so that the code coverage will work
XSL_TRANSFORM = CLASSPATH="$(CLASSPATH)$(AUXCLASSPATH)" "$(JAVA)" \
			ptolemy.util.XSLTUtilities
JCLASS = $(JSRCS:%.java=%.class)

# This rule will convert a .hsif file into a .moml file. Try:
#   cp demo/SwimmingPool/SwimmingPool.xml SwimmingPool.hsif
#   cp demo/SwimmingPool/HSIF.dtd .
#   make SwimmingPool.moml
.SUFFIXES: .hsif .moml
.hsif.moml:
	rm -f `basename $< .hsif`.moml
	CLASSPATH="$(CLASSPATH)$(AUXCLASSPATH)" "$(JAVA)" \
		ptolemy.hsif.HSIFUtilities $< $@

all:	jclass Thermostat_moml.xml SwimmingPool_moml.xml
install: all jars 

# In the rules below, note that the .xsl style sheet is at the end
# of the list of dependencies.

# Dealing with redundant links
Thermostat_moml.xml: data/Thermostat_mid.xml xsl/SlimPreprocessorForLinks.xsl
	$(XSL_TRANSFORM) $^ $@

SwimmingPool_moml.xml: data/SwimmingPool_mid.xml xsl/SlimPreprocessorForLinks.xsl
	$(XSL_TRANSFORM) $^ $@


# Generating MoML files
data/Thermostat_mid.xml: data/Thermostat_mid_sl.xml xsl/HSIF.xsl
	$(XSL_TRANSFORM) $^ $@

data/SwimmingPool_mid.xml: data/SwimmingPool_mid_sl.xml xsl/HSIF.xsl
	$(XSL_TRANSFORM) $^ $@


# Dealing with the redundant nodes after l_pre_shif
data/Thermostat_mid_sl.xml: data/Thermostat_mid_l.xml xsl/SlimPreprocessor.xsl
	$(XSL_TRANSFORM) $^ $@

data/SwimmingPool_mid_sl.xml: data/SwimmingPool_mid_l.xml xsl/SlimPreprocessor.xsl
	$(XSL_TRANSFORM) $^ $@


# Dealing with Local Variables
data/Thermostat_mid_l.xml: data/Thermostat_mid_sg.xml xsl/LocalVariablePreprocessor.xsl
	$(XSL_TRANSFORM) $^ $@

data/SwimmingPool_mid_l.xml: data/SwimmingPool_mid_sg.xml xsl/LocalVariablePreprocessor.xsl
	$(XSL_TRANSFORM) $^ $@


# Dealing with the redundant nodes after g_pre_shif
data/Thermostat_mid_sg.xml: data/Thermostat_mid_g.xml xsl/SlimPreprocessor.xsl
	$(XSL_TRANSFORM) $^ $@

data/SwimmingPool_mid_sg.xml: data/SwimmingPool_mid_g.xml xsl/SlimPreprocessor.xsl
	$(XSL_TRANSFORM) $^ $@


# Dealing with Global Variables
data/Thermostat_mid_g.xml: demo/Thermostat/Thermostat.xml xsl/GlobalVariablePreprocessor.xsl
	$(XSL_TRANSFORM) $^ $@

data/SwimmingPool_mid_g.xml: demo/SwimmingPool/SwimmingPool.xml xsl/GlobalVariablePreprocessor.xsl
	$(XSL_TRANSFORM) $^ $@


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
