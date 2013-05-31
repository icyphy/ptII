# Makefile for Functional Mock-up Interface ++
#
# @Authors: Christopher Brooks (makefile only)
#
# @Version: $Id: makefile 65534 2013-01-25 22:53:11Z hudson $
#
# @Copyright (c) 2013 The Regents of the University of California.
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

# This directory contains polymorphic actors.

ME =		ptolemy/actor/lib/fmi/fmipp

DIRS = 		demo test

# Root of the Java directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(JNA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	fmipp
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	FMUModelExchange.java

OTHER_FILES_TO_BE_JARED = \
	fmipp.xml \
	fmipp-license.htm

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't set MISC_FILES to $(DIRS) or else 'make sources' will try to invoke the demos.
MISC_FILES = test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	doc \
	test

JCLASS = $(JSRCS:%.java=%.class) $(PTLIB) $(PTLIB2)

OPTFLAGS = -O3
OBJECTS=src/stack.o src/xml_parser.o src/FMU.o src/FMUIntegrator.o src/FMUIntegratorStepper.o src/History.o src/IncrementalFMU.o src/ModelManager.o

CFLAGS= -DBUILD_FMI_DLL $(PTJNI_SHAREDLIBRARY_CFLAG) -Iinclude -I$(PTBOOST_INCLUDE_DIR)

PTLIB=libfmipp.$(PTJNI_SHAREDLIBRARY_SUFFIX)
PTLIB2=libIncrementalFMU_wrap.$(PTJNI_SHAREDLIBRARY_SUFFIX)
all: jclass $(PTLIB) $(PTLIB2)
install: jclass jars
	cp $(PTLIB) $(PTLIB2) $(ROOT)/lib

$(PTLIB): $(OBJECTS)
	$(CXX) $(PTJNI_GCC_SHARED_FLAG) -o $(PTLIB) $(OBJECTS) -L$(LIBEXPAT_LIB_DIR) -lexpat 
	if [ "`uname -s`" = "Darwin" ]; then \
		ln $(PTLIB) libfmipp.dylib; \
	fi

src/stack.o: src/stack.c include/stack.h
	$(CC) $(INCLUDE) -c $(OPTFLAGS) $(CFLAGS) src/stack.c -o src/stack.o

src/xml_parser.o: src/xml_parser.c include/xml_parser.h
	$(CC) $(INCLUDE) -c $(OPTFLAGS) $(CFLAGS) src/xml_parser.c -o src/xml_parser.o

src/FMU.o: src/FMU.cpp include/FMU.h
	$(CXX) $(INCLUDE) -c $(OPTFLAGS) $(CFLAGS) src/FMU.cpp -o src/FMU.o

src/FMUIntegrator.o: src/FMUIntegrator.cpp include/FMUIntegrator.h
	$(CXX) $(INCLUDE) -c $(OPTFLAGS) $(CFLAGS) src/FMUIntegrator.cpp -o src/FMUIntegrator.o

src/FMUIntegratorStepper.o: src/FMUIntegratorStepper.cpp include/FMUIntegratorStepper.h
	$(CXX) $(INCLUDE) -c $(OPTFLAGS) $(CFLAGS) src/FMUIntegratorStepper.cpp -o src/FMUIntegratorStepper.o

src/History.o:  src/History.cpp include/History.h
	$(CXX) $(INCLUDE) -c $(OPTFLAGS) $(CFLAGS) src/History.cpp -o src/History.o

src/IncrementalFMU.o: src/IncrementalFMU.cpp include/IncrementalFMU.h
	$(CXX) $(INCLUDE) -c $(OPTFLAGS) $(CFLAGS) src/IncrementalFMU.cpp -o src/IncrementalFMU.o

src/ModelManager.o:  src/ModelManager.cpp include/ModelManager.h
	$(CXX) $(INCLUDE) -c $(OPTFLAGS) $(CFLAGS) src/ModelManager.cpp -o src/ModelManager.o

.PHONY = swig
swig: $(PTLIB2)
$(PTLIB2): swig/IncrementalFMU.i
	swig -c++ -java -package ptolemy.actor.lib.fmi.fmipp.swig  swig/IncrementalFMU.i
	$(CXX) $(INC) -c $(OPTFLAGS) $(CFLAGS) swig/IncrementalFMU_wrap.cxx -o swig/IncrementalFMU_wrap.o -I$(PTJNI_INCLUDE) -I$(PTJNI_INCLUDE)/$(PTJNI_ARCHITECTURE)
	$(CXX) -shared -o $(PTLIB2) swig/IncrementalFMU_wrap.o -L. -lfmipp -lexpat $(PTJNI_DL_LIBRARY)
	javac swig/*.java

KRUFT = swig/*.o swig/*.class src/*.o

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
