# Makefile for org.ptolemy.agent.library
#
# @Version: $Id$
##########################################################################

ROOT = ../../../..

ME = org/ptolemy/agent/library

CLASSPATH = $(ROOT)

VPATH = $(ROOT)/$(ME)

CONFIG = $(ROOT)/mk/ptII.mk
include $(CONFIG)

PTPACKAGE = library
PTCLASSJAR = $(PTPACKAGE).jar

JSRCS = \
	LibraryIndex.java \
	LibraryScanner.java

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

include $(ROOT)/mk/ptcommon.mk
