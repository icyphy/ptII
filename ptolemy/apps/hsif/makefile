# Makefile for the HSIF
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


ME =		ptolemy/apps/hsif

DIRS =

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	.$(CLASSPATHSEPARATOR)./saxon7.jar
# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# make clean will remove these derived files
KRUFT = \
	data/thermalStat_mid_g.xml \
	data/SwimmingPool_mid_g.xml \
	data/thermalStat_mid_sg.xml \
	data/SwimmingPool_mid_sg.xml \
	data/thermalStat_mid_l.xml \
	data/SwimmingPool_mid_l.xml \
	data/thermalStat_mid_sl.xml \
	data/SwimmingPool_mid_sl.xml \


# Command to run the Saxon transformer.
SAXON_TRANSFORM = CLASSPATH="$(CLASSPATH)" "$(JAVA)" \
			net.sf.saxon.Transform 
# make all
all:	thermalStat.xml SwimmingPool.xml
install: all

# In the rules below, note that the .xsl style sheet is at the end
# of the list of dependencies.

# Generating MoML files
#thermalStat : sl_pre_thermalStat thermalStat.xml
thermalStat.xml: data/thermalStat_mid_sl.xml data/hsif.xsl
	$(SAXON_TRANSFORM) $^ > $@

#SwimmingPool : sl_pre_SwimmingPool SwimmingPool.xml
SwimmingPool.xml: data/SwimmingPool_mid_sl.xml data/hsif.xsl
	$(SAXON_TRANSFORM) $^ > $@


# Dealing with the redundant nodes after l_pre_shif
#sl_pre_thermalStat : l_pre_thermalStat data/thermalStat_mid_sl.xml
data/thermalStat_mid_sl.xml: data/thermalStat_mid_l.xml data/hsif_slim_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#sl_pre_SwimmingPool : l_pre_thermalStat data/SwimmingPool_mid_sl.xml
data/SwimmingPool_mid_sl.xml: data/SwimmingPool_mid_l.xml data/hsif_slim_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@


# Dealing with Local Variables
#l_pre_thermalStat : sg_pre_thermalStat data/thermalStat_mid_l.xml
data/thermalStat_mid_l.xml: data/thermalStat_mid_sg.xml data/hsif_localVariable_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#l_pre_SwimmingPool : sg_pre_SwimmingPool data/SwimmingPool_mid_l.xml
data/SwimmingPool_mid_l.xml: data/SwimmingPool_mid_sg.xml data/hsif_localVariable_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@


# Dealing with the redundant nodes after g_pre_shif
#sg_pre_thermalStat : g_pre_thermalStat data/thermalStat_mid_sg.xml
data/thermalStat_mid_sg.xml: data/thermalStat_mid_g.xml data/hsif_slim_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#sg_pre_SwimmingPool : g_pre_SwimmingPool data/SwimmingPool_mid_sg.xml
data/SwimmingPool_mid_sg.xml: data/SwimmingPool_mid_g.xml data/hsif_slim_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@


# Dealing with Global Variables
#g_pre_thermalStat: data/thermalStat_mid_g.xml
data/thermalStat_mid_g.xml: data/thermalStat.xml data/hsif_globalVariable_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#g_pre_SwimmingPool: data/SwimmingPool_mid_g.xml
data/SwimmingPool_mid_g.xml: data/SwimmingPool.xml data/hsif_globalVariable_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk