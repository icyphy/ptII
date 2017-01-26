# Makefile org.ptolemy sources
#
# @Version: $Id$
# @Author: Christopher Brooks
#
# @Copyright (c) 2012-2016 The Regents of the University of California.
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

# PTANGO_DIR is set by configure in $PTII/mk if jetty and javax.servlet are found.
# PTOPENCV_DIR is set by configure in $PTII/mk/ptII.mk if Open Computer Vision was found. 
# See also $PTII/org/opencv
DIRS = classloading \
	commons \
	faultModels \
	fmi \
	machineImprovisation \
	machineLearning \
	$(PTOPENCV_DIR) \
	optimization \
	$(PTOSCP5_DIR) \
	$(PTANGO_DIR) \
	qss \
	ssm

# Root of Ptolemy II directory
ROOT =		../..

# $(ME) is used in ptdir.mk
ME =		org.ptolemy

# True source directory
VPATH =		$(ROOT)/$(ME)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ptolemy
PTCLASSJAR =

PTCLASSALLJAR = $(PTPACKAGE).jar

# Include these jars in PTAUXALLJAR
# plotmlapplet pulls in the com/microstar/xml and util/ classes
PTCLASSALLJARS = \
	classloading/classloading.jar \
	commons/commons.jar \
	faultModels/faultModels.jar \
	fmi/fmi.jar \
	machineImprovisation/machineImprovisation.jar \
	machineLearning/machineLearning.jar \
	optimization/optimization.jar \
	qss/qss.jar \
	ssm/ssm.jar

all: suball
install: subinstall jars

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = $(DIRS)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
