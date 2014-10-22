# Makefile for the ptango temperature simulation demo.
#
# @Version: $Id$
# @Author: Christopher Brooks (makefile only)
#
# @Copyright (c) 2011-2014 The Regents of the University of California.
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

ME =		org/ptolemy/ptango/demo/TemperatureSimulation

DIRS =	pages

# Root of Ptolemy II directory
ROOT =		../../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	TemperatureSimulation
PTCLASSJAR =	$(PTPACKAGE).jar


OTHER_FILES_TO_BE_JARED = \
	EPlusScaife.idf \
	EPlusScaife.osm \
	EPlusScaife.skp \
	EPlusSimulation.xml \
	FileServer.xml \
	scaifethermalzones.png \
	pages \
	TemperatureSimulation.xml \
	USA_PA_Pittsburgh.Intl.AP.725200_TMY3.ddy \
	USA_PA_Pittsburgh.Intl.AP.725200_TMY3.epw \
	USA_PA_Pittsburgh.Intl.AP.725200_TMY3.stat \
	variables.cfg 

EXTRA_SRCS = \
	$(OTHER_FILES_TO_BE_JARED) 

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

OPTIONAL_FILES =

# Files to remove with 'make clean'
KRUFT = \
	*.jar

all: suball $(EXTRA_SRCS)
install: subinstall jars

# Include rules for directories that contain only subdirectories.
include $(ROOT)/mk/ptcommon.mk
