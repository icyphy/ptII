# Makefile for Ptolemy II demonstration
#
# @Author:
# @Version: $Id$
#

# Root of the Java directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	InverseKinematics
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS =

OTHER_FILES_TO_BE_JARED = \
	$(JSRCS) \
	InverseKinematics.xml

EXTRA_SRCS = \
	$(OTHER_FILES_TO_BE_JARED) \
	InverseKinematics.htm \
	InverseKinematicsVergil.htm

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass
	@echo "To run all the demos, run 'make demo'"

install: all $(PTCLASSJAR)

demo: demo_applet demo_vergil_applet demo_ptolemy demo_vergil
demo_applet:
	CLASSPATH="$(CLASSPATH)" appletviewer InverseKinematics.htm

demo_vergil_applet:
	CLASSPATH="$(CLASSPATH)" appletviewer InverseKinematicsVergil.htm

demo_ptolemy:
	"$(PTII)/bin/ptolemy" InverseKinematics.xml

demo_vergil:
	"$(PTII)/bin/vergil" InverseKinematics.xml

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
