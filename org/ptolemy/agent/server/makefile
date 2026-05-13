# Makefile for org.ptolemy.agent.server
#
# @Version: $Id$
##########################################################################

ROOT = ../../../..

ME = org/ptolemy/agent/server

CLASSPATH = $(ROOT)

VPATH = $(ROOT)/$(ME)

CONFIG = $(ROOT)/mk/ptII.mk
include $(CONFIG)

PTPACKAGE = server
PTCLASSJAR = $(PTPACKAGE).jar

JSRCS = \
	AgentServerMain.java \
	RestRoutes.java \
	SimpleHttpServer.java

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

include $(ROOT)/mk/ptcommon.mk
