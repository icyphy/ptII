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
PTPACKAGE = 	CarTracking
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS =

OTHER_FILES_TO_BE_JARED = \
	$(JSRCS) \
	CarTracking.xml

EXTRA_SRCS = \
	$(OTHER_FILES_TO_BE_JARED) \
	CarTracking.htm \
	CarTrackingVergil.htm

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
	CLASSPATH="$(CLASSPATH)" appletviewer CarTracking.htm

demo_vergil_applet:
	CLASSPATH="$(CLASSPATH)" appletviewer CarTrackingVergil.htm

demo_ptolemy:
	"$(PTII)/bin/ptolemy" CarTracking.xml

demo_vergil:
	"$(PTII)/bin/vergil" CarTracking.xml

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
