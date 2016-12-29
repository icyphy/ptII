# Makefile for Java classes used to implement Ptolemy
#
# @Version: $Id$
# @Author: Christopher Hylands
#
# @Copyright (c) 1997-2013 The Regents of the University of California.
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

ME =		ptolemy

# Order matters here.  kernel should be first, then actors, then domains
# so that when we compile in a directory we should have already compiled
# any classes in other directories that we depend on.
# Note that data.expr uses moml
# Tests in kernel.util use data.expr which uses math
# PTCOPERNICUS_DIR is set to copernicus by configure in $PTII/mk/ptII.mk
# if the Soot package was found.
#
# copernicus should be compiled after the domains because it uses the domains.
#
# PTCHIC_DIR is set to chic by configure in $PTII/mk/ptII.mk
# if $PTII/lib/chic.jar was found
#
# PTCALTROP_DIR is set to caltrop by configure in $PTII/mk/ptII.mk
# if $PTII/lib/ptCal.jar was found
#
# $PTII/configure looks for Jini and sets PTDISTRIBUTED_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTDISTRIBUTED_DIR is empty.
#
# $PTII/configure looks for Java 1.5 and sets PTBACKTRACK_DIR to backtrack
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTBACKTRACK is empty.
#
# $PTII/configure looks for Netbeans gui jars and sets PTHOMER_DIR to homer
# if it was found.  Otherwise, PTHOMER_DIR is empty.
#
DIRS = util math kernel plot graph moml data actor gui media	\
	$(PTMATLAB_DIR)	\
	domains	\
	$(PTCOPERNICUS_DIR) \
	verification \
	vergil $(PTCHIC_DIR) cg component $(PTBACKTRACK_DIR) \
	configs \
	$(PTDISTRIBUTED_DIR) $(PTHOMER_DIR) ptp $(PTCALTROP_DIR) \
	contrib demo

# Root of Ptolemy II directory
ROOT =		..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ptolemy
PTCLASSJAR =

# Include these jars in PTAUXALLJAR
# plotmlapplet pulls in the com/microstar/xml and util/ classes
PTAUXALLJARS = \
		actor/actor.jar \
		configs/configs.jar \
		data/data.jar \
		gui/gui.jar \
		graph/graph.jar \
		kernel/kernel.jar \
		math/math.jar \
		media/media.jar \
		moml/moml.jar \
		plot/plotmlapplet.jar \
		util/util.jar

# No need for util/util.jar, plotmlapplet.jar includes those classes
# util/util.jar

# All the .class files necessary to run a Ptolemy II domain applet
PTAUXALLJAR =	ptsupport.jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		$(PTAUXALLJARS) \
		actor/gt/gt.jar \
		domains/domains.jar \
		copernicus/copernicus.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

EXTRA_SRCS =

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = util kernel plot graph moml data actor gui math media	\
	backtrack ptp verification \
	$(PTCALTROP_DIR) \
	$(PTCOPERNICUS_DIR) \
	$(PTDISTRIBUTED_DIR) \
	$(PTHOMER_DIR) \
	$(PTMATLAB_DIR)	domains vergil configs

OPTIONAL_FILES = apps cal chic cg component demo matlab


all: suball $(EXTRA_SRCS)
install: subinstall jars

# Run all the tests that do not take much time.
fast_tests:
	$(MAKE) PTCOPERNICUS_DIR= tests 

# Include rules for directories that contain only subdirectories.
include $(ROOT)/mk/ptcommon.mk
