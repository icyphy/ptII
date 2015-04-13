# Functional Mockup Unit (FMU) Master Algorithm in C

# @Authors: Fabian Stanke, Christopher Brooks
#
# @Version: $Id: makefile 70106 2014-09-22 23:18:09Z cxh $
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

# Object directory
ODIR = obj

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
SHARED_SRCS = \
	shared/sim_support.c

CPP_SRCS = \
	parser/XmlElement.cpp \
	parser/XmlParser.cpp \
	parser/XmlParserCApi.cpp

# Main target
TARGET = main2.c

# Dependencies
DEPS = \
	maSupport.h \
	shared/sim_support.h \
	includes/fmi2.h \
	includes/fmi2Functions.h \
	includes/fmi2FunctionTypes.h \
	includes/fmi2TypesPlatform.h \
	parser/XmlElement.h \
	parser/XmlParser.h \
	parser/XmlParserCApi.h \
	parser/XmlParserException.h

EXTRA_SRCS = $(CPP_SRCS) $(TARGET) $(DEPS) $(SHARED_SRCS)

# Set CFLAGS to -m32 to build for linux32
CFLAGS= -g -Wall -DFMI_COSIMULATION -DSTANDALONE_XML_PARSER -DLIBXML_STATIC $(INCFLAGS)
INCFLAGS= -Iincludes -Iparser/libxml -Iparser -Ishared
LIBFLAGS= -lexpat -ldl -lxml2
OBJECTS = $(TARGET:.c=.o) $(SHARED_SRCS:.c=.o)

CXX=c++

# Build target
%.o: %.c
	$(CC) -o $@ -c $< $(CFLAGS)

all: main
	@echo "Invoke 'make run' to run a demo."

# Build the master algorithm executable.
main: $(OBJECTS)
	$(CXX) -o $@ $^ $(CPP_SRCS) $(CFLAGS) $(LIBFLAGS)

FMUS = \
	../fmus/inc20pt/inc20pt.fmu \
	../fmus/scale20pt/scale20pt.fmu \
	../fmus/out20pt/out20pt.fmu

FMUS0 = \
	../fmus/inc20pt/inc20pt.fmu \
	../fmus/out20pt/out20pt.fmu

# Run the master algorithm executable on two fmus.
run: main $(FMUS)
	./main -t15 -h1 -llogAll $(FMUS)

../fmus/inc20RC1pt/inc20pt.fmu:
	(cd ../fmus/inc20pt; $(MAKE))
../fmus/scale20pt/scale20pt.fmu:
	(cd ../fmus/scale20pt; $(MAKE))

# Run yilin
run2: main $(FMUS0)
	./main -t15 -h1 -llogAll $(FMUS0)

../fmus/inc20pt/inc20pt.fmu:
	(cd ../fmus/inc20pt; $(MAKE))
../fmus/out20pt/out20pt.fmu:
	(cd ../out20pt/out20pt; $(MAKE))

clean_fmus:
	rm -f $(FMUS)

# Debug the master algorithm with two fmus using gdb.
debug: main
	gdb --args ./main $(FMUS) -t15 -h1 -l logAll

# Use valgrind to look for memory leaks.
valgrind: main
	valgrind --leak-check=full --show-leak-kinds=all ./main $(FMUS) -t15 -h1 -l logAll

# Run the master algorithm and plot the output.
plot: run
	$(PTII)/bin/ptplot result.csv

clean: tmpclean

tmpclean:
	-rm -rf fmuTmp*
	-rm -f *.csv
	-rm -f shared/*.o

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't set MISC_FILES to $(DIRS) or else 'make sources' will try to invoke the demos.
MISC_FILES = includes \
	fmusdk-license.htm

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	parser \
	shared

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk	
