# Makefile for the HSIF
#
# @Authors: Christopher Hylands (makefile only)
#
# @Version $Id$
#
# @Copyright (c) 2002 The Regents of the University of California.
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


# dealing with Global Variables

ME =		ptolemy/apps/hsif

DIRS =

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)
# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

g_pre_shsif :
	java -classpath ".$(CLASSPATHSEPARATOR)./saxon7.jar" net.sf.saxon.Transform data/shsif.xml data/hsif_globalVariable_preprocessor.xsl >data/shsif_mid_g.xml

g_pre_thsif :
	java -classpath ".$(CLASSPATHSEPARATOR)./saxon7.jar" net.sf.saxon.Transform data/thsif.xml data/hsif_globalVariable_preprocessor.xsl >data/thsif_mid_g.xml

# dealing with the redundant nodes after g_pre_shif
sg_pre_shsif : g_pre_shsif
	java -classpath ".$(CLASSPATHSEPARATOR)./saxon7.jar" net.sf.saxon.Transform data/shsif_mid_g.xml data/hsif_slim_preprocessor.xsl >data/shsif_mid_sg.xml

sg_pre_thsif : g_pre_thsif
	java -classpath ".$(CLASSPATHSEPARATOR)./saxon7.jar" net.sf.saxon.Transform data/thsif_mid_g.xml data/hsif_slim_preprocessor.xsl >data/thsif_mid_sg.xml

# dealing with Local Variables
l_pre_shsif : sg_pre_shsif
	java -classpath ".$(CLASSPATHSEPARATOR)./saxon7.jar" net.sf.saxon.Transform data/shsif_mid_sg.xml data/hsif_localVariable_preprocessor.xsl >data/shsif_mid_l.xml

l_pre_thsif : sg_pre_thsif
	java -classpath ".$(CLASSPATHSEPARATOR)./saxon7.jar" net.sf.saxon.Transform data/thsif_mid_sg.xml data/hsif_localVariable_preprocessor.xsl >data/thsif_mid_l.xml

# dealing with the redundant nodes after l_pre_shif
sl_pre_shsif : l_pre_shsif
	java -classpath ".$(CLASSPATHSEPARATOR)./saxon7.jar" net.sf.saxon.Transform data/shsif_mid_l.xml data/hsif_slim_preprocessor.xsl >data/shsif_mid_sl.xml

sl_pre_thsif : l_pre_thsif
	java -classpath ".$(CLASSPATHSEPARATOR)./saxon7.jar" net.sf.saxon.Transform data/thsif_mid_l.xml data/hsif_slim_preprocessor.xsl >data/thsif_mid_sl.xml

# generating MoML files
shsif : sl_pre_shsif
	java -classpath ".$(CLASSPATHSEPARATOR)./saxon7.jar" net.sf.saxon.Transform data/shsif_mid_sl.xml data/hsif.xsl >shsif.xml

thsif : sl_pre_thsif
	java -classpath ".$(CLASSPATHSEPARATOR)./saxon7.jar" net.sf.saxon.Transform data/thsif_mid_sl.xml data/hsif.xsl >thsif.xml

# make all
all:	shsif thsif

