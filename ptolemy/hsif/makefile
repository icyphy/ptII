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

DIRS =		xsl

# Root of the Java directory
ROOT =		../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR).$(CLASSPATHSEPARATOR)$(ROOT)/ptolemy/apps/hsif/saxon7.jar
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
	HSIFEffigyFactory.java

# make clean will remove these derived files
KRUFT = \
	data/Thermostat_mid_g.xml \
	data/SwimmingPool_mid_g.xml \
	data/Thermostat_mid_sg.xml \
	data/SwimmingPool_mid_sg.xml \
	data/Thermostat_mid_l.xml \
	data/SwimmingPool_mid_l.xml \
	data/Thermostat_mid_sl.xml \
	data/SwimmingPool_mid_sl.xml


# Files to be included in the jar file
OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

# Command to run the Saxon transformer.
SAXON_TRANSFORM = CLASSPATH="$(CLASSPATH)" "$(JAVA)" \
			net.sf.saxon.Transform 
JCLASS = $(JSRCS:%.java=%.class)

all:	jclass Thermostat.xml SwimmingPool.xml
install: all jars 

# In the rules below, note that the .xsl style sheet is at the end
# of the list of dependencies.

# Generating MoML files
#Thermostat : sl_pre_Thermostat Thermostat.xml
Thermostat.xml: data/Thermostat_mid_sl.xml xsl/HSIF.xsl
	$(SAXON_TRANSFORM) $^ > $@

#SwimmingPool : sl_pre_SwimmingPool SwimmingPool.xml
SwimmingPool.xml: data/SwimmingPool_mid_sl.xml xsl/HSIF.xsl
	$(SAXON_TRANSFORM) $^ > $@


# Dealing with the redundant nodes after l_pre_shif
#sl_pre_Thermostat : l_pre_Thermostat data/Thermostat_mid_sl.xml
data/Thermostat_mid_sl.xml: data/Thermostat_mid_l.xml xsl/SlimPreprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#sl_pre_SwimmingPool : l_pre_Thermostat data/SwimmingPool_mid_sl.xml
data/SwimmingPool_mid_sl.xml: data/SwimmingPool_mid_l.xml xsl/SlimPreprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@


# Dealing with Local Variables
#l_pre_Thermostat : sg_pre_Thermostat data/Thermostat_mid_l.xml
data/Thermostat_mid_l.xml: data/Thermostat_mid_sg.xml xsl/LocalVariablePreprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#l_pre_SwimmingPool : sg_pre_SwimmingPool data/SwimmingPool_mid_l.xml
data/SwimmingPool_mid_l.xml: data/SwimmingPool_mid_sg.xml xsl/LocalVariablePreprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@


# Dealing with the redundant nodes after g_pre_shif
#sg_pre_Thermostat : g_pre_Thermostat data/Thermostat_mid_sg.xml
data/Thermostat_mid_sg.xml: data/Thermostat_mid_g.xml xsl/SlimPreprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#sg_pre_SwimmingPool : g_pre_SwimmingPool data/SwimmingPool_mid_sg.xml
data/SwimmingPool_mid_sg.xml: data/SwimmingPool_mid_g.xml xsl/SlimPreprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@


# Dealing with Global Variables
#g_pre_Thermostat: data/Thermostat_mid_g.xml
data/Thermostat_mid_g.xml: data/Thermostat.xml xsl/GlobalVariablePreprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#g_pre_SwimmingPool: data/SwimmingPool_mid_g.xml
data/SwimmingPool_mid_g.xml: data/SwimmingPool.xml xsl/GlobalVariablePreprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
