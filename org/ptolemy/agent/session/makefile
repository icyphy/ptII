# Makefile for org.ptolemy.agent.session
#
# @Version: $Id$
##########################################################################

ROOT = ../../../..

ME = org/ptolemy/agent/session

CLASSPATH = $(ROOT)

VPATH = $(ROOT)/$(ME)

CONFIG = $(ROOT)/mk/ptII.mk
include $(CONFIG)

PTPACKAGE = session
PTCLASSJAR = $(PTPACKAGE).jar

JSRCS = \
	PtolemySession.java \
	SessionManager.java \
	SignalCollector.java \
	GraphSerializer.java

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

include $(ROOT)/mk/ptcommon.mk
