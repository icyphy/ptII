# Makefile for org.ptolemy.agent.llm
#
# @Version: $Id$
##########################################################################

ROOT = ../../../..

ME = org/ptolemy/agent/llm

CLASSPATH = $(ROOT)

VPATH = $(ROOT)/$(ME)

CONFIG = $(ROOT)/mk/ptII.mk
include $(CONFIG)

PTPACKAGE = llm
PTCLASSJAR = $(PTPACKAGE).jar

JSRCS = \
	LLMClient.java \
	LLMResponse.java \
	OpenAIClient.java \
	NullLLMClient.java \
	PromptTemplates.java

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

include $(ROOT)/mk/ptcommon.mk
