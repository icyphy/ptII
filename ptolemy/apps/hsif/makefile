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
	data/mobies1_mid_g.xml \
	data/mobies2_mid_g.xml \
	data/shsif_mid_g.xml \
	data/thsif_mid_g.xml \
	data/mobies1_mid_sg.xml \
	data/mobies2_mid_sg.xml \
	data/shsif_mid_sg.xml \
	data/thsif_mid_sg.xml \
	data/mobies1_mid_l.xml \
	data/mobies2_mid_l.xml \
	data/shsif_mid_l.xml \
	data/thsif_mid_l.xml \
	data/mobies1_mid_sl.xml \
	data/mobies2_mid_sl.xml \
	data/shsif_mid_sl.xml \
	data/thsif_mid_sl.xml


# Command to run the Saxon transformer.
SAXON_TRANSFORM = CLASSPATH="$(CLASSPATH)" "$(JAVA)" \
			net.sf.saxon.Transform 
# make all
all:	mobies1.xml mobies2.xml shsif.xml thsif.xml
install: all

# In the rules below, note that the .xsl style sheet is at the end
# of the list of dependencies.

# Generating MoML files
#mobies1 : sl_pre_mobies1 mobies1.xml
mobies1.xml: data/mobies1_mid_sl.xml data/hsif.xsl
	$(SAXON_TRANSFORM) $^ > $@

#mobies2 : sl_pre_mobies2 mobies2.xml
mobies2.xml: data/mobies2_mid_sl.xml data/hsif.xsl
	$(SAXON_TRANSFORM) $^ > $@

#shsif : sl_pre_shsif shsif.xml
shsif.xml: data/shsif_mid_sl.xml data/hsif.xsl
	$(SAXON_TRANSFORM) $^ > $@

#thsif : sl_pre_thsif thsif.xml
thsif.xml: data/thsif_mid_sl.xml data/hsif.xsl
	$(SAXON_TRANSFORM) $^ > $@


# Dealing with the redundant nodes after l_pre_shif
#sl_pre_mobies1 : l_pre_mobies1 data/mobies1_mid_sl.xml
data/mobies1_mid_sl.xml: data/mobies1_mid_l.xml data/hsif_slim_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#sl_pre_mobies2 : l_pre_mobies1 data/mobies2_mid_sl.xml
data/mobies2_mid_sl.xml: data/mobies2_mid_l.xml data/hsif_slim_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#sl_pre_shsif : l_pre_shsif data/shsif_mid_sl.xml
data/shsif_mid_sl.xml: data/shsif_mid_l.xml data/hsif_slim_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#sl_pre_thsif : l_pre_thsif data/thsif_mid_sl.xml
data/thsif_mid_sl.xml: data/thsif_mid_l.xml data/hsif_slim_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@


# Dealing with Local Variables
#l_pre_mobies1 : sg_pre_mobies1 data/mobies1_mid_l.xml
data/mobies1_mid_l.xml: data/mobies1_mid_sg.xml data/hsif_localVariable_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#l_pre_mobies2 : sg_pre_mobies2 data/mobies2_mid_l.xml
data/mobies2_mid_l.xml: data/mobies2_mid_sg.xml data/hsif_localVariable_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#l_pre_shsif : sg_pre_shsif data/shsif_mid_l.xml
data/shsif_mid_l.xml: data/shsif_mid_sg.xml data/hsif_localVariable_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#l_pre_thsif : sg_pre_thsif data/thsif_mid_l.xml
data/thsif_mid_l.xml: data/thsif_mid_sg.xml data/hsif_localVariable_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

# Dealing with the redundant nodes after g_pre_shif
#sg_pre_mobies1 : g_pre_mobies1 data/mobies1_mid_sg.xml
data/mobies1_mid_sg.xml: data/mobies1_mid_g.xml data/hsif_slim_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#sg_pre_mobies2 : g_pre_mobies2 data/mobies2_mid_sg.xml
data/mobies2_mid_sg.xml: data/mobies2_mid_g.xml data/hsif_slim_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#sg_pre_shsif : g_pre_shsif data/shsif_mid_sg.xml
data/shsif_mid_sg.xml: data/shsif_mid_g.xml data/hsif_slim_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#sg_pre_thsif : g_pre_thsif data/thsif_mid_sg.xml
data/thsif_mid_sg.xml: data/thsif_mid_g.xml data/hsif_slim_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@


# Dealing with Global Variables
#g_pre_mobies1: data/mobies1_mid_g.xml
data/mobies1_mid_g.xml: data/mobies1.xml data/hsif_globalVariable_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#g_pre_mobies2: data/mobies2_mid_g.xml
data/mobies2_mid_g.xml: data/mobies2.xml data/hsif_globalVariable_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#g_pre_shsif: data/shsif_mid_g.xml
data/shsif_mid_g.xml: data/shsif.xml data/hsif_globalVariable_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@

#g_pre_thsif: data/thsif_mid_g.xml
data/thsif_mid_g.xml: data/thsif.xml data/hsif_globalVariable_preprocessor.xsl
	$(SAXON_TRANSFORM) $^ > $@


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk