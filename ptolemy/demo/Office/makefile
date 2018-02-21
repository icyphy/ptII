# Makefile for Ptolemy II Office demo
#
# @Authors: Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2018 The Regents of the University of California.
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

ME =		ptolemy/demo/Office

# Root of the Java directory
ROOT =		../../../

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	Office
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS =

OTHER_FILES_TO_BE_JARED = \
	AugmentedRealityOffice.xml \
	ControllableSensor.js \
	OfficeServices/AprilTags/README.txt \
	OfficeServices/AprilTags/tag36_11_00000.png \
	OfficeServices/AprilTags/tag36_11_00001.png \
	OfficeServices/AprilTags/tag36_11_00002.png \
	OfficeServices/AprilTags/tag36_11_00003.png \
	OfficeServices/AprilTags/tag36_11_00004.png \
	OfficeServices/AprilTags/tag36_11_00005.png \
	OfficeServices/AprilTags/tag36_11_00006.png \
	OfficeServices/AprilTags/tag36_11_00007.png \
	OfficeServices/AprilTags/tag36_11_00008.png \
	OfficeServices/AprilTags/tag36_11_00009.png \
	OfficeServices/AprilTags/tag36_11_00010.png \
	OfficeServices/AprilTags/tag36_11_00011.png \
	OfficeServices/AprilTags/tag36_11_00012.png \
	OfficeServices/AprilTags/tag36_11_00013.png \
	OfficeServices/AprilTags/tag36_11_00014.png \
	OfficeServices/AprilTags/tag36_11_00015.png \
	OfficeServices/AprilTags/tag36_11_00016.png \
	OfficeServices/AprilTags/tag36_11_00017.png \
	OfficeServices/AprilTags/tag36_11_00018.png \
	OfficeServices/AprilTags/tag36_11_00019.png \
	OfficeServices/AprilTags/tag36_11_00020.png \
	OfficeServices/AprilTags/tag36_11_00021.png \
	OfficeServices/AprilTags/tag36_11_00022.png \
	OfficeServices/AprilTags/tag36_11_00023.png \
	OfficeServices/AprilTags/tag36_11_00024.png \
	OfficeServices/AprilTags/tag36_11_00025.png \
	OfficeServices/AprilTags/tag36_11_00026.png \
	OfficeServices/AprilTags/tag36_11_00027.png \
	OfficeServices/AprilTags/tag36_11_00028.png \
	OfficeServices/AprilTags/tag36_11_00029.png \
	OfficeMonitorWebServer.xml \
	OfficeServices/HueCommandExplorer.xml \
	OfficeServices/KeyValueStoreServer.xml \
	OfficeServices/Lights.xml \
	OfficeServices/Sensors.xml \
	OfficeServices/Services.xml \
	OfficeServices/Sound.xml \
	OfficeServices/Sounds/bell.wav \
	OfficeServices/Sounds/laughter.wav \
	OfficeServices/Sounds/ring.wav \
	OfficeServices/Sounds/strum.wav \
	OfficeServices/Sounds/thunder.wav \
	OfficeServices/Sounds/train.wav \
	Office.xml

EXTRA_SRCS = \
	$(JSRCS) \
	$(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	OfficeServices

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass
	@echo "To run all the demos, run 'make demo'"

install: all $(PTCLASSJAR)

demo: demo_ptolemy demo_vergil

#demo_applet:
#	CLASSPATH="$(CLASSPATH)" appletviewer Office.htm

#demo_applet_vergil:
#	CLASSPATH="$(CLASSPATH)" appletviewer .htm

demo_ptolemy:
	"$(PTII)/bin/ptolemy" Office.xml

demo_vergil:
	"$(PTII)/bin/vergil" Office.xml

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

