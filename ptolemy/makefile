# Makefile for Java classes used to implement Ptolemy
#
# @Version: $Id$
# @Author: Christopher Hylands
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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
DIRS = kernel plot graph moml data actor gui math media	domains vergil

# Root of Ptolemy II directory
ROOT =		..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ptolemy
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

# Include these jars in PTAUXALLJAR
# plotmlapplet pulls in the com/microstar/xml classes
PTAUXALLJARS = \
		../collections/ptcollections.jar \
		gui/gui.jar \
		kernel/kernel.jar \
		plot/plotmlapplet.jar \
		graph/graph.jar \
		data/data.jar \
		actor/actor.jar \
		math/math.jar \
		media/media.jar \
		moml/moml.jar

# All the .class files necessary to run a Ptolemy II domain applet
PTAUXALLJAR =	ptsupport.jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		$(PTAUXALLJARS) \
		domains/domains.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

EXTRA_SRCS =	

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS)

OPTIONAL_FILES = filter system

all: suball $(EXTRA_SRCS)
install: subinstall jars

../collections/ptcollections.jar:
	(cd ../collections; $(MAKE) ptcollections.jar)

# Include rules for directories that contain only subdirectories.
include $(ROOT)/mk/ptcommon.mk
