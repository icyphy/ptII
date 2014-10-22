# Makefile Ptango - Ptolemy Internet of Things demos
#
# @Version: $Id$
# @Author: Christopher Brooks
#
# @Copyright (c) 2012-2014 The Regents of the University of California.
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

DIRS = BlinkyLight Chat Cookies DOPpresence \
	EPlus70Actuator ErrorHandler Exception files Guidance \
	OccupancyAlert OptimizationAdvisor SensorAndrew \
	SmartRoom TemperatureSimulation \
	UCBPower UCBPowerPtera WebServerDE

# Root of Ptolemy II directory
ROOT =		../../../..

# $(ME) is used in ptdir.mk
ME =		org/ptolemy/ptango/demo

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	demo
#PTCLASSJAR =	$(PTPACKAGE).jar

# Include these jars in PTAUXALLJAR
# (Copied syntax from \ptolemy\demo )
PTAUXALLJARS = \
	BlinkyLight/BlinkyLight.jar \
	Chat/Chat.jar \
	Cookies/Cookies.jar \
	DOPpresence/DOPpresence.jar \
	EPlus70Actuator/EPlus70Actuator.jar \
	ErrorHandler/ErrorHandler.jar \
	Exception/Exception.jar \
	files/files.jar \
	Guidance/Guidance.jar \
	OccupancyAlert/OccupancyAlert.jar \
	OptimizationAdvisor/OptimizationAdvisor.jar \
	SensorAndrew/SensorAndrew.jar \
	SmartRoom/SmartRoom.jar \
	TemperatureSimulation/TemperatureSimulation.jar \
	UCBPower/UCBPower.jar \
	UCBPowerPtera/UCBPowerPtera.jar \
	WebServerDE/WebServerDE.jar 

PTAUXALLJAR = $(PTPACKAGE).jar

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS = \
	$(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS)

OPTIONAL_FILES = $(DIRS)

# Files to remove with 'make clean'
KRUFT = \
	*.jar

all: suball $(EXTRA_SRCS)
install: subinstall jars

# Include rules for directories that contain only subdirectories.
include $(ROOT)/mk/ptcommon.mk
