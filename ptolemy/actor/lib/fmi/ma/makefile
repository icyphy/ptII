# Functional Mockup Unit (FMU) Master Algorithm in C

# @Authors: Fabian Stanke, Christopher Brooks
#
# @Version: $Id: makefile 67236 2013-08-26 16:46:24Z cxh $
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

ME =		ptolemy/actor/lib/fmi/ma

DIRS =

# Root of the Java directory
ROOT =		../../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ma
PTCLASSJAR = 	$(PTPACKAGE).jar

EXEC = main # name of the executable

CC = gcc

KRUFT = $(EXEC) *.dSYM *.o
# Build master algorithm
all: $(EXEC)

# Helper sources
SHARED_SRCS = sim_support.c

CPP_SRCS = \
	parser/XmlElement.cpp \
	parser/XmlParser.cpp \
	parser/XmlParserCApi.cpp

# Main target
TARGET = main.c

# Dependencies
DEPS = \
	includes/fmi.h \
	includes/fmiFunctions.h \
	includes/fmiFunctionTypes.h \
	includes/fmiTypesPlatform.h \
	parser/XmlElement.h \
	parser/XmlParser.h \
	parser/XmlParserCApi.h \
	parser/XmlParserException.h

EXTRA_SRCS = $(CPP_SRCS) $(TARGET) $(DEPS) sim_support.h sim_support.c

# Set CFLAGS to -m32 to build for linux32
CFLAGS= -g -Wall -DFMI_COSIMULATION -DSTANDALONE_XML_PARSER -DLIBXML_STATIC $(INCFLAGS)
INCFLAGS= -Iincludes -Iparser/libxml -Iparser
LIBFLAGS= -lexpat -ldl -lxml2

CXX=c++

# Build target
%.o: %.c
	$(CC) -o $@ -c $< $(CFLAGS)

main: main.o sim_support.o	
	$(CXX) -o $@ $^ $(CPP_SRCS) $(CFLAGS) $(LIBFLAGS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't set MISC_FILES to $(DIRS) or else 'make sources' will try to invoke the demos.
MISC_FILES = includes 

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	parser

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk	
