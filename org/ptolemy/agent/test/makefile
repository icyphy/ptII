# Makefile for org.ptolemy.agent.test
#
# @Version: $Id$
##########################################################################

ROOT = ../../../..

ME = org/ptolemy/agent/test

CLASSPATH = $(ROOT)$(CLASSPATHSEPARATOR)$(JUNIT_JARS)

VPATH = $(ROOT)/$(ME)

CONFIG = $(ROOT)/mk/ptII.mk
include $(CONFIG)

PTPACKAGE = test
PTCLASSJAR =

JSRCS = \
	SessionSmokeTest.java \
	ToolRegistrySmokeTest.java \
	LibraryIndexSmokeTest.java

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass

include $(ROOT)/mk/ptcommon.mk
