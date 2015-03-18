# Makefile org sources from outside of Ptolemy
#
# @Version: $Id$
# @Author: Ilge Akkaya
#
# @Copyright (c) 2014 The Regents of the University of California.
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

# Root of Ptolemy II directory
ROOT = ../../../..

DIRS = demo doc test

# $(ME) is used in ptdir.mk
ME = org/ptolemy/machineLearning/particleFilter

CLASSPATH =	$(ROOT)

# True source directory
VPATH =	$(ROOT)/$(ME)

# Get configuration info
CONFIG = $(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = particleFilter
PTCLASSJAR = $(PTPACKAGE).jar

JSRCS = \
	AbstractParticleFilter.java \
	AbstractPredictor.java \
	AbstractStateSpaceSimulator.java \
	ConstraintsCalculator.java \
	MutualInformationCalculator.java \
	Optimizer.java \
	ParticleFilter.java \
	ParticleFilterRange.java \
	Particle.java  \
	ParticleMutualInformation.java

OTHER_FILES_TO_BE_JARED = \
	ParticleFilterRangeIcon.xml \
	particleFilter.xml \
	ParticleFilterIcon.xml

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES = test doc

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = demo doc test \
	'AbstractParticleFilter$$1.class' \
	'AbstractParticleFilter$$InputType.class' \
	'AbstractParticleFilter$$Particle.class' \
	'AbstractParticleFilter$$VariableScope.class' \
	'AbstractPredictor$$1.class' \
	'AbstractPredictor$$Particle.class' \
	'AbstractPredictor$$VariableScope.class' \
	'AbstractStateSpaceSimulator$$1.class' \
	'AbstractStateSpaceSimulator$$VariableScope.class' \
	'Optimizer$$1.class' \
	'ParticleFilter$$1.class' \
	'ParticleFilter$$Particle.class' \
	'ParticleFilter$$VariableScope.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
