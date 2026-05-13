# Makefile for org.ptolemy.agent.agent
#
# @Version: $Id$
##########################################################################

ROOT = ../../../..

ME = org/ptolemy/agent/agent

CLASSPATH = $(ROOT)

VPATH = $(ROOT)/$(ME)

CONFIG = $(ROOT)/mk/ptII.mk
include $(CONFIG)

PTPACKAGE = agent-core
PTCLASSJAR = $(PTPACKAGE).jar

JSRCS = \
	AgentLoop.java \
	AgentBackend.java \
	AgentTrace.java

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

include $(ROOT)/mk/ptcommon.mk
