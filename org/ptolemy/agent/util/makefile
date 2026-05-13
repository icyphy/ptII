# Makefile for org.ptolemy.agent.util
#
# @Version: $Id$
##########################################################################

ROOT = ../../../..

# $(ME) is used in ptdir.mk
ME = org/ptolemy/agent/util

CLASSPATH = $(ROOT)

VPATH = $(ROOT)/$(ME)

CONFIG = $(ROOT)/mk/ptII.mk
include $(CONFIG)

PTPACKAGE = util
PTCLASSJAR = $(PTPACKAGE).jar

JSRCS = \
	AgentResult.java \
	JsonResponse.java

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

include $(ROOT)/mk/ptcommon.mk
